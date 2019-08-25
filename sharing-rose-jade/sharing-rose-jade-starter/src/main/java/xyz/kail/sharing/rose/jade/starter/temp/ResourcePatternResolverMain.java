package xyz.kail.sharing.rose.jade.starter.temp;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.stream.Stream;

public class ResourcePatternResolverMain {

    public static void main(String[] args) throws IOException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        final Resource[] resources = resourcePatternResolver.getResources("classpath*:/META-INF/");
        Stream.of(resources).forEach(resource->{
            try {
                System.out.println(resource.getURI());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

}
