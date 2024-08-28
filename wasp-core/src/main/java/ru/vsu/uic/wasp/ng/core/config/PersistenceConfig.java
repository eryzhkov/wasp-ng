package ru.vsu.uic.wasp.ng.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;

import java.time.OffsetDateTime;
import java.util.Optional;

@Configuration
public class PersistenceConfig {

    // The Spring JPA Audit doesn't understand OffsetDataTime class.
    // The only way to fix it is to provide the custom provider.
    @Bean(name = "auditingDateTimeProvider")
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
