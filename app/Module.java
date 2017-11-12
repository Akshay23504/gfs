import com.google.inject.AbstractModule;

import play.Environment;
import com.typesafe.config.Config;

public class Module extends AbstractModule {

    protected void configure() {
        bind(ApplicationStart.class).asEagerSingleton();
    }
}