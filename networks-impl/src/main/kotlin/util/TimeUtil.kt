package util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Utilities for time operations.
 */
object TimeUtil {

    /**
     * Fills an [Instant] with a random date up to now.
     */
    fun Instant.increaseRandomlyUpToNow(): Instant {
        return increaseRandomlyUpTo(Instant.now())
    }

    /**
     * Fills an [Instant] with a random date up to a given date.
     */
    fun Instant.increaseRandomlyUpTo(date: Instant): Instant {
        return Instant.ofEpochMilli(
            (toEpochMilli()..date.toEpochMilli()).randomOrNull() ?: date.toEpochMilli()
        )
    }

    /**
     * Converts an [Instant] to a [LocalDate].
     */
    fun Instant.toLocalDate(): LocalDate {
        return atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    /**
     * Converts a [String] to an [Instant].
     */
    fun String.toInstant(): Instant {
        listOf(
            { input: String ->
                LocalDate
                    .parse(
                        input,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    )
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
            },
            { input: String ->
                LocalDate
                    .parse(
                        input,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    )
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
            },
            { input: String -> Instant.parse(input) }
        )
            .forEach { parser ->
                try {
                    return parser(this)
                } catch (e: Exception) {
                    // Ignore
                }
            }

        throw IllegalArgumentException("Could not parse date: $this")
    }
}