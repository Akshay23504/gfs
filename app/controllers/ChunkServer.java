package controllers;

import play.api.Play;
import play.mvc.Controller;
import play.mvc.Result;

public class ChunkServer extends Controller {

    public Result poll() {
        return ok();
    }
}
