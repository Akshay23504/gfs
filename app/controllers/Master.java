package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.ChunkMetadata;
import models.ChunkServer;
import models.GFSFile;
import play.Logger;
import services.GFSFileSystem;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.dashboard;
import views.html.index;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

    // TODO we don't need this now
    public Result chunkHandle(String filename, String chunkIndex) {
        return ok(new java.io.File("/build.sbt"));
    }

    public Result createFile(String filename, String size) {
        Integer chunkNumber = (int) Math.ceil(Double.valueOf(size) / GFSFileSystem.chunkSizeBytes);
        //TODO: create first initial empty chunk
        int counter = 0;
        GFSFile gfsFile = new GFSFile(filename);
        while(counter++ < chunkNumber) {
            gfsFile.addChunkMetadata(new ChunkMetadata(UUID.randomUUID().toString()));
        }
        try {
            GFSFileSystem.addFile(gfsFile);
        } catch (IOException e) {
            Logger.error("File creation failed :( ");
            return internalServerError(); // Should not happen
        }



        gfsFile.chunkMetadataList.forEach(x -> {
            x.setAddress(chooseChunkServerForChunk().getAddress());
            WSRequest request = wsClient.url("http://" + x.getAddress() + "/chunkServer/initializeChunk?uuid=" + x.getId());
            request.get().thenApply(response -> {
                Logger.info(response.asJson().toString());
                return response.asJson();
            });
        });

        return redirect("http://localhost:9000/master/triggerPolling");
    }

    private ChunkServer chooseChunkServerForChunk() {
        return chunkServerList.get(new Random().nextInt(chunkServerList.size()));
    }

    public Result getChunkHandlesForFile(String filename) {
        List<ChunkMetadata> chunkHandles = new ArrayList<>();
        GFSFileSystem.getFiles()
                .stream()
                .filter(file -> file.getName().equals(filename))
                .forEach(file -> {
                    Logger.info("Locating ChunkHandles for " + filename);
                    file.chunkMetadataList
                            .forEach(fileChunk -> chunkServerList
                                    .forEach(server -> server.getChunkMetadataList()
                                            .stream()
                                            .filter(chunkServer -> chunkServer.getId().equals(fileChunk.getId()))
                                            .forEach(serverChunk -> {
                                                Logger.info(serverChunk.getId());
                                                Logger.info(fileChunk.getId());
                                                ChunkMetadata chunkHandle = new ChunkMetadata(fileChunk.getId());
                                                chunkHandle.setAddress(server.getAddress());
                                                chunkHandles.add(chunkHandle);
                                            })));
                });
        ObjectNode handles = Json.newObject();
        arrayNode = handles.putArray("chunkHandles");
        arrayNode.add(mapper.valueToTree(chunkHandles));
        return ok(handles);
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

    public Result dashboard() {
        return ok(dashboard.render());
    }
}

