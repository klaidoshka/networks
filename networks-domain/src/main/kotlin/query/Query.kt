package query

import org.neo4j.driver.Transaction

/**
 * Represents a query to be executed and how to execute it.
 *
 * When a query is executed, it will be given a [Transaction] to work with.
 *
 * @param R the return type of the query.
 */
interface Query<R> : (Transaction) -> R {

    /**
     * Executes the query.
     *
     * @param transaction the transaction to execute the query in.
     *
     * @return the result of the query.
     */
    override fun invoke(transaction: Transaction): R
}

/**
 * Represents a query to be executed and how to execute it. It does not return anything.
 *
 * When a query is executed, it will be given a [Transaction] to work with.
 */
interface QueryNoReturn : Query<Unit>