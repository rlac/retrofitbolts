package com.github.rlac.retrofitbolts;

import java.lang.reflect.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an unsuccessful response.
 */
public class HttpException extends Exception {
    private final int code;
    private final Object errorBody;
    private final Type errorType;

    HttpException(int code, @Nullable String message, @Nullable Object errorBody, @Nonnull Type errorType) {
        super(message);
        this.code = code;
        this.errorBody = errorBody;
        this.errorType = errorType;
    }

    /**
     * @return the HTTP response code received.
     */
    public int getCode() {
        return code;
    }

    /**
     * @return the error body converted to the expected response type, if one was available.
     */
    @Nullable
    public Object getErrorBody() {
        return errorBody;
    }

    /**
     * @return the type of the error body.
     */
    @Nonnull
    public Type getErrorType() {
        return errorType;
    }
}
