package com.github.rlac.retrofitbolts;

import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import bolts.Task;

/**
 * Error responses are represented as an error using {@link HttpException}.
 *
 * @param <T> Task result type.
 */
public interface BoltsCall<T> {
    /**
     * Request the ongoing call be cancelled.
     */
    void cancel();

    /**
     * Executes the call in the calling thread, returning a Task containing the result.
     *
     * Error responses are represented as an error using {@link HttpException}.
     *
     * @return a Task containing the result of the call.
     */
    @NonNull
    Task<T> immediate();

    /**
     * Executes the call in a background thread, returning a Task that will contain the result.
     *
     * @return a Task that will contain the result of the call.
     */
    @NonNull
    Task<T> background();

    /**
     * Executes the call on the provided Executor, returning a Task that will contain the result.
     *
     * @param executor the Executor that will execute the call.
     * @return a Task that will contain the result of the call.
     */
    @NonNull
    Task<T> execute(@NonNull Executor executor);
}
