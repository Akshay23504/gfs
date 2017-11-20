package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.ChunkMetadata;
import models.ChunkServer;
import models.GFSFile;
import models.GFSFileSystem;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import javax.sound.midi.SysexMessage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Master extends Controller {

    private final WSClient wsClient;
    private final ObjectMapper mapper;
    private ArrayNode arrayNode;
    private List<ChunkServer> chunkServerList;
    private GFSFileSystem gfsFileSystem;

    @Inject
    public Master(ObjectMapper objectMapper, WSClient wsClient, ObjectMapper mapper) {
        this.wsClient = wsClient;
        this.mapper = objectMapper;
        arrayNode = mapper.createArrayNode();
        chunkServerList = new ArrayList<>();
        gfsFileSystem = new GFSFileSystem();
    }

    public ChunkServer chooseChunkServerForChunk(ChunkMetadata chunk) {
        return chunkServerList.get(0);
    }

    public void saveGFS(GFSFileSystem fileSystem) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("gfs.ser");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            oos.writeObject(gfsFileSystem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GFSFileSystem getGFS() {
        FileInputStream fis = null;
        try {
            System.out.println("Retrieving GFSFileSystem from serialization");
            fis = new FileInputStream("gfs.ser");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            saveGFS(new GFSFileSystem());
            gfsFileSystem = getGFS();
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            gfsFileSystem = (GFSFileSystem) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return gfsFileSystem;
    }



    public Result chunkHandle(String filename, String chunkIndex) {
        return ok(new java.io.File("/build.sbt"));
    }

    public Result createFile(String filename, String size) {
        Integer chunksNumber = (int) Math.ceil(Double.valueOf(size) / GFSFileSystem.chunkSizeBytes);
        //TODO: create first initial empty chunk
        int counter = 0;
        GFSFile file = new GFSFile(filename);
        while(counter < chunksNumber) {
            String uuid = UUID.randomUUID().toString();
            file.addChunkMetadata(new ChunkMetadata(uuid));
            counter++;
        }
        gfsFileSystem.addFile(file);

        file.chunkMetadataList.forEach(x -> {
            x.setAddress(chooseChunkServerForChunk(x).getAddress());
            WSRequest request = wsClient.url("http://" + x.getAddress() + "/chunkServer/writeChunk?uuid=" + x.getId());
            request.get().thenApply(response -> {
                System.out.println(response.asJson().toString());
                return response.asJson();
            });
        });
        return ok();
    }

    public Result getChunkHandlesForFile(String filename) {
        List<ChunkMetadata> chunkHandles = new ArrayList<>();
        gfsFileSystem.getGFSfiles().forEach(file -> {
        if (file.getName().equals(filename)){
            System.out.println("Locating ChunkHandles for " + filename);
            file.chunkMetadataList.forEach(fileChunk -> {
                chunkServerList.forEach(server -> {
                    server.getChunkMetadataList().forEach(serverChunk -> {
                        System.out.println(serverChunk.getId());
                        System.out.println(fileChunk.getId());
                        if(serverChunk.getId().equals(fileChunk.getId())){
                            ChunkMetadata chunkHandle = new ChunkMetadata(fileChunk.getId());
                            chunkHandle.setAddress(server.getAddress());
                            chunkHandles.add(chunkHandle);
                        }
                    });

                });

            });

        }
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
}

