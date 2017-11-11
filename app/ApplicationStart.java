import javax.inject.*;
import play.inject.ApplicationLifecycle;
import play.Environment;
import java.util.concurrent.CompletableFuture;

// This creates an `ApplicationStart` object once at start-up.
@Singleton
public class ApplicationStart {

    // Inject the application's Environment upon start-up and register hook(s) for shut-down.
    @Inject
    public ApplicationStart(ApplicationLifecycle lifecycle, Environment environment) {
        System.out.println("Registering with master");
        lifecycle.addStopHook( () -> CompletableFuture.completedFuture(null));
    }
}