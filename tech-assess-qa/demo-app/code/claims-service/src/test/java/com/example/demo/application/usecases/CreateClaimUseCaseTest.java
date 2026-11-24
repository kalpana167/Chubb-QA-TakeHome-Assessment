
package com.example.demo.application.usecases;

import com.example.demo.application.exceptions.UserNotFoundException;
import com.example.demo.domain.claim.Claim;
import com.example.demo.domain.claim.ClaimRepository;
import com.example.demo.domain.claim.ClaimStatus;
import com.example.demo.domain.claim.events.ClaimSubmitted;
import com.example.demo.user.domain.UserId;
import com.example.demo.user.domain.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateClaimUseCaseTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CreateClaimUseCase createClaimUseCase;

    private UUID userId;
    private CreateClaimCommand command;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        command = new CreateClaimCommand(
                userId,
                LocalDate.now(),
                "Bengaluru, Karnataka",
                "Vehicle accident damage",
                new BigDecimal("50000")
        );

        lenient().when(meterRegistry.counter(
                "claims.submitted",
                "status", "SUBMITTED",
                "userId", userId.toString()
        )).thenReturn(counter);
    }

    @Test
    void shouldCreateClaimSuccessfullyWhenUserExists() {

        when(userRepository.findById(any(UserId.class)))
                .thenReturn(Optional.of(
                        mock(com.example.demo.user.domain.User.class)
                ));

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.setField(
                createClaimUseCase, "cdcEnabled", true
        );

        Claim result = createClaimUseCase.execute(command);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(ClaimStatus.SUBMITTED, result.getStatus());
        assertEquals(
                new BigDecimal("50000"),
                result.getClaimAmount()
        );

        verify(claimRepository, times(1)).save(any(Claim.class));
        verify(counter, times(1)).increment();
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(any(UserId.class)))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> createClaimUseCase.execute(command)
        );

        verify(claimRepository, never()).save(any(Claim.class));
        verify(counter, never()).increment();
    }

    @Test
    void shouldNotPublishEventDirectlyWhenCdcIsEnabled() {

        when(userRepository.findById(any(UserId.class)))
                .thenReturn(Optional.of(
                        mock(com.example.demo.user.domain.User.class)
                ));

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.setField(
                createClaimUseCase, "cdcEnabled", true
        );

        Claim result = createClaimUseCase.execute(command);

        verify(eventPublisher, never())
                .publishEvent(any(ClaimSubmitted.class));

        assertFalse(result.getDomainEvents().isEmpty());
    }

    @Test
    void shouldPublishAndClearEventsWhenCdcIsDisabled() {

        when(userRepository.findById(any(UserId.class)))
                .thenReturn(Optional.of(
                        mock(com.example.demo.user.domain.User.class)
                ));

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.setField(
                createClaimUseCase, "cdcEnabled", false
        );

        Claim result = createClaimUseCase.execute(command);

        verify(eventPublisher, times(1))
                .publishEvent(any(ClaimSubmitted.class));

        assertTrue(result.getDomainEvents().isEmpty());
    }

    @Test
    void shouldIncrementClaimSubmissionMetric() {

        when(userRepository.findById(any(UserId.class)))
                .thenReturn(Optional.of(
                        mock(com.example.demo.user.domain.User.class)
                ));

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.setField(
                createClaimUseCase, "cdcEnabled", true
        );

        createClaimUseCase.execute(command);

        verify(meterRegistry).counter(
                "claims.submitted",
                "status", "SUBMITTED",
                "userId", userId.toString()
        );

        verify(counter, times(1)).increment();
    }
}
