package util

import java.util.logging.Logger

inline fun <reified T : Any> T.logger(): Logger = Logger.getLogger(T::class.simpleName)