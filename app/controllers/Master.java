package controllers;

import play.mvc.Controller;
import play.mvc.Result;

import java.util.UUID;

public class Master extends Controller {

    public Result chunkHandle(String filename, String chunkIndex) {
        return ok(new java.io.File("/build.sbt"));
    }

    public Result createFile(String filename) {
        //TODO: create first initial empty chunk
        String uniqueID = UUID.randomUUID().toString();
        return (ok("File to create: " + filename));
    }

    public Result registerChunkServer(String IP, String port) {
        return ok();
    }
}
