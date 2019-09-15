package xyz.kail.sharing.component.rose.starter.controllers;

import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;

@Path("/rose")
public class RoseController {

    @Get("/index")
    public String index() {
        return "@" + System.currentTimeMillis();
    }

}
