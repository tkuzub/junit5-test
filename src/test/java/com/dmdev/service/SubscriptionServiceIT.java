package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.integration.IntegrationTestBase;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SubscriptionServiceIT extends IntegrationTestBase {

    private SubscriptionService subscriptionService;
    private SubscriptionDao subscriptionDao;

    @BeforeEach
    void init() {
        var clock = Clock.systemUTC();
        subscriptionDao = SubscriptionDao.getInstance();
        subscriptionService = new SubscriptionService(
                subscriptionDao,
                CreateSubscriptionMapper.getInstance(),
                CreateSubscriptionValidator.getInstance(),
                clock
        );
    }

    @Test
    void upsertSuccess() {
        subscriptionDao.insert(getSubscription());

        var upsertSubscription = subscriptionService.upsert(getSubscriptionDto());
        var actualResult = subscriptionDao.findById(upsertSubscription.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get().getId()).isEqualTo(upsertSubscription.getId());
    }

    @Test
    void shouldExpireSuccess() {
        var subscription = getSubscription();
        subscriptionDao.insert(subscription);

        subscriptionService.expire(subscription.getId());
        var actualResult = subscriptionDao.findById(subscription.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get().getStatus()).isEqualTo(Status.EXPIRED);
    }

    @Test
    void shouldCanselUnSuccessIfStatusExpired() {
        var subscription = getSubscription();
        subscription.setStatus(Status.EXPIRED);
        subscriptionDao.insert(subscription);
        Optional<Subscription> actualResult = subscriptionDao.findById(subscription.getId());

        assertThat(actualResult).isPresent();
        var subscriptionException = assertThrows(SubscriptionException.class, () -> subscriptionService.expire(actualResult.get().getId()));
        assertThat(subscriptionException).hasMessageContaining(String.format("Subscription %d has already expired", actualResult.get().getId()));
    }

    @Test
    void shouldCancelSuccess() {
        var subscription = getSubscription();
        subscriptionDao.insert(subscription);

        subscriptionService.cancel(subscription.getId());
        var actualResult = subscriptionDao.findById(subscription.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get().getStatus()).isEqualTo(Status.CANCELED);
    }

    @Test
    void shouldCanselUnSuccessIfStatusNotActive() {
        var subscription = getSubscription();
        subscription.setStatus(Status.CANCELED);
        subscriptionDao.insert(subscription);
        var actualResult = subscriptionDao.findById(subscription.getId());

        assertThat(actualResult).isPresent();
        var subscriptionException = assertThrows(SubscriptionException.class, () -> subscriptionService.cancel(actualResult.get().getId()));
        assertThat(subscriptionException).hasMessageContaining(String.format("Only active subscription %d can be canceled", actualResult.get().getId()));
    }

    private CreateSubscriptionDto getSubscriptionDto() {
        return CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(Instant.parse("2025-01-23T00:00:00Z"))
                .build();
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
}
