package com.github.rlac.retrofitbolts;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import bolts.Task;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Allows using {@link BoltsCall} return types on service interface methods.
 */
public class BoltsCallAdapterFactory extends CallAdapter.Factory {
    private static final Executor IMMEDIATE = Runnable::run;

    @NonNull
    public static BoltsCallAdapterFactory create() {
        return new BoltsCallAdapterFactory(Task.BACKGROUND_EXECUTOR, IMMEDIATE);
    }

    protected final Executor backgroundExecutor;
    protected final Executor immediateExecutor;

    private BoltsCallAdapterFactory(@NonNull Executor backgroundExecutor, @NonNull Executor immediateExecutor) {
        this.backgroundExecutor = backgroundExecutor;
        this.immediateExecutor = immediateExecutor;
    }

    @Nullable
    @Override
    public CallAdapter<?, ?> get(@NonNull Type returnType, @NonNull Annotation[] annotations, @NonNull Retrofit retrofit) {
        if (!getRawType(returnType).isAssignableFrom(BoltsCall.class)) {
            return null;
        }
        Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
        Converter<ResponseBody, ?> errorConverter = retrofit.responseBodyConverter(responseType, annotations);
        return new TaskCallAdapter<>(responseType, errorConverter);
    }

    class TaskCallAdapter<T> implements CallAdapter<T, BoltsCall<T>> {
        private final Type responseType;
        private final Converter<ResponseBody, T> errorConverter;

        TaskCallAdapter(@NonNull Type responseType, @NonNull Converter<ResponseBody, T> errorConverter) {
            this.responseType = responseType;
            this.errorConverter = errorConverter;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public BoltsCall<T> adapt(@NonNull final Call<T> call) {
            return new BoltsCall<T>() {
                @Override
                public void cancel() {
                    call.cancel();
                }

                @NonNull
                @Override
                public Task<T> immediate() {
                    return createTask(immediateExecutor);
                }

                @NonNull
                @Override
                public Task<T> background() {
                    return createTask(backgroundExecutor);
                }

                @NonNull
                @Override
                public Task<T> execute(@NonNull Executor executor) {
                    return createTask(executor);
                }

                @NonNull
                private Task<T> createTask(@NonNull Executor executor) {
                    return Task.call(() -> {
                        if (call.isCanceled()) {
                            throw new CancellationException();
                        }
                        Response<T> response = call.execute();
                        if (response.isSuccessful()) {
                            return response.body();
                        } else {
                            Object errorBody = null;
                            if (response.errorBody() != null) {
                                try {
                                    //noinspection ConstantConditions
                                    errorBody = errorConverter.convert(response.errorBody());
                                } catch (Exception ignore) {
                                    errorBody = null;
                                }
                            }
                            throw new HttpException(response.code(), response.message(), errorBody, responseType);
                        }
                    }, executor);
                }
            };
        }
    }
}
