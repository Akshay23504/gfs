import play.Environment;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * This is not used anymore.
 * This was written initially so that the chunkServer can register
 * automatically with the master when it starts up.
 * We have a different and a better approach now.
 *
 * This creates an `ApplicationStart` object once at start-up.
 */
@Singleton
public class ApplicationStart {

    // Inject the application's Environment upon start-up and register hook(s) for shut-down.
    @Inject
    public ApplicationStart(ApplicationLifecycle lifecycle, Environment environment, WSClient wsClient) throws ExecutionException, InterruptedException {
        Logger.info("Registering with master");
        lifecycle.addStopHook( () -> CompletableFuture.completedFuture(null));

        WSRequest request = wsClient.url("http://localhost:9000/master/createFile?filename=abc");
        CompletionStage<WSResponse> responsePromise = request.get();
    }
}