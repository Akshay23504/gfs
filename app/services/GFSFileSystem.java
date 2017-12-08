package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.ChunkMetadata;
import models.GFSFile;
import play.Environment;
import play.Logger;
import play.libs.Json;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model class which has entities like chunk size, list of gfs files etc.
 */
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

    /**
     * Add the GFSFile to the json file for consistency and make it persistent.
     *
     * @param file GFSFile to add
     * @throws IOException, If we are unable to open or write to a file
     */
    public static void addFile(GFSFile file) throws IOException {
        GFSFiles = GFSFiles.stream().filter(x -> x.getName().equals(file.getName())).collect(Collectors.toList());
        GFSFiles.add(file);
        ObjectNode files = Json.newObject();
        ArrayNode arrayNode = files.putArray("files");
        arrayNode.add(new ObjectMapper().valueToTree(GFSFiles));
        BufferedWriter writer = new BufferedWriter(new FileWriter(gfsFile));
        writer.write(files.toString());
        writer.close();
    }

    /**
     * This method returns the list of gfs files by reading the json from the
     * json file and adds the appropriate chunkServer locations.
     *
     * @return A list of GFSFiles
     */
    public static List<GFSFile> getFiles() {
        List<GFSFile> gfsFiles = new ArrayList<>();
        JsonNode jsonNode;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(gfsFile));
            jsonNode = Json.parse(reader.readLine());
        } catch (IOException e) {
            Logger.error(e.getMessage());
            return gfsFiles;
        }

        GFSFiles = new ArrayList<>();
        jsonNode.get("files").get(0).forEach(x -> {
            GFSFile gfsFile = new GFSFile(x.get("name").asText());
            x.get("chunkMetadataList").forEach(e -> gfsFile.addChunkMetadata(new ChunkMetadata(e.get("id").asText())));
            GFSFiles.add(gfsFile);

        });
        return GFSFiles;
    }

}
