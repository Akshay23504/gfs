package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Environment;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.File;

public class ChunkServer extends Controller {

    private final Environment environment;

    @Inject
    public ChunkServer(Environment environment) {
        this.environment = environment;
    }

    public Result poll() {
        //TODO: read in all chunks
        //TODO: create list of the unique ids of all chunks
        //TODO: return the list of the chunk unique ids
        //TODO: Maybe create a model when we have time

        File folder = new File(environment.rootPath() + "/chunks");
        File[] listOfFiles = folder.listFiles();
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode result = Json.newObject();

        for (int i = 0; i < (listOfFiles != null ? listOfFiles.length : 0); i++) {
            if (listOfFiles[i].isFile()) {
                arrayNode.add(listOfFiles[i].getName());
                // TODO Remove print statement
                System.out.println("File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        result.set("chunks", arrayNode);
        return ok(result);
    }

    public Result stop(String ip, String port) {
        if (System.getProperty("http.port").equals(port)) {
            return forbidden(); // Cannot stop chunkserver from the same chunkserver!
        }
        return redirect("localhost:9000/chunkServerDead?ip=" + ip + "&port=" + port);
    }
}
