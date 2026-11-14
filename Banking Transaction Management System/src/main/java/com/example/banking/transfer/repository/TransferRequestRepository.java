package com.example.banking.transfer.repository;
import com.example.banking.transfer.entity.TransferRequest;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface TransferRequestRepository extends JpaRepository<TransferRequest,UUID> {
    Optional<TransferRequest> findByUserIdAndIdempotencyKey(UUID userId,String key);
    // ON CONFLICT waits on an uncommitted competing key. At READ COMMITTED the following SELECT
    // sees its committed result. On rollback, this INSERT instead succeeds and owns the key.
    @Modifying
    @Query(value="""
        INSERT INTO transfer_requests(id,user_id,idempotency_key,payload_hash,status,source_id,destination_id,amount,created_at)
        VALUES (:id,:userId,:key,:hash,'PROCESSING',:source,:destination,:amount,now())
        ON CONFLICT (user_id,idempotency_key) DO NOTHING
        """,nativeQuery=true)
    int reserve(@Param("id") UUID id,@Param("userId") UUID userId,@Param("key") String key,
                @Param("hash") String hash,@Param("source") UUID source,@Param("destination") UUID destination,
                @Param("amount") java.math.BigDecimal amount);
}
