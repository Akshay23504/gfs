package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.ChunkMetadata;
import models.ChunkServer;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Master extends Controller {

    private final WSClient wsClient;
    private final ObjectMapper mapper;
    private ArrayNode arrayNode;
    private List<ChunkServer> chunkServerList;

    @Inject
    public Master(ObjectMapper objectMapper, WSClient wsClient, ObjectMapper mapper) {
        this.wsClient = wsClient;
        this.mapper = objectMapper;
        arrayNode = mapper.createArrayNode();
        chunkServerList = new ArrayList<>();
    }

    public Result chunkHandle(String filename, String chunkIndex) {
        return ok(new java.io.File("/build.sbt"));
    }

    public Result createFile(String filename) {
        System.out.println(System.getProperty("http.port"));

        //TODO: create first initial empty chunk
        String uniqueID = UUID.randomUUID().toString();
        return (ok("File to create: " + filename));
    }

    public Result registerChunkServer(String ip, String port) {
        chunkServerList.add(new ChunkServer(ip, port));
        return ok();
    }

    public Result getChunkServers() {
        ObjectNode metadata = Json.newObject();
        arrayNode = metadata.putArray("chunkServers");
        arrayNode.add(mapper.valueToTree(chunkServerList));
        return ok(metadata);
    }

    public Result triggerPolling() {
        chunkServerList.forEach(chunkServer -> {
            WSRequest request = wsClient.url("http://" + chunkServer.getAddress() + "/chunkServer/poll");
            request.get().thenApply(x -> {
                x.asJson().get("chunks")
                        .forEach(id -> chunkServer.addChunkServerMetadata(new ChunkMetadata(id.asText())));
                return x.asJson();
            });
        });
        return ok();
    }
}

