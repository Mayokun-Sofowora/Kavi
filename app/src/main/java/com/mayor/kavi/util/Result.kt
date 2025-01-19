package com.mayor.kavi.util

/**
 * Represents the outcome of an operation that can either succeed or fail.
 *
 * @param T The type of the result data in case of a successful operation.
 */
sealed class Result<out T> {

    /**
     * Represents a successful result.
     *
     * @param data The data of the successful result.
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Represents a failed result.
     *
     * @param message The error message describing the failure.
     * @param exception The optional exception that caused the failure.
     */
    data class Error<T>(
        val message: String = "An unknown error occurred",
        val exception: Throwable? = null,
        val data: T? = null
    ) : Result<T>()

    /**
     * Represents a loading state.
     *
     * @param data The optional data associated with the loading state.
     */
    data class Loading<out T>(val data: T? = null) : Result<T>()

}
