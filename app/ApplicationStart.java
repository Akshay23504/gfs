import play.Environment;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

// This creates an `ApplicationStart` object once at start-up.
@Singleton
public class ApplicationStart {

    // Inject the application's Environment upon start-up and register hook(s) for shut-down.
    @Inject
    public ApplicationStart(ApplicationLifecycle lifecycle, Environment environment, WSClient wsClient) {
        System.out.println("Registering with master");
        lifecycle.addStopHook( () -> CompletableFuture.completedFuture(null));

        WSRequest request = wsClient.url("http://localhost:9000/master/registerChunkServer");
        CompletionStage<String> responsePromise = request.get().thenApply(WSResponse::getBody);
        try {
            System.out.print(responsePromise.toCompletableFuture().get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}