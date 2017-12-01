package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
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

/**
 * ChunkServer controller to handle polling, initialize chunks, read and write
 * chunks
 */
public class ChunkServer extends Controller {

    private final services.ChunkServer chunkServer;
    private final WSClient wsClient;

    /**
     * Constructor to initialize chunkServer services and wsClient
     * @param chunkServer ChunkServer services
     * @param wsClient WSClient to make async requests
     */
    @Inject
    public ChunkServer(services.ChunkServer chunkServer, WSClient wsClient) {
        this.chunkServer = chunkServer;
        this.wsClient = wsClient;
    }

    /**
     * This API reads the directories of the chunks (we are dealing with a
     * single level of directory in our GFS, but the logic can be extended to
     * handle any level of directories) and for each file, we create JSON.
     * This JSON consists "chunks" as the key and an array of chunks for a
     * particular chunkServer
     * @return JSON of chunks
     */
    public Result poll() {
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

    /**
     * This API takes an IP (localhost in our case) and a port and stops the
     * chunkServer running on the IP and the port. It also makes an async
     * request using the wsClient to the master indicating it is stopped.
     * The master can update the metadata and the chunkServerList for this
     * chunkServer.
     *
     * We choose to do this way, instead of the master trying to contact the
     * chunkServer because this was more easy and the master and is an extra
     * task which is not specified in the requirements.
     *
     * This API can be triggered by the Stop button on the master dashboard
     * for each chunkServer
     *
     * This also stops the chunkServer and hence the return statement will not
     * be reached
     * @param ip IP of the chunkServer to stop
     * @param port port number of the chunkServer to stop
     * @return 200, but this will not be reached
     */
    public Result stop(String ip, String port) {
        wsClient.url("http://localhost:9000/master/chunkServerDead?ip=" + ip + "&port=" + port).get();
        System.exit(1);
        return ok(); // Will not be executed
    }

    /**
     * This API gets an uuid as a parameter from the client and initializes a
     * chunk for the uuid
     * @param uuid uuid from the client
     * @return ok if everything goes well,
     *         A serverError if there is an IOException
     */
    public Result initializeChunk(String uuid) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(chunkServer.getChunksPath() + uuid));
            writer.write(123); // This shouldn't matter, because the first time we write, we write from position 0
            writer.close();
        } catch (IOException e) {
            Logger.error(e.getMessage());
            return internalServerError();
        }
        return ok();
    }

    /**
     * This API deals with writing the chunks to the appropriate files.
     * This is a POST request and takes in a multipartFormData from the client.
     * We get the filePartResponse and the byte content from the request and
     * write the bytes to the appropriate chunk directory and files.
     * @return ok (200) or serverError (500)
     */
    public Result writeChunk() {
        Http.MultipartFormData.FilePart<Object> filePartResponse =
                request().body().asMultipartFormData().getFile("content");
        try {
            byte[] content = Files.readAllBytes(Paths.get(((File) filePartResponse.getFile()).getPath()));
            Logger.info(Arrays.toString(content));
            BufferedWriter writer =
                    new BufferedWriter(new FileWriter(chunkServer.getChunksPath() + filePartResponse.getFilename()));
            writer.write(Arrays.toString(content));
            writer.close();
        } catch (IOException e) {
            Logger.error(e.getMessage());
            return internalServerError();
        }
        return ok();
    }

    /**
     * This API deals with reading the chunks using the uuid sent from the
     * client. We get an array of bytes from the chunks and the uuid
     * combination which we would have earlier stored using the write API.
     * This array of bytes is then transferred as a octet-stream to the client.
     * @param uuid uuid to read from
     * @return byte array as an octet-stream
     */
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
