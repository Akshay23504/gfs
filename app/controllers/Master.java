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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public Result createFile(String filename) throws IOException {
        System.out.println(System.getProperty("http.port"));

        //TODO: create first initial empty chunk
        String uniqueID = UUID.randomUUID().toString();
        //System.out.println(Runtime.getRuntime().exec("ls"));
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

    public Result getChunkServer(String ip, String port) {
        return ok((JsonNode) mapper.valueToTree(chunkServerList
                .stream()
                .filter(x -> x.getIp().equals(ip) && x.getPort().equals(port))
                .distinct().collect(Collectors.toList()))
        );
    }

    public Result chunkServerDead(String ip, String port) {
        chunkServerList
                .stream()
                .filter(x -> x.getIp().equals(ip) && x.getPort().equals(port))
                .distinct() // TODO Ideally this should always be distinct and hence we remove the distinct clause
                .forEach(x -> x.setStatus(ChunkServer.ChunkServerStatus.DEAD));
        return ok();
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

