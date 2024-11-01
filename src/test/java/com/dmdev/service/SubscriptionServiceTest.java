package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.exception.ValidationException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.Error;
import com.dmdev.validator.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {
    @Mock
    private Clock clock;
    @Mock
    private SubscriptionDao subscriptionDao;
    @Mock
    private CreateSubscriptionMapper createSubscriptionMapper;
    @Mock
    private CreateSubscriptionValidator createSubscriptionValidator;
    @InjectMocks
    private SubscriptionService subscriptionService;
    @Test
    void upsertSuccess() {
        var createSubscriptionDto = getSubscriptionDto();
        var createSubscription = getSubscription();
        doReturn(new ValidationResult()).when(createSubscriptionValidator).validate(createSubscriptionDto);
        doReturn(createSubscription).when(createSubscriptionMapper).map(createSubscriptionDto);
        doReturn(createSubscription).when(subscriptionDao).upsert(createSubscription);

        var actualResult = subscriptionService.upsert(createSubscriptionDto);

        assertThat(actualResult).isEqualTo(createSubscription);
        verify(createSubscriptionValidator).validate(createSubscriptionDto);
        verify(createSubscriptionMapper).map(createSubscriptionDto);
        verify(subscriptionDao).upsert(createSubscription);
    }

    @Test
    void shouldValidationExceptionIfUserIdInvalid() {
        var createSubscriptionDto = getSubscriptionDto();
        ValidationResult validationResult = new ValidationResult();
        validationResult.add(Error.of(100, "userId is invalid"));
        doReturn(validationResult).when(createSubscriptionValidator).validate(createSubscriptionDto);

        assertThrows(ValidationException.class, () -> subscriptionService.upsert(createSubscriptionDto));
        verifyNoInteractions(createSubscriptionMapper, subscriptionDao);
    }

    @Test
    void cancelSuccess() {
        var createSubscription = getSubscription();
        doReturn(Optional.of(createSubscription)).when(subscriptionDao).findById(createSubscription.getUserId());

        subscriptionService.cancel(createSubscription.getUserId());

        verify(subscriptionDao).findById(anyInt());
    }

    @Test
    void shouldSubscriptionExceptionIfStatusNotActive() {
        var createSubscription = getSubscription();
        createSubscription.setStatus(Status.CANCELED);
        doReturn(Optional.of(createSubscription)).when(subscriptionDao).findById(createSubscription.getUserId());

        var subscriptionException = assertThrows(SubscriptionException.class, () -> subscriptionService.cancel(createSubscription.getUserId()));
        assertThat(subscriptionException).hasMessage(String.format("Only active subscription %d can be canceled", createSubscription.getUserId()));
    }

    @Test
    void expireSuccess() {
        var createSubscription = getSubscription();
        doReturn(Optional.of(createSubscription)).when(subscriptionDao).findById(createSubscription.getUserId());

        subscriptionService.expire(createSubscription.getUserId());

        verify(subscriptionDao).findById(createSubscription.getUserId());
        verify(subscriptionDao).update(createSubscription);
    }

    @Test
    void shouldSubscriptionExceptionIfStatusExpired() {
        var createSubscription = getSubscription();
        createSubscription.setStatus(Status.EXPIRED);
        doReturn(Optional.of(createSubscription)).when(subscriptionDao).findById(createSubscription.getUserId());

        var subscriptionException = assertThrows(SubscriptionException.class, () -> subscriptionService.expire(createSubscription.getUserId()));
        assertThat(subscriptionException).hasMessage(String.format("Subscription %d has already expired", createSubscription.getUserId()));
    }

    private Subscription getSubscription() {
        return Subscription.builder()
                .userId(1)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(Instant.parse("2025-01-23T00:00:00Z"))
                .status(Status.ACTIVE)
                .build();
    }

    private CreateSubscriptionDto getSubscriptionDto() {
        return CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(Instant.parse("2025-01-23T00:00:00Z"))
                .build();
    }
}