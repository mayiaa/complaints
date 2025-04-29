package com.maja.complaints.config;

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@TestConfiguration
class TestClockConfig {

    private static final Instant FIXED_INSTANT = Instant.parse("2023-07-15T10:15:30Z")
    private static final ZoneId ZONE_ID = ZoneId.of("UTC")

    @Bean
    @Primary
    Clock fixedClock() {
        return Clock.fixed(FIXED_INSTANT, ZONE_ID)
    }
}
