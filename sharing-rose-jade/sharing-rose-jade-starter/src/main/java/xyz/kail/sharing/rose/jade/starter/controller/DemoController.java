package xyz.kail.sharing.rose.jade.starter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.kail.sharing.rose.jade.repo.dao.MyTest2DAO;
import xyz.kail.sharing.rose.jade.starter.dao.MyTestDAO;

import javax.annotation.Resource;
import java.util.Date;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @Resource
    private MyTestDAO myTestDAO;

    @GetMapping("/index")
    public Object index() {
        return new Date() + "--" + myTestDAO.count();
    }


    @Resource
    private MyTest2DAO myTest2DAO;

    @GetMapping("/index2")
    public Object index2() {
        return new Date() + "--" + myTest2DAO.count();
    }

}
