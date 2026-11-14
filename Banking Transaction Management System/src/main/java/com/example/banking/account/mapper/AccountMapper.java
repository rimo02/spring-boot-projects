package com.example.banking.account.mapper;
import com.example.banking.account.entity.Account;
import com.example.banking.account.dto.AccountDtos;
public final class AccountMapper {
    private AccountMapper() {}
    public static AccountDtos.View view(Account a) {
        return new AccountDtos.View(a.getId(),a.getOwnerId(),a.getDisplayName(),a.getCurrency(),a.getBalance(),a.getCreatedAt());
    }
    public static AccountDtos.Profile profile(Account a) {
        return new AccountDtos.Profile(a.getId(),a.getOwnerId(),a.getDisplayName(),a.getCurrency());
    }
}
