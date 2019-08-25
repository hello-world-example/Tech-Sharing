package xyz.kail.sharing.rose.jade.starter.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MyTestDAOIt {

    @Resource
    private MyTestDAO myTestDAO;

    @Resource
    private Map<String, DataSource> dataSourceMap;

    @Test
    public void count() {
        System.out.println(dataSourceMap);
        System.out.println(myTestDAO);
        System.out.println(myTestDAO.count());
    }
}
