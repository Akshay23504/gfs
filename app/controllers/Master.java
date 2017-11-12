package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class Master extends Controller {

    @Inject WSClient wsClient;

    ObjectNode metadata = Json.newObject();
    @Inject ObjectMapper mapper;
    ArrayNode arrayNode = mapper.createArrayNode();

    public Master() {
        metadata.set("chunkservers", arrayNode);
    }

    public Result chunkHandle(String filename, String chunkIndex) {
        return ok(new java.io.File("/build.sbt"));
    }

    public Result createFile(String filename) {
        //TODO: create first initial empty chunk
        String uniqueID = UUID.randomUUID().toString();
        return (ok("File to create: " + filename));
    }

    public Result registerChunkServer(String ip, String port) {

        ObjectNode chunkserver = Json.newObject();
        chunkserver.put("ip", ip);
        chunkserver.put("port", port);

        arrayNode.add(chunkserver);

        return ok(metadata);
    }

    public Result triggerPolling() {
        for chunkserver in metadata["chunkservers"]:
        {
            WSRequest request = wsClient.url("http://localhost:9000/master/registerChunkServer");
            CompletionStage<String> responsePromise = request.get().thenApply(WSResponse::getBody);
            try {
                System.out.print(responsePromise.toCompletableFuture().get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return ok(metadata);
    }

