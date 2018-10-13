package com.github.rlac.retrofitbolts;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import bolts.Task;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;

import static com.google.common.truth.Truth.assertThat;

public class BoltsCallAdapterFactoryTest {

    private MockWebServer server;
    private Retrofit retrofit;
    private TestService service;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(BoltsCallAdapterFactory.create())
                .baseUrl(server.url("/"))
                .build();
        service = retrofit.create(TestService.class);
    }

    @After
    public void tearDown() throws IOException {
        server.close();
        server = null;
        retrofit = null;
        service = null;
    }

    @Test
    public void testImmediateSuccess() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("success"));
        Task<String> immediateSuccess = service.test().immediate();
        assertThat(immediateSuccess.isFaulted()).isFalse();
        assertThat(immediateSuccess.isCancelled()).isFalse();
        assertThat(immediateSuccess.isCompleted()).isTrue();
        assertThat(immediateSuccess.getResult()).isEqualTo("success");
        assertThat(immediateSuccess.getError()).isNull();
    }

    @Test
    public void testImmediateError() {
        server.enqueue(new MockResponse().setResponseCode(400).setBody("error"));
        Task<String> immediateError = service.test().immediate();
        assertThat(immediateError.isFaulted()).isTrue();
        assertThat(immediateError.isCancelled()).isFalse();
        assertThat(immediateError.isCompleted()).isTrue();
        assertThat(immediateError.getResult()).isNull();
        assertThat(immediateError.getError()).isInstanceOf(HttpException.class);
        assertThat(((HttpException) immediateError.getError()).getErrorBody()).isEqualTo("error");
        assertThat(((HttpException) immediateError.getError()).getCode()).isEqualTo(400);
    }

    @Test
    public void testBackgroundSuccess() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("success"));
        Task<String> backgroundSuccess = service.test().background();
        backgroundSuccess.waitForCompletion();
        assertThat(backgroundSuccess.isFaulted()).isFalse();
        assertThat(backgroundSuccess.isCancelled()).isFalse();
        assertThat(backgroundSuccess.isCompleted()).isTrue();
        assertThat(backgroundSuccess.getResult()).isEqualTo("success");
        assertThat(backgroundSuccess.getError()).isNull();
    }

    @Test
    public void testBackgroundError() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(400).setBody("error"));
        Task<String> backgroundError = service.test().background();
        backgroundError.waitForCompletion();
        assertThat(backgroundError.isFaulted()).isTrue();
        assertThat(backgroundError.isCancelled()).isFalse();
        assertThat(backgroundError.isCompleted()).isTrue();
        assertThat(backgroundError.getResult()).isNull();
        assertThat(backgroundError.getError()).isInstanceOf(HttpException.class);
        assertThat(((HttpException) backgroundError.getError()).getErrorBody()).isEqualTo("error");
        assertThat(((HttpException) backgroundError.getError()).getCode()).isEqualTo(400);
    }

    @Test
    public void testOnExecutorSuccess() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("success"));
        Task<String> onExecutorSuccess = service.test().execute(Task.BACKGROUND_EXECUTOR);
        onExecutorSuccess.waitForCompletion();
        assertThat(onExecutorSuccess.isFaulted()).isFalse();
        assertThat(onExecutorSuccess.isCancelled()).isFalse();
        assertThat(onExecutorSuccess.isCompleted()).isTrue();
        assertThat(onExecutorSuccess.getResult()).isEqualTo("success");
        assertThat(onExecutorSuccess.getError()).isNull();
    }

    @Test
    public void testOnExecutorError() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(400).setBody("error"));
        Task<String> onExecutorError = service.test().execute(Task.BACKGROUND_EXECUTOR);
        onExecutorError.waitForCompletion();
        assertThat(onExecutorError.isFaulted()).isTrue();
        assertThat(onExecutorError.isCancelled()).isFalse();
        assertThat(onExecutorError.isCompleted()).isTrue();
        assertThat(onExecutorError.getResult()).isNull();
        assertThat(onExecutorError.getError()).isInstanceOf(HttpException.class);
        assertThat(((HttpException) onExecutorError.getError()).getErrorBody()).isEqualTo("error");
        assertThat(((HttpException) onExecutorError.getError()).getCode()).isEqualTo(400);
    }

    @Test
    public void testCancellation() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("cancel"));
        final AtomicReference<Runnable> delayedRunnable = new AtomicReference<>();
        BoltsCall<String> cancelCall = service.test();
        Task<String> cancelTask = cancelCall.execute(delayedRunnable::set);
        cancelCall.cancel();
        delayedRunnable.get().run();
        assertThat(cancelTask.isFaulted()).isFalse();
        assertThat(cancelTask.isCancelled()).isTrue();
        assertThat(cancelTask.isCompleted()).isTrue();
        assertThat(cancelTask.getResult()).isNull();
        assertThat(cancelTask.getError()).isNull();
    }

    interface TestService {
        @GET("/test")
        BoltsCall<String> test();
    }

}
