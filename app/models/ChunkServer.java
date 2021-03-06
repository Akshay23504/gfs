package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for the chunkServer.
 * It also has the status of the chunkServer which the master will make use of.
 */
public class ChunkServer {

    public enum ChunkServerStatus {RUNNING, DEAD}

    private List<ChunkMetadata> chunkMetadataList;
    private String ip;
    private String port;
    private ChunkServerStatus status;

    public ChunkServer(String ip, String port) {
        this.ip = ip;
        this.port = port;
        this.status = ChunkServerStatus.RUNNING;
        chunkMetadataList = new ArrayList<>();
    }

    public List<ChunkMetadata> getChunkMetadataList() {
        return chunkMetadataList;
    }

    public void setChunkMetadataList(List<ChunkMetadata> chunkMetadataList) {
        this.chunkMetadataList = chunkMetadataList;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public ChunkServerStatus getStatus() {
        return status;
    }

    public void setStatus(ChunkServerStatus status) {
        this.status = status;
    }

    public String getAddress() {
        return this.ip + ":" + this.port;
    }

    public void addChunkServerMetadata(ChunkMetadata chunkMetadata) {
        this.getChunkMetadataList().add(chunkMetadata);
    }
}
