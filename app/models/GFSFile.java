package models;

import java.util.ArrayList;
import java.util.List;

/**
 * The model class that has the entities of the GFSFile like name of the file,
 * and also the metadata list.
 */
public class GFSFile {

    private String name;
    public List<ChunkMetadata> chunkMetadataList;

    public GFSFile(String name) {
        this.name = name;
        chunkMetadataList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addChunkMetadata(ChunkMetadata chunkMetadata) {
        this.chunkMetadataList.add(chunkMetadata);
    }
}
