package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class Application extends Controller {

    private final String root = "/Users/akshay/Documents/MacSystem/MS/1sem/Operating-Systems/Lab/Project3/gfs";

    public Result index() {
        return ok(index.render("GFS....."));
    }

    public Result chunkHandle(String filename, String chunkIndex) {
        return ok(new java.io.File("/build.sbt"));
    }

}
