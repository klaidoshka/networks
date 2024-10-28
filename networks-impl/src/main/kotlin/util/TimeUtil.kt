package util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Utilities for time operations.
 */
object TimeUtil {

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
        return Instant.parse(this)
    }
}