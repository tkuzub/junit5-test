package com.dmdev.validator;

import com.dmdev.dto.CreateSubscriptionDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class CreateSubscriptionValidatorTest {

    private final CreateSubscriptionValidator validator = CreateSubscriptionValidator.getInstance();

    @Test
    void shouldPassValidation() {
        var dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(Instant.parse("2025-01-23T00:00:00Z"))
                .build();

        var actual = validator.validate(dto);

        assertFalse(actual.hasErrors());
    }


    @Test
    void shouldFailValidationIfInvalidUserId() {
        var dto = CreateSubscriptionDto.builder()
                .userId(null)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(Instant.parse("2025-01-23T00:00:00Z"))
                .build();

        var validationResult = validator.validate(dto);

        Map<Integer, String> mapErrors = validationResult.getErrors().stream()
                .collect(Collectors
                        .toMap(Error::getCode, Error::getMessage));
        assertAll(
                () -> assertTrue(validationResult.hasErrors()),
                () -> assertThat(mapErrors).containsKey(100),
                () -> assertThat(mapErrors).containsValue("userId is invalid")
        );
    }

    @Test
    void shouldFailValidationIfInvalidName() {
        var dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("")
                .provider("GOOGLE")
                .expirationDate(Instant.parse("2025-01-23T00:00:00Z"))
                .build();

        var validationResult = validator.validate(dto);

        Map<Integer, String> mapErrors = validationResult.getErrors().stream()
                .collect(Collectors
                        .toMap(Error::getCode, Error::getMessage));
        assertAll(
                () -> assertThat(mapErrors).containsKey(101),
                () -> assertThat(mapErrors).containsValue("name is invalid")
        );
    }

    @Test
    void shouldFailValidationIfInvalidProvider() {
        var dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("some_provider")
                .expirationDate(Instant.parse("2025-01-23T00:00:00Z"))
                .build();

        var validationResult = validator.validate(dto);

        Map<Integer, String> mapErrors = validationResult.getErrors().stream()
                .collect(Collectors
                        .toMap(Error::getCode, Error::getMessage));
        assertAll(
                () -> assertTrue(validationResult.hasErrors()),
                () -> assertThat(mapErrors).containsKey(102),
                () -> assertThat(mapErrors).containsValue("provider is invalid")
        );
    }

    @Test
    void shouldFailValidationIfInvalidExpirationDate() {
        var dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(Instant.now())
                .build();

        var validationResult = validator.validate(dto);

        Map<Integer, String> mapErrors = validationResult.getErrors().stream()
                .collect(Collectors
                        .toMap(Error::getCode, Error::getMessage));
        assertAll(
                () -> assertThat(mapErrors).containsKey(103),
                () -> assertThat(mapErrors).containsValue("expirationDate is invalid")
        );
    }

    @Test
    void shouldInvalidIfInvalidExpirationDateUserIdProviderName() {
        var dto = CreateSubscriptionDto.builder()
                .userId(null)
                .name("")
                .provider("some_provider")
                .expirationDate(Instant.now())
                .build();

        var validationResult = validator.validate(dto);

        Map<Integer, String> mapErrors = validationResult.getErrors().stream()
                .collect(Collectors
                        .toMap(Error::getCode, Error::getMessage));
        assertAll(
                () -> assertThat(mapErrors).containsKeys(100, 101, 102, 103),
                () -> assertThat(mapErrors).containsValues("userId is invalid", "name is invalid", "provider is invalid", "expirationDate is invalid")
        );
    }
}