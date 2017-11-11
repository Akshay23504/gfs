package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class ChunkServer extends Controller {

    public Result poll() {
        //TODO: read in all hunks
        //TODO: create list of the unique ids of all chunks
        //TODO: return the list of the chunk unique ids
        return ok();
    }
}
