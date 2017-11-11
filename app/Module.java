import com.google.inject.AbstractModule;

public class Module extends AbstractModule {
    protected void configure() {
        bind(ApplicationStart.class).asEagerSingleton();
    }
}