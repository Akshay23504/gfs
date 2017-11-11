import javax.inject.*;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.ws.*;
import play.inject.ApplicationLifecycle;
import play.Environment;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


import akka.stream.Materializer;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import play.mvc.Results;

// This creates an `ApplicationStart` object once at start-up.
@Singleton
public class ApplicationStart implements WSBodyReadables, WSBodyWritables  {

    // Inject the application's Environment upon start-up and register hook(s) for shut-down.
    @Inject
    public ApplicationStart(ApplicationLifecycle lifecycle, Environment environment) {
        System.out.println("Registering with master");

        WSClient ws = play.libs.ws.ahc.AhcWSClient.create(
                play.libs.ws.ahc.AhcWSClientConfigFactory.forConfig(
                        configuration.underlying(),
                        environment.classLoader()),
                null, // no HTTP caching
                materializer);

        WSRequest request = ws.url("http://localhost:9000");

        WSRequest complexRequest = request.addHeader("headerKey", "headerValue")
                .setRequestTimeout(Duration.of(1000, ChronoUnit.MILLIS))
                .addQueryParameter("paramKey", "paramValue");

        CompletionStage<JsonNode> jsonPromise = ws.url(url).get()
                .thenApply(r -> r.getBody(json()))



        // Shut-down hook
        lifecycle.addStopHook( () -> {
            return CompletableFuture.completedFuture(null);
        } );
        // ...
    }
}