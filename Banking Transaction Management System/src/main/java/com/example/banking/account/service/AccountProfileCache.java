package com.example.banking.account.service;
import com.example.banking.account.dto.AccountDtos.Profile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.*;
import java.time.Duration;
import java.util.*;
@Component
public class AccountProfileCache {
    private static final Logger log=LoggerFactory.getLogger(AccountProfileCache.class);
    private final StringRedisTemplate redis; private final ObjectMapper json;
    public AccountProfileCache(StringRedisTemplate redis,ObjectMapper json) { this.redis=redis; this.json=json; }
    public Optional<Profile> get(UUID id) {
        try {
            String value=redis.opsForValue().get(key(id));
            return value == null ? Optional.empty() : Optional.of(json.readValue(value,Profile.class));
        } catch (RuntimeException | JsonProcessingException e) { unavailable(); return Optional.empty(); }
    }
    public void put(Profile profile) {
        try { redis.opsForValue().set(key(profile.id()),json.writeValueAsString(profile),Duration.ofMinutes(5)); }
        catch(RuntimeException | JsonProcessingException e) { unavailable(); }
    }
    public void evict(UUID id) {
        try { redis.delete(key(id)); } catch(RuntimeException e) { unavailable(); }
    }
    private String key(UUID id) { return "banking:account-profile:v1:"+id; }
    private void unavailable() { log.warn("event=profile_cache_unavailable"); }
}
