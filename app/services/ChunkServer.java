package services;

import play.Environment;
import play.Logger;

import javax.inject.Inject;
import java.io.File;

public class ChunkServer {

    private final Environment environment;

    @Inject
    public ChunkServer(Environment environment) {
        this.environment = environment;
    }

    public String getChunksPath() {
        String port = System.getProperty("http.port");
        File file = new File(environment.rootPath() + "/chunks");
        if (!file.exists()) {
            if (file.mkdir()) {
                Logger.info("Chunks folder created for server " + port);
                File chunkDirectory = new File(environment.rootPath() + "/chunks/chunks" + port);
                if (!chunkDirectory.exists()) {
                    chunkDirectory.mkdir();
                }
            } else {
                Logger.error("Chunks folder creation failed for server " + port);
            }
        }
        return environment.rootPath() + "/chunks/chunks" +  port + "/";
    }
}
