package com.example.banking.transfer.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="transfer_requests")
public class TransferRequest {
    @Id private UUID id;
    private UUID userId;
    private String idempotencyKey;
    private String payloadHash;
    @Enumerated(EnumType.STRING) private Status status;
    private UUID sourceId;
    private UUID destinationId;
    @Column(precision=19,scale=2) private BigDecimal amount;
    private Instant createdAt;
    private Instant completedAt;
    protected TransferRequest() {}
    public void complete(Instant now) { status=Status.COMPLETED; completedAt=now; }
    public UUID getId() { return id; }
    public String getPayloadHash() { return payloadHash; }
    public Status getStatus() { return status; }
    public UUID getSourceId() { return sourceId; }
    public UUID getDestinationId() { return destinationId; }
    public BigDecimal getAmount() { return amount; }
    public Instant getCompletedAt() { return completedAt; }
    public enum Status { PROCESSING, COMPLETED }
}
