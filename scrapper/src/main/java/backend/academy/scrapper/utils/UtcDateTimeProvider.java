package backend.academy.scrapper.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Класс предназначен для создания и преобразования LocalDateTime с часовым поясом UTC
 */
public class UtcDateTimeProvider {

    private static final ZoneOffset UTC = ZoneOffset.UTC;

    private UtcDateTimeProvider() {}

    public static LocalDateTime now() {
        return LocalDateTime.now(UTC);
    }

    public static LocalDateTime of(long timestamp) {
        return Instant.ofEpochSecond(timestamp)
            .atZone(UTC)
            .toLocalDateTime();
    }

    public static long toUTCTimestamp(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(UTC);
    }
}
