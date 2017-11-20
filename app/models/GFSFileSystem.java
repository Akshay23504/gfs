package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.parser.ParseException;
import play.libs.Json;
import scala.util.parsing.json.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.sound.midi.SysexMessage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GFSFileSystem implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final Integer chunkSizeBytes = 64;
    private static final String gfs_file = "gfs.json";
    private static List<GFSFile> GFSfiles;

    public GFSFileSystem() {
        GFSfiles = new ArrayList<>();
    }

    public static void addFile(GFSFile file) {
        ObjectMapper mapper = new ObjectMapper();
        GFSfiles.add(file);
        ObjectNode files = Json.newObject();
        ArrayNode arrayNode = files.putArray("files");
        arrayNode.add(mapper.valueToTree(GFSfiles));
        try {
            try (FileWriter open_file = new FileWriter(gfs_file))
            {
                try {
                    open_file.write(files.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Successfully updated json object to file...!!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<GFSFile> getGFSfiles() {
        File file = new File(gfs_file);
        JsonNode json = null;

        try (
                FileInputStream is =new FileInputStream(file);
        ){
            json = Json.parse(is);
        } catch(IOException e){
            e.printStackTrace();
        }
        GFSfiles = new ArrayList<>();
        System.out.println(json.toString());
        json.get("files").get(0).forEach(x -> {
            System.out.println(x.toString());
            GFSFile gfsFile = new GFSFile(x.get("name").asText());
            x.get("chunkMetadataList").forEach(e -> {
                gfsFile.addChunkMetadata(new ChunkMetadata(e.get("id").asText()));
            });
            GFSfiles.add(gfsFile);

        });
        System.out.println(GFSfiles);
        return GFSfiles;
    }




}
