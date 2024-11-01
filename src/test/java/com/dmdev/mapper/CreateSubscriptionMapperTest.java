package com.dmdev.mapper;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CreateSubscriptionMapperTest {

    private final CreateSubscriptionMapper createSubscriptionMapper = CreateSubscriptionMapper.getInstance();

    @Test
    void shouldSuccessfullyCreateSubscription() {
        var dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(Instant.parse("2025-01-23T00:00:00Z"))
                .build();
        var expectedResult = Subscription.builder()
                .userId(1)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(Instant.parse("2025-01-23T00:00:00Z"))
                .status(Status.ACTIVE)
                .build();

        var actualResult = createSubscriptionMapper.map(dto);

        assertEquals(expectedResult, actualResult);
    }
}