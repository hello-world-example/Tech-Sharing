package xyz.kail.sharing.rose.jade.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource(locations = {
        "classpath*:/applicationContext-jade.xml",
        "classpath*:/jdbc_druid.xml",
})
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
