package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.api.Play;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.File;

import javax.inject.*;
import play.libs.ws.*;
import play.Environment;

public class ChunkServer extends Controller {

    @Inject Environment environment;

    public Result poll() {
        //TODO: read in all chunks
        //TODO: create list of the unique ids of all chunks
        //TODO: return the list of the chunk unique ids

        File folder = new File(environment.rootPath() + "/chunks");
        File[] listOfFiles = folder.listFiles();
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode result = Json.newObject();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                arrayNode.add(listOfFiles[i].getName());
                System.out.println("File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        result.set("chunks", arrayNode);
        System.out.println("Came here....");

        return ok(result);
    }
}
