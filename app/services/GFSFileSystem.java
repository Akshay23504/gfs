package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.GFSFile;
import play.libs.Json;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GFSFileSystem implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final Integer chunkSizeBytes = 64;
    private static final String gfsFile = "../conf/gfs.json";
    private static List<GFSFile> GFSFiles = new ArrayList<>();

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
        /*List<GFSFile> gfsFiles = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(gfsFile));
            JsonNode jsonNode = Json.parse(reader.readLine());
            jsonNode.get("files").get(0).forEach(x -> {
                GFSFile gfsFile = new GFSFile()
            });
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return gfsFiles;
        }

        GFSFiles = new ArrayList<>();
        System.out.println(json.toString());
        json.get("files").get(0).forEach(x -> {
            System.out.println(x.toString());
            GFSFile gfsFile = new GFSFile(x.get("name").asText());
            x.get("chunkMetadataList").forEach(e -> {
                gfsFile.addChunkMetadata(new ChunkMetadata(e.get("id").asText()));
            });
            GFSFiles.add(gfsFile);

        });
        System.out.println(GFSFiles);*/
        return GFSFiles;
    }

    private void saveGFS(GFSFileSystem fileSystem) {
        /*FileOutputStream fos = null;
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
        }*/
    }

    public GFSFileSystem getGFS() {
        /*FileInputStream fis = null;
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

        return gfsFileSystem;*/
        return null;
    }

}
