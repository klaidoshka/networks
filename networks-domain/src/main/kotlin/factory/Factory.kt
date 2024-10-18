package factory

fun interface Factory<R> {

    /**
     * Creates an object.
     *
     * @return an object
     */
    fun create(): R

    /**
     * Creates a list of objects.
     *
     * @param count the number of objects to create
     *
     * @return a list of objects
     */
    fun create(count: Int): List<R> {
        return (0 until count).map { create() }
    }
}