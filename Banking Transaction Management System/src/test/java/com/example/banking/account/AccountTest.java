package com.example.banking.account;
import com.example.banking.account.entity.Account;
import com.example.banking.common.exception.InsufficientBalanceException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
class AccountTest {
    @Test void debitAndCreditUseExactDecimalArithmetic() {
        var account=new Account(UUID.randomUUID(),"Savings");
        account.credit(new BigDecimal("10.30")); account.debit(new BigDecimal("0.20"));
        assertThat(account.getBalance()).isEqualByComparingTo("10.10");
    }
    @Test void cannotOverdrawAndFailureDoesNotChangeBalance() {
        var account=new Account(UUID.randomUUID(),"Savings");
        account.credit(new BigDecimal("10.00"));
        assertThatThrownBy(()->account.debit(new BigDecimal("10.01"))).isInstanceOf(InsufficientBalanceException.class);
        assertThat(account.getBalance()).isEqualByComparingTo("10.00");
    }
    @Test void rejectsInvalidMoney() {
        var account=new Account(UUID.randomUUID(),"Savings");
        for(String value:new String[]{"0.00","-1.00","0.001"}) {
            assertThatThrownBy(()->account.credit(new BigDecimal(value))).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
