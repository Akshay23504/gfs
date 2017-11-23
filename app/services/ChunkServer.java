package services;

import play.Environment;

import javax.inject.Inject;

public class ChunkServer {

    private final Environment environment;

    @Inject
    public ChunkServer(Environment environment) {
        this.environment = environment;
    }

    public String getChunksPath() {
        return environment.rootPath() + "/chunks/chunks" +  System.getProperty("http.port") + "/";
    }
}
