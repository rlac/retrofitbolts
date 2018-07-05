package com.github.rlac.retrofitbolts;

import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents an unsuccessful response.
 */
public class HttpException extends Exception {
    private final int code;
    private final Object errorBody;
    private final Type errorType;

    HttpException(int code, @Nullable String message, @Nullable Object errorBody, @NonNull Type errorType) {
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
    @NonNull
    public Type getErrorType() {
        return errorType;
    }
}
