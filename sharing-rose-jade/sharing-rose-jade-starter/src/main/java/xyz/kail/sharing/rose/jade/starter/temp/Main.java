package xyz.kail.sharing.rose.jade.starter.temp;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        final Resource[] resources = resourcePatternResolver.getResources("classpath*:/META-INF/");

        Arrays.asList(resources).stream().forEach(r -> {
            try {
                System.out.println(r.getURI());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


}
