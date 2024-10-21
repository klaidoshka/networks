package handler

import kotlinx.coroutines.*
import org.neo4j.driver.Session
import org.neo4j.driver.Transaction

/**
 * A handler for executing multiple transactions in parallel.
 *
 * @param sessionFactory a factory function for creating a new session.
 * @param transactions a map where key is the identifier of the transaction and value is a block of
 * code to execute.
 */
class ParallelTransactionsHandler<K, R>(
    private val sessionFactory: () -> Session,
    private val transactions: Map<K, (Transaction) -> R>
) {

    /**
     * Storage for transactions.
     *
     * The key is the identifier of the transaction and the value is a pair of a function to close the transaction
     * and the result of the transaction.
     */
    private val storage = mutableMapOf<K, Pair<(Boolean) -> Unit, TransactionResult>>()

    /**
     * Executes all transactions in parallel. If any of the transactions fail, all transactions
     * are rolled back.
     *
     * In case of **success**, the holding result is of type [Map] with the id of the transaction as the key
     * and the result of the transaction as the value.
     *
     * @return [TransactionResult.Success] if all transactions were successful, [TransactionResult.Failure]
     * otherwise.
     */
    suspend fun handle(): TransactionResult = coroutineScope {
        withContext(Dispatchers.IO) {
            val results = transactions
                .map { (name, block) ->
                    async(Dispatchers.IO) {
                        val session = sessionFactory()
                        val transaction = session.beginTransaction()

                        val result = try {
                            TransactionResultInternal.Success(block(transaction))
                        } catch (e: Throwable) {
                            TransactionResultInternal.Failure(e)
                        }

                        storage[name] = { success: Boolean ->
                            if (success) {
                                transaction.commit()
                            }

                            transaction.close()

                            session.close()
                        } to result

                        result
                    }
                }
                .awaitAll()

            val success = results.none {
                it is TransactionResultInternal.Failure
            }

            storage.values.forEach { (close, _) ->
                close(success)
            }

            return@withContext if (success) {
                TransactionResult.Success(
                    result = storage.mapValues { (_, value) ->
                        (value.second as TransactionResultInternal.Success<*>).result
                    }
                )
            } else {
                TransactionResult.Failure(
                    error = results
                        .filterIsInstance<TransactionResultInternal.Failure>()
                        .map { it.error }
                        .reduce { acc, e -> acc.apply { addSuppressed(e) } }
                )
            }
        }
    }

    /**
     * Internal transaction result.
     */
    private sealed class TransactionResultInternal {

        /**
         * In case of **success**, the holding result is of type [R] which is the result of the transaction.
         */
        data class Success<R>(val result: R) : TransactionResult()

        /**
         * In case of **failure**, the holding error is of type [Throwable] which is the final error
         * that occurred during the transaction execution.
         */
        data class Failure(val error: Throwable) : TransactionResult()
    }

    /**
     * Exposed transaction result.
     *
     * @see TransactionResult.Success
     * @see TransactionResult.Failure
     */
    sealed class TransactionResult {

        /**
         * In case of **success**, the holding result is of type [Map] with the id of the transaction
         * as the key and the result of the transaction as the value.
         */
        data class Success<K, R>(val result: Map<K, R>) : TransactionResult()

        /**
         * In case of **failure**, the holding error is of type [Throwable] which is the first error
         * that occurred during the transaction execution. Other errors are suppressed.
         */
        data class Failure(val error: Throwable) : TransactionResult()
    }
}