package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class ChunkServer extends Controller {

    private final services.ChunkServer chunkServer;

    @Inject
    public ChunkServer(services.ChunkServer chunkServer) {
        this.chunkServer = chunkServer;
    }

    public Result poll() {
        //TODO: read in all chunks
        //TODO: create list of the unique ids of all chunks
        //TODO: return the list of the chunk unique ids
        //TODO: Maybe create a model when we have time

        File folder = new File(chunkServer.getChunksPath());
        File[] listOfFiles = folder.listFiles();
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode result = Json.newObject();

        for (int i = 0; i < (listOfFiles != null ? listOfFiles.length : 0); i++) {
            if (listOfFiles[i].isFile()) {
                arrayNode.add(listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                Logger.info("Directory " + listOfFiles[i].getName());
            }
        }
        result.set("chunks", arrayNode);
        return ok(result);
    }

    public Result stop(String ip, String port) {
        if (true) return ok();
        if (System.getProperty("http.port").equals(port)) {
            return forbidden(); // Cannot stop chunkServer from the same chunkServer!
        }
        return redirect("http://localhost:9000/chunkServerDead?ip=" + ip + "&port=" + port);
    }

    public Result writeChunk() {
        Http.MultipartFormData.FilePart<Object> filePartResponse = request().body().asMultipartFormData().getFile("content");
        try {
            byte[] content = Files.readAllBytes(Paths.get(((File) filePartResponse.getFile()).getPath()));
            Logger.info(Arrays.toString(content));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO Do we need DataOutputStream?
        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(chunkServer.getChunksPath() + filePartResponse.getFilename())
            );
            writer.write(123);
            writer.close();
        } catch (IOException e) {
            Logger.error(e.getMessage());
            return internalServerError();
        }
        return ok();
    }

    public Result readChunk(String uuid) {
        byte[] data;
        try {
            data = Files.readAllBytes(Paths.get(chunkServer.getChunksPath() + uuid));
        } catch (IOException e) {
            Logger.error(e.getMessage());
            return internalServerError();
        }
        Logger.info(Arrays.toString(data));
        return ok(data).as("application/octet-stream");
    }
}
