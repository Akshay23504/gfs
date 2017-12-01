package models;

/**
 * The model class that has the entities of the chunk metadata
 */
public class ChunkMetadata {

    private String uuid;
    private String address;

    public ChunkMetadata(String uuid) {
        this.uuid = uuid;
    }

    public String getId() {
        return uuid;
    }

    public void setId(String uuid) {
        this.uuid = uuid;
    }

    public void setAddress(String address) { this.address = address;}

    public String getAddress() {
        return this.address;
    }
}
