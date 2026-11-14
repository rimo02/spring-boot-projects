package com.example.banking;

import com.example.banking.account.entity.Account;
import com.example.banking.account.repository.AccountRepository;
import com.example.banking.auth.entity.User;
import com.example.banking.auth.repository.UserRepository;
import com.example.banking.common.exception.*;
import com.example.banking.security.Actor;
import com.example.banking.transfer.dto.TransferDtos;
import com.example.banking.transfer.service.TransferService;
import com.example.banking.report.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.junit.jupiter.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@SpringBootTest @AutoConfigureMockMvc @Testcontainers @ExtendWith(OutputCaptureExtension.class)
class BankingIT {
    @Container static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:17-alpine");
    @Container static GenericContainer<?> redis=new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine")).withExposedPorts(6379);
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",postgres::getJdbcUrl);
        r.add("spring.datasource.username",postgres::getUsername);
        r.add("spring.datasource.password",postgres::getPassword);
        r.add("banking.jwt.secret",()->Base64.getEncoder().encodeToString(new byte[32]));
        r.add("banking.reports.cron",()->"-");
        r.add("spring.data.redis.host",redis::getHost);
        r.add("spring.data.redis.port",()->redis.getMappedPort(6379));
        // CI/cold JVM startup can be slow; production cache timeouts remain deliberately short.
        r.add("spring.data.redis.timeout",()->"30s");
        r.add("spring.data.redis.connect-timeout",()->"30s");
    }
    @Autowired TransferService transfers;
    @Autowired AccountRepository accounts;
    @Autowired UserRepository users;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired ReportJobService jobs;
    @Autowired ReportService reports;
    @Autowired com.example.banking.account.service.AccountService accountService;
    @Autowired org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    @Autowired com.example.banking.ledger.service.LedgerService ledgerService;
    Actor actor; UUID source,destination;
    @BeforeEach void seed() {
        jdbc.execute("TRUNCATE transactions, transfer_requests, accounts, app_users, daily_reports CASCADE");
        User user=users.saveAndFlush(new User(UUID.randomUUID()+"@example.test","unused"));
        actor=new Actor(user.getId(),User.Role.CUSTOMER);
        Account a=new Account(user.getId(),"Source"),b=new Account(user.getId(),"Destination");
        a.credit(new BigDecimal("100.00")); accounts.saveAllAndFlush(List.of(a,b));
        source=a.getId(); destination=b.getId();
    }
    TransferDtos.Create request(String amount) { return new TransferDtos.Create(source,destination,new BigDecimal(amount)); }
    @Test void successfulTransferAndReplayHaveExactlyTwoLedgerEntries() {
        var receipt=transfers.transfer(actor,"retry",request("30.00"));
        var replay=transfers.transfer(actor,"retry",request("30.0"));
        assertThat(replay).isEqualTo(receipt);
        assertBalance(source,"70.00"); assertBalance(destination,"30.00");
        assertThat(count("transactions")).isEqualTo(2);
        assertThat(count("transfer_requests")).isEqualTo(1);
    }
    @Test void insufficientFundsRollsBackReservationAndCanRetryAfterFunding() {
        assertThatThrownBy(()->transfers.transfer(actor,"insufficient",request("101.00"))).isInstanceOf(InsufficientBalanceException.class);
        assertBalance(source,"100.00"); assertBalance(destination,"0.00");
        assertThat(count("transactions")).isZero(); assertThat(count("transfer_requests")).isZero();
        jdbc.update("UPDATE accounts SET balance=200 WHERE id=?",source);
        transfers.transfer(actor,"insufficient",request("101.00"));
        assertBalance(source,"99.00");
    }
    @Test void databaseFailureAfterBalanceMutationRollsBackEverything(CapturedOutput output) {
        jdbc.execute("ALTER TABLE transactions ADD CONSTRAINT test_reject_credit CHECK(type <> 'CREDIT')");
        try {
            assertThatThrownBy(()->transfers.transfer(actor,"rollback",request("20.00"))).isInstanceOf(RuntimeException.class);
            assertBalance(source,"100.00"); assertBalance(destination,"0.00");
            assertThat(count("transactions")).isZero(); assertThat(count("transfer_requests")).isZero();
            assertThat(output.getAll()).doesNotContain("Failing row contains");
        } finally { jdbc.execute("ALTER TABLE transactions DROP CONSTRAINT test_reject_credit"); }
    }
    @Test void sameKeyDifferentPayloadIsRejected() {
        transfers.transfer(actor,"same",request("10.00"));
        assertThatThrownBy(()->transfers.transfer(actor,"same",request("11.00"))).isInstanceOf(DuplicateRequestException.class);
        assertBalance(source,"90.00"); assertThat(count("transactions")).isEqualTo(2);
    }
    @Test void concurrentDebitsCannotOverdraw() throws Exception {
        var results=race(()->attempt("a",request("80.00")),()->attempt("b",request("80.00")));
        assertThat(results).containsExactlyInAnyOrder("success","insufficient");
        assertBalance(source,"20.00"); assertBalance(destination,"80.00"); assertThat(count("transactions")).isEqualTo(2);
    }
    @Test void simultaneousSameKeyExecutesOnce() throws Exception {
        var results=race(()->transfers.transfer(actor,"concurrent",request("40.00")),
                         ()->transfers.transfer(actor,"concurrent",request("40.00")));
        assertThat(results.get(0)).isEqualTo(results.get(1));
        assertBalance(source,"60.00"); assertThat(count("transactions")).isEqualTo(2);
    }
    @Test void oppositeDirectionTransfersDoNotDeadlock() throws Exception {
        jdbc.update("UPDATE accounts SET balance=100 WHERE id=?",destination);
        race(()->transfers.transfer(actor,"forward",request("10.00")),
             ()->transfers.transfer(actor,"reverse",new TransferDtos.Create(destination,source,new BigDecimal("15.00"))));
        assertBalance(source,"105.00"); assertBalance(destination,"95.00");
        assertThat(count("transactions")).isEqualTo(4);
    }
    @Test void authenticationOwnershipAndAdminBoundaries() throws Exception {
        mvc.perform(get("/api/accounts/"+source)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        var other=new Actor(UUID.randomUUID(),User.Role.CUSTOMER);
        var auth=new UsernamePasswordAuthenticationToken(other,null,List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        mvc.perform(get("/api/accounts/"+source).with(authentication(auth))).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/accounts").with(authentication(auth))).andExpect(status().isForbidden());
    }
    @Test void registrationLoginJwtAndValidation() throws Exception {
        String body="{\"email\":\"new@example.test\",\"password\":\"a-long-test-password\"}";
        String response=mvc.perform(post("/api/auth/register").contentType("application/json").content(body))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        var json=new com.fasterxml.jackson.databind.ObjectMapper().readTree(response);
        mvc.perform(get("/api/accounts").header("Authorization","Bearer "+json.get("accessToken").asText())).andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType("application/json").content(body)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/register").contentType("application/json").content("{\"email\":\"bad\",\"password\":\"short\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/accounts").header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
    }
    @Test void dailyBatchJobPersistsBalancedTotalsAndIsRestartSafe() {
        transfers.transfer(actor,"report",request("25.00"));
        LocalDate yesterday=LocalDate.now(ZoneOffset.UTC).minusDays(1);
        jdbc.update("UPDATE transactions SET created_at=?",java.sql.Timestamp.from(yesterday.atTime(12,0).toInstant(ZoneOffset.UTC)));
        assertThat(jobs.run(yesterday).status()).isEqualTo("COMPLETED");
        var report=reports.get(yesterday);
        assertThat(report.totalDebits()).isEqualByComparingTo("25.00");
        assertThat(report.totalCredits()).isEqualByComparingTo("25.00");
        assertThat(report.transactionCount()).isEqualTo(2);
        assertThat(jobs.run(yesterday).status()).isEqualTo("ALREADY_COMPLETED");
    }
    @Test void profileCacheExcludesBalanceAndEvictsAfterRename() {
        accountService.profile(actor,source);
        String key="banking:account-profile:v1:"+source;
        assertThat(redisTemplate.opsForValue().get(key)).contains("Source").doesNotContain("balance","password");
        accountService.rename(actor,source,new com.example.banking.account.dto.AccountDtos.Rename("Renamed"));
        assertThat(redisTemplate.hasKey(key)).isFalse();
        assertThat(accountService.profile(actor,source).displayName()).isEqualTo("Renamed");
        transfers.transfer(actor,"cached-profile",request("10.00"));
        assertThat(accountService.get(actor,source).balance()).isEqualByComparingTo("90.00");
    }
    @Test void statementTotalsAreIndependentOfPaginationAndFiltersUseHalfOpenRange() {
        transfers.transfer(actor,"statement-a",request("10.00"));
        transfers.transfer(actor,"statement-b",request("15.00"));
        Instant from=Instant.now().minusSeconds(60),to=Instant.now().plusSeconds(60);
        var statement=ledgerService.statement(actor,source,from,to,org.springframework.data.domain.PageRequest.of(0,1));
        assertThat(statement.openingBalance()).isEqualByComparingTo("100.00");
        assertThat(statement.closingBalance()).isEqualByComparingTo("75.00");
        assertThat(statement.debits()).isEqualByComparingTo("25.00");
        assertThat(statement.transactionCount()).isEqualTo(2);
        assertThat(statement.entries().content()).hasSize(1);
        assertThat(statement.entries().totalElements()).isEqualTo(2);
        var before=ledgerService.history(actor,source,from.minusSeconds(60),from,org.springframework.data.domain.PageRequest.of(0,20));
        assertThat(before.getContent()).isEmpty();
    }
    @Test void concurrentSameKeyWithDifferentPayloadRejectsOne() throws Exception {
        var results=race(()->attemptConflict(request("10.00")),()->attemptConflict(request("20.00")));
        assertThat(results).containsExactlyInAnyOrder("success","conflict");
        assertThat(count("transactions")).isEqualTo(2);
        assertThat(count("transfer_requests")).isEqualTo(1);
    }
    private String attemptConflict(TransferDtos.Create input) {
        try { transfers.transfer(actor,"concurrent-conflict",input); return "success"; }
        catch(DuplicateRequestException e) { return "conflict"; }
    }
    private String attempt(String key,TransferDtos.Create request) {
        try { transfers.transfer(actor,key,request); return "success"; }
        catch(InsufficientBalanceException e) { return "insufficient"; }
    }
    private <T> List<T> race(Callable<T> first,Callable<T> second) throws Exception {
        var gate=new CountDownLatch(1);
        try(var executor=Executors.newFixedThreadPool(2)) {
            var a=executor.submit(()->{ gate.await(); return first.call(); });
            var b=executor.submit(()->{ gate.await(); return second.call(); });
            gate.countDown();
            return List.of(a.get(40,TimeUnit.SECONDS),b.get(40,TimeUnit.SECONDS));
        }
    }
    private void assertBalance(UUID id,String expected) { assertThat(accounts.findById(id).orElseThrow().getBalance()).isEqualByComparingTo(expected); }
    private long count(String table) { return jdbc.queryForObject("SELECT count(*) FROM "+table,Long.class); }
}
