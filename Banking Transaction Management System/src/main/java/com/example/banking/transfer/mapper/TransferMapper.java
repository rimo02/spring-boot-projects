package com.example.banking.transfer.mapper;

import com.example.banking.transfer.entity.TransferRequest;
import com.example.banking.transfer.dto.TransferDtos;

public final class TransferMapper {
    private TransferMapper() {
    }

    public static TransferDtos.Receipt receipt(TransferRequest t) {
        return new TransferDtos.Receipt(t.getId(), t.getSourceId(), t.getDestinationId(), t.getAmount(),
                t.getStatus().name(), t.getCompletedAt());
    }
}
