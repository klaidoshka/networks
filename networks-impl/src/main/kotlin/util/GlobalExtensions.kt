package util

import java.util.logging.Logger

/**
 * Returns a logger for the class.
 */
inline fun <reified T : Any> T.logger(): Logger = Logger.getLogger(T::class.simpleName)