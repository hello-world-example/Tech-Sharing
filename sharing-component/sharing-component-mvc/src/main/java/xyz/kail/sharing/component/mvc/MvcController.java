package xyz.kail.sharing.component.mvc;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MvcController {

    @RequestMapping("/date")
    public String date() {
        return String.valueOf(System.currentTimeMillis());
    }

}
