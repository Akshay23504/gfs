package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.UUID;

public class Master extends Controller {

    private final WSClient wsClient;
    private final ObjectMapper mapper;
    private ObjectNode metadata = Json.newObject();
    private ArrayNode arrayNode;

    @Inject
    public Master(ObjectMapper objectMapper, WSClient wsClient, ObjectMapper mapper) {
        this.wsClient = wsClient;
        this.mapper = objectMapper;
        arrayNode = mapper.createArrayNode();
        metadata.set("chunkServers", arrayNode);
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
        ArrayNode temp = mapper.createArrayNode();
        ObjectNode chunkServer = Json.newObject();
        chunkServer.put("address", ip + ":" + port);
        temp.add(chunkServer);

        arrayNode.add(temp);
        return ok(metadata);
    }

    public Result triggerPolling() {
        ArrayNode arrayNode = (ArrayNode) metadata.get("chunkServers");
        arrayNode.forEach(x -> {
            System.out.println(x.get(0).get("address").asText());
            WSRequest request = wsClient.url("http://" + x.get(0).get("address").asText() + "/chunkServer/poll");
            request.get().thenApply(y -> {
                ArrayNode s = (ArrayNode) x;
                System.out.println(y.asJson());
                s.add(y.asJson());
                return y.asJson();
            });
        });
        return ok(metadata);
    }
}

