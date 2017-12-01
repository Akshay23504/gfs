package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.ChunkMetadata;
import models.ChunkServer;
import models.GFSFile;
import play.Environment;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import services.GFSFileSystem;
import views.html.dashboard;

import javax.inject.Inject;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Master controller to handle chunks, create files, respond to clients,
 * choose chunkServers to read and write chunks, register new chunkServers,
 * polling on the chunkServers and few other features.
 */
public class Master extends Controller {

    private final WSClient wsClient;
    private final ObjectMapper mapper;
    private ArrayNode arrayNode;
    private List<ChunkServer> chunkServerList;
    private String gfsPorts;
    private String newChunkServerScript;
    private final Environment environment;
    GFSFileSystem gfsFileSystem;

    /**
     * Constructor to initialize few things
     * Some things are dependent on the environment we are running the
     * application in.
     * @param objectMapper objectMapper for JSON parsing and construction
     * @param wsClient wsClient to make async requests
     * @param environment Environment the application is running in
     *                    (Dev, Test or Prod)
     */
    @Inject
    public Master(ObjectMapper objectMapper, WSClient wsClient, Environment environment) {
        this.wsClient = wsClient;
        this.mapper = objectMapper;
        arrayNode = objectMapper.createArrayNode();
        chunkServerList = new ArrayList<>();
        this.environment = environment;
        if (this.environment.isDev()) {
            gfsPorts = "conf/gfsPorts.json";
            newChunkServerScript = "activator run -Dhttp.port=";
        } else {
            gfsPorts = "../conf/gfsPorts.json";
            newChunkServerScript = "sh ../conf/chunkServer.sh "; // Mind the space after .sh
        }
        gfsFileSystem = new GFSFileSystem(environment);
    }

    // TODO we don't need this now
    public Result chunkHandle(String filename, String chunkIndex) {
        return ok(new java.io.File("/build.sbt"));
    }

    /**
     * This API creates a file by taking a request from the client and sends
     * a request to one of the chunkServers to initialize the chunks and
     * finally triggers a polling and updates the metadata. More like this will
     * redirect to the polling API of the master.
     * @param filename filename to create
     * @param size Size of the file
     * @return redirect to triggerPolling
     */
    public Result createFile(String filename, String size) {
        Integer chunkNumber = (int) Math.ceil(Double.valueOf(size) / GFSFileSystem.chunkSizeBytes);
        int counter = 0;
        GFSFile gfsFile = new GFSFile(filename);
        while(counter++ < chunkNumber) {
            gfsFile.addChunkMetadata(new ChunkMetadata(UUID.randomUUID().toString()));
        }
        try {
            GFSFileSystem.addFile(gfsFile);
        } catch (IOException e) {
            Logger.error("File creation failed :( ");
            Logger.error(e.getMessage());
            return internalServerError(); // Should not happen
        }

        // For each of the metadata list that we have, choose a chunkServer
        // and make a request to initialize chunks on the chosen chunkServer
        gfsFile.chunkMetadataList.forEach(x -> {
            x.setAddress(chooseChunkServerForChunk().getAddress());
            WSRequest request = wsClient
                    .url("http://" + x.getAddress() + "/chunkServer/initializeChunk?uuid=" + x.getId());
            request.get().thenApply(WSResponse::asJson);
        });
        return redirect("http://localhost:9000/master/triggerPolling");
    }

    /**
     * Choose a chunkServer from the chunkServerList that we have on the master
     * @return Chosen RUNNING chunkServer
     */
    private ChunkServer chooseChunkServerForChunk() {
        List<ChunkServer> runningChunkServers = chunkServerList
                .stream()
                .filter(x -> x.getStatus().equals(ChunkServer.ChunkServerStatus.RUNNING))
                .collect(Collectors.toList());
        return runningChunkServers.get(new Random().nextInt(runningChunkServers.size()));
    }

    /**
     * This API gets the chunk handles using the file name and returns a
     * response in the form of a JSON to the client.
     * @param filename Filename to get the chunk handles for
     * @return JSON string of the chunk handles and list of chunk servers,
     *          and other stuff required for the client to contact individual
     *          chunkServers.
     */
    public Result getChunkHandlesForFile(String filename) throws InterruptedException {
        ObjectNode handles = Json.newObject();
        wsClient.url("http://localhost:9000/master/triggerPolling").get();
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
        arrayNode = handles.putArray("chunkHandles");
        arrayNode.add(mapper.valueToTree(chunkHandles));
        return ok(handles);
    }

    /**
     * This API is not used by the client. This is here so that we can manually
     * register a chunkServer.
     * @param ip IP of the chunkServer to register
     * @param port Port number of the chunkServer to register
     * @return 200
     */
    public Result registerChunkServer(String ip, String port) {
        if (chunkServerList.stream().anyMatch(x -> (x.getIp().equals(ip) && x.getPort().equals(port)))) {
            // We already have a chunkServer with this IP and port
            return badRequest("ChunkServer with IP: " + ip + " and port: " + port + " already exists");
        }
        chunkServerList.add(new ChunkServer(ip, port));
        return ok();
    }

