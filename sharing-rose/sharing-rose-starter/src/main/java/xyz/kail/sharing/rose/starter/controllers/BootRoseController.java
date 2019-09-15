package xyz.kail.sharing.rose.starter.controllers;

import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;

@Path("/rose")
public class BootRoseController {

    @Get("/index")
    public String index() {
        return "@" + System.currentTimeMillis();
    }

    @Get("/error")
    public String error() {
        return "@error" + (1 / 0);
    }

}
