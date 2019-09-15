package xyz.kail.sharing.component.boot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boot")
public class BootController {

    @GetMapping("/index")
    public long index() {
        return System.currentTimeMillis();
    }

}
