package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.ChunkMetadata;
import models.GFSFile;
import play.Environment;
import play.libs.Json;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GFSFileSystem {

    public static final Integer chunkSizeBytes = 64;
    private static String gfsFile;
    private final Environment environment;
    private static List<GFSFile> GFSFiles = new ArrayList<>();

    @Inject
    public GFSFileSystem(Environment environment) {
        this.environment = environment;
        if (this.environment.isDev()) {
            gfsFile = "conf/gfs.json";
        } else {
            gfsFile = "../conf/gfs.json";
        }
    }

    public static void addFile(GFSFile file) throws IOException {
        GFSFiles.add(file);
        ObjectNode files = Json.newObject();
        ArrayNode arrayNode = files.putArray("files");
        arrayNode.add(new ObjectMapper().valueToTree(GFSFiles));
        BufferedWriter writer = new BufferedWriter(new FileWriter(gfsFile));
        writer.write(files.toString());
        writer.close();
    }

    public static List<GFSFile> getFiles() {
        List<GFSFile> gfsFiles = new ArrayList<>();
        JsonNode jsonNode;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(gfsFile));
            jsonNode = Json.parse(reader.readLine());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return gfsFiles;
        }

        GFSFiles = new ArrayList<>();
        System.out.println(jsonNode.toString());
        jsonNode.get("files").get(0).forEach(x -> {
            System.out.println(x.toString());
            GFSFile gfsFile = new GFSFile(x.get("name").asText());
            x.get("chunkMetadataList").forEach(e -> {
                gfsFile.addChunkMetadata(new ChunkMetadata(e.get("id").asText()));
            });
            GFSFiles.add(gfsFile);

        });
        System.out.println(GFSFiles);
        return GFSFiles;
    }



}
