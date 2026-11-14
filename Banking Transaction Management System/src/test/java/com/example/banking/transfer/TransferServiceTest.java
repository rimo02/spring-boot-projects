package com.example.banking.transfer;
import com.example.banking.account.repository.AccountRepository;
import com.example.banking.auth.entity.User;
import com.example.banking.common.exception.UnauthorizedAccessException;
import com.example.banking.ledger.repository.TransactionRepository;
import com.example.banking.security.Actor;
import com.example.banking.transfer.dto.TransferDtos;
import com.example.banking.transfer.repository.TransferRequestRepository;
import com.example.banking.transfer.service.TransferService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {
    @Mock AccountRepository accounts;
    @Mock TransferRequestRepository requests;
    @Mock TransactionRepository ledger;
    @InjectMocks TransferService service;
    final Actor actor=new Actor(UUID.randomUUID(),User.Role.CUSTOMER);
    @Test void rejectsSelfTransferWithoutAccessingDatabase() {
        UUID id=UUID.randomUUID();
        assertThatThrownBy(()->service.transfer(actor,"key",new TransferDtos.Create(id,id,BigDecimal.ONE)))
            .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(accounts,requests,ledger);
    }
    @Test void cannotSpendFromAnotherCustomersAccount() {
        UUID source=UUID.randomUUID(),destination=UUID.randomUUID();
        when(requests.findByUserIdAndIdempotencyKey(actor.id(),"key")).thenReturn(Optional.empty());
        when(accounts.existsById(source)).thenReturn(true);
        when(accounts.existsByIdAndOwnerId(source,actor.id())).thenReturn(false);
        assertThatThrownBy(()->service.transfer(actor,"key",new TransferDtos.Create(source,destination,BigDecimal.ONE)))
            .isInstanceOf(UnauthorizedAccessException.class);
        verifyNoInteractions(ledger);
        verify(requests,never()).reserve(any(),any(),any(),any(),any(),any(),any());
    }
}
