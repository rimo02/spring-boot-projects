package com.example.banking.ledger.repository;

import com.example.banking.ledger.entity.Transaction;
import org.springframework.data.jpa.repository.*;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
}
