package util

import handler.ParallelTransactionsHandler
import handler.ParallelTransactionsHandler.TransactionResult
import org.neo4j.driver.Driver
import org.neo4j.driver.SessionConfig
import org.neo4j.driver.Transaction

object DriverUtil {

    /**
     * Executes all transactions in parallel in IO coroutine context. If any of the transactions fail,
     * all transactions are rolled back.
     *
     * In case of **success**, the holding result is of type [Map] with the id of the transaction as
     * the key and the result of the transaction as the value.
     *
     * @param database The database to execute the transactions on.
     * @param transactions The transactions to execute.
     * @param K The type of the transaction id.
     * @param R The type of the transaction result.
     *
     * @return [TransactionResult.Success] if all transactions were successful, [TransactionResult.Failure]
     * otherwise.
     */
    suspend fun <K, R> Driver.runInParallel(
        database: String,
        transactions: Map<K, (Transaction) -> R>
    ): Map<K, R> {
        return ParallelTransactionsHandler(
            sessionFactory = { session(SessionConfig.forDatabase(database)) },
            transactions = transactions
        )
            .handle()
            .let {
                if (it is TransactionResult.Failure) {
                    throw RuntimeException("Failed to execute transactions: ${it.error.message}")
                } else {
                    @Suppress("UNCHECKED_CAST")
                    (it as TransactionResult.Success<K, R>).result
                }
            }
    }

    /**
     * Executes a transaction in a single session.
     *
     * @param database The database to execute the transaction on.
     * @param transaction The transaction to execute.
     * @param R The type of the transaction result.
     *
     * @return The result of the transaction.
     */
    fun <R> Driver.runSingle(
        database: String,
        transaction: (Transaction) -> R
    ): R {
        return session(SessionConfig.forDatabase(database))
            .use { session ->
                session
                    .beginTransaction()
                    .use { transaction(it) }
            }
    }
}