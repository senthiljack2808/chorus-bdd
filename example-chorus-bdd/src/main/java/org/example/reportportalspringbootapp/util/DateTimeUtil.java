package org.example.reportportalspringbootapp.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for generating default timestamp values in various formats.
 * Used by DTO request classes to provide sensible defaults.
 */
public final class DateTimeUtil {

    /**
     * ISO 8601 formatter with milliseconds and 'Z' suffix for UTC.
     * Format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     */
    private static final DateTimeFormatter ISO_8601_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);

    private DateTimeUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Returns the current UTC time formatted as ISO 8601 with milliseconds.
     * Format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     * Example: "2026-01-18T10:38:29.679Z"
     *
     * @return Current UTC time as ISO 8601 string
     */
    public static String getCurrentIso8601Timestamp() {
        return ISO_8601_FORMATTER.format(Instant.now());
    }

    /**
     * Returns the current Unix epoch time in milliseconds as a String.
     * Example: "1737196709679"
     *
     * @return Current Unix epoch time in milliseconds as String
     */
    public static String getCurrentUnixTimestampMillis() {
        return String.valueOf(Instant.now().toEpochMilli());
    }

    /**
     * Converts IST (Indian Standard Time) to UTC and formats as ISO 8601.
     * Note: IST is UTC+5:30, so this subtracts 5 hours 30 minutes from IST.
     *
     * @param istInstant The instant representing IST time
     * @return UTC time formatted as ISO 8601 string
     */
    public static String convertIstToUtcIso8601(Instant istInstant) {
        return ISO_8601_FORMATTER.format(istInstant);
    }
}