    /**
     * This API gets called when the user clicks on the new ChunkServer button
     * on the master dashboard. We start a new chunkServer from the next port
     * that is available. If this is the first chunkServer, then we start from
     * 9001. This is recorded in a JSON file to keep track of the chunkServers.
     * ChunkServer is started in the background and the chunkServerList is
     * updated to account for the new chunkServer
     * @return 200 if a new chunkServer is started
     *         400 if we try to select a port that already has a chunkServer
     *         running. This will not happen, unless we manually tamper with
     *         the ports file. Do not do this.
     * @throws IOException
     */
    public Result registerNewChunkServer() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(gfsPorts));
        String jsonString = bufferedReader.readLine();
        bufferedReader.close();
        int portNumber;
        ObjectNode objectNode = Json.newObject();
        ArrayNode portsArray = objectNode.putArray("ports");
        ArrayNode tempPortsArray = null;
        if (jsonString == null || jsonString.isEmpty()) {
            portNumber = 9001;
        } else {
            tempPortsArray = (ArrayNode) Json.parse(jsonString).get("ports");
            // This will be our new chunkServer's port
            portNumber = tempPortsArray.get(tempPortsArray.size() - 1).asInt() + 1;
        }
        if (tempPortsArray != null) {
            tempPortsArray.forEach(portsArray::add);
        }
        portsArray.add(portNumber);
        if(environment.isProd()) {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(gfsPorts));
            bufferedWriter.write(objectNode.toString());
            bufferedWriter.close();
        }
        if (chunkServerList
                .stream()
                .anyMatch(x -> (x.getIp().equals("localhost") && x.getPort().equals(String.valueOf(portNumber))))) {
            // We already have a chunkServer with this IP and port
            return badRequest("ChunkServer with IP: localhost and port: " + portNumber + " already exists");
        }
        Runtime.getRuntime().exec(newChunkServerScript + portNumber);
        chunkServerList.add(new ChunkServer("localhost", String.valueOf(portNumber)));
        return ok();
    }

    /**
     * This API is called by the master to get the information about the
     * chunkServers. This is done for each heartbeat message
     * @return JSON of chunkServers and its information
     */
    public Result getChunkServers() {
        ObjectNode metadata = Json.newObject();
        arrayNode = metadata.putArray("chunkServers");
        arrayNode.add(mapper.valueToTree(chunkServerList));
        return ok(metadata);
    }

    /**
     * This API is used to get information about a single chunkServer based on
     * the ip and port specified
     * @param ip IP of the chunkServer to get data
     * @param port Port number of the chunkServer to get data
     * @return Information about the chunkServer
     */
    public Result getChunkServer(String ip, String port) {
        return ok((JsonNode) mapper.valueToTree(chunkServerList
                .stream()
                .filter(x -> x.getIp().equals(ip) && x.getPort().equals(port))
                .distinct().collect(Collectors.toList()))
        );
    }

    /**
     * This API is called by the files section on the dashboard. This will
     * return the JSON of the files and the chunks
     * @return
     */
    public Result getFiles() {
        ObjectNode metadata = Json.newObject();
        arrayNode = metadata.putArray("files");
        arrayNode.add(mapper.valueToTree(gfsFileSystem.getFiles()));
        return ok(metadata);
    }

    /**
     * This API is called by the chunkServer to stop a chunkServer. The
     * chunkServer calls this so that the master can update information about
     * this chunkServer. This is beyond the requirements!
     * @param ip IP of the chunkServer that is being stopped or dying
     * @param port Port number of the chunkServer that is being stopped or
     *             dying
     * @return 200
     */
    public Result chunkServerDead(String ip, String port) {
        chunkServerList
                .stream()
                .filter(x -> x.getIp().equals(ip) && x.getPort().equals(port))
                .distinct() // TODO Ideally this should always be distinct and hence we remove the distinct clause
                .forEach(x -> x.setStatus(ChunkServer.ChunkServerStatus.DEAD));
        return ok();
    }

    /**
     * This API will update the chunks. Basically the metadata of the master
     * @return 200
     */
    public Result triggerPolling() {
        chunkServerList.forEach(chunkServer -> {
            WSRequest request = wsClient.url("http://" + chunkServer.getAddress() + "/chunkServer/poll");
            request.get().thenApply(x -> {
                List<ChunkMetadata> chunkMetadataList = new LinkedList<>();
                x.asJson().get("chunks")
                        .forEach(id -> chunkMetadataList.add(new ChunkMetadata(id.asText())));
                chunkServer.setChunkMetadataList(chunkMetadataList);
                return x.asJson();
            });
        });
        return ok();
    }

    /**
     * Render the beautiful GFS master dashboard
     * @return Dashboard html file
     */
    public Result dashboard() {
        return ok(dashboard.render());
    }
}

