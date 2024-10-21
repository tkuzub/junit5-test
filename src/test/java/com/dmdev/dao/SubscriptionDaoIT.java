package com.dmdev.dao;

import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionDaoIT extends IntegrationTestBase {

    private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();

    @Test
    void findAll() {
        var subscription1 = getSubscription(getRandomUserId());
        var subscription2 = getSubscription(getRandomUserId());
        var subscription3 = getSubscription(getRandomUserId());
        subscriptionDao.insert(subscription1);
        subscriptionDao.insert(subscription2);
        subscriptionDao.insert(subscription3);

        var ids = subscriptionDao.findAll().stream().map(Subscription::getId).toList();

        assertThat(ids).hasSize(3);
        assertThat(ids).contains(subscription1.getId(), subscription2.getId(), subscription3.getId());
    }

    @Test
    void findById() {
        var subscription = getSubscription(getRandomUserId());
        subscriptionDao.insert(subscription);

        var actualResult = subscriptionDao.findById(subscription.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get()).isEqualTo(subscription);
    }

    @Test
    void shouldSuccessfulDelete() {
        var subscription = getSubscription(getRandomUserId());
        subscriptionDao.insert(subscription);

        var actualResult = subscriptionDao.delete(subscription.getId());

        assertTrue(actualResult);
    }

    @Test
    void shouldNotSuccessfulDeleteIfUserDoesNotExist() {
        var subscription = getSubscription(getRandomUserId());
        subscriptionDao.insert(subscription);

        var actualResult = subscriptionDao.delete(getRandomUserId());

        assertFalse(actualResult);
    }

    @Test
    void shouldSuccessfulUpdate() {
        var subscription = getSubscription(getRandomUserId());
        subscriptionDao.insert(subscription);
        subscription.setStatus(Status.CANCELED);
        subscription.setProvider(Provider.APPLE);

        subscriptionDao.update(subscription);
        Optional<Subscription> subscriptionId = subscriptionDao.findById(subscription.getId());

        subscriptionId.ifPresent(actualResult -> assertThat(actualResult).isEqualTo(subscription));
    }

    @Test
    void shouldNotSuccessfulUpdateIfUserDataDoesNotUpdate() {
        var subscription = getSubscription(getRandomUserId());
        subscriptionDao.insert(subscription);
        subscription.setStatus(Status.CANCELED);
        subscription.setProvider(Provider.APPLE);

        subscriptionDao.update(subscription);
        Optional<Subscription> subscriptionId = subscriptionDao.findById(getRandomUserId());

        assertTrue(subscriptionId.isEmpty());
    }

    @Test
    void insert() {
        var subscription = getSubscription(getRandomUserId());

        Subscription actualResult = subscriptionDao.insert(subscription);

        assertNotNull(actualResult);

    }

    @Test
    void findByUserId() {
        var subscription = getSubscription(getRandomUserId());
        subscriptionDao.insert(subscription);
        var possibleSubscription = subscriptionDao.findByUserId(subscription.getUserId()).stream().findFirst();

        possibleSubscription.ifPresent(actualResult -> assertThat(subscription.getUserId()).isEqualTo(actualResult.getUserId()));
    }

    @Test
    void shouldNotFindByUserIdIfUserIdDoesNotExist() {
        var subscription = getSubscription(getRandomUserId());
        subscriptionDao.insert(subscription);
        var possibleSubscription = subscriptionDao.findByUserId(getRandomUserId()).stream().findFirst();

        assertTrue(possibleSubscription.isEmpty());
    }

    private Subscription getSubscription(Integer userId) {
        return Subscription.builder()
                .userId(userId)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(Instant.parse("2025-01-23T00:00:00Z"))
                .status(Status.ACTIVE)
                .build();
    }

    private Integer getRandomUserId() {
        return ThreadLocalRandom.current().nextInt(10000);
    }
}