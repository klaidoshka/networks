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
class ParallelTransactionsHandler<K>(
    private val sessionFactory: () -> Session,
    private val transactions: Map<K, (Transaction) -> Any>
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
                            TransactionResult.Success(block(transaction))
                        } catch (e: Throwable) {
                            TransactionResult.Failure(e)
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
                it is TransactionResult.Failure
            }

            storage.values.forEach { (close, _) ->
                close(success)
            }

            return@withContext if (success) {
                TransactionResult.Success(
                    result = storage.mapValues { (_, value) ->
                        (value.second as TransactionResult.Success<*>).result
                    }
                )
            } else {
                TransactionResult.Failure(
                    error = results
                        .filterIsInstance<TransactionResult.Failure>()
                        .map { it.error }
                        .reduce { acc, e -> acc.apply { addSuppressed(e) } }
                )
            }
        }
    }

    sealed class TransactionResult {
        data class Success<R>(val result: R) : TransactionResult()
        data class Failure(val error: Throwable) : TransactionResult()
    }
}