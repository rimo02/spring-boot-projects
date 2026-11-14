package com.example.banking.account.repository;
import com.example.banking.account.entity.Account;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface AccountRepository extends JpaRepository<Account,UUID> {
    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);
    Page<Account> findByOwnerId(UUID ownerId, Pageable pageable);
    // NO KEY UPDATE serializes balance writers without conflicting with FK KEY SHARE locks.
    @Query(value="select * from accounts where id = :id for no key update",nativeQuery=true)
    Optional<Account> lockById(@Param("id") UUID id);
}
