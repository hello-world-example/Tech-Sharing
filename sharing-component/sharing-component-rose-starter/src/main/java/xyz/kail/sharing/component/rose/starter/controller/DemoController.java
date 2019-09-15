package xyz.kail.sharing.component.rose.starter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boot")
public class DemoController {

    @GetMapping("/index")
    public long index() {
        return System.currentTimeMillis();
    }

}
