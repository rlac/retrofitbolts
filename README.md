Bolts Tasks Call Adapter for Retrofit 2
=======================================

A Retrofit 2 [CallAdapter.Factory](https://github.com/square/retrofit/wiki/Call-Adapters) for executing web service calls as [Bolts Tasks](https://github.com/BoltsFramework/Bolts-Android).

How to use
----------

(note: not yet available on a repository)

Add the BoltsCallAdapterFactory when constructing your Retrofit instance:

```java
new Retrofit.Builder()
    .addCallAdapterFactory(BoltsCallAdapterFactory.create())
    // further configuration
    .build();
```

Use BoltsCall\<T> as the return type for your service interface methods:

```java
interface Service {
    @GET("/fizzbuzz")
    BoltsCall<String> getFizzBuzz();
}
```

Use the returned BoltsCall to execute the call as a Task on an appropriate Executor:

```java
// call can be made on an immediate, background, or custom executor
// this example uses the background executor
Task<String> getTask = service.getFizzBuzz().background();
```

Unsuccessful responses throw a HttpException. HttpException includes the error code, deserialized error body if available, and the error body type.

License
-------

    Copyright (C) 2018 Robert Carr

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.