package xyz.kail.sharing.component.rose.web.controllers;

import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;
import xyz.kail.sharing.component.rose.web.RoseMain;

import javax.annotation.Resource;

@Path("/rose")
public class RoseController {

//    @Resource
//    private RoseMain roseMain;

    @Get("/index")
    public String index() {
        return "@rose-" + System.currentTimeMillis();
    }

}
