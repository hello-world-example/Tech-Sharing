package xyz.kail.sharing.component.boot;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.List;

public class SpringFactoriesLoaderMain {

    public static void main(String[] args) {
        final List<String> strings = SpringFactoriesLoader.loadFactoryNames(EnableAutoConfiguration.class, ClassLoader.getSystemClassLoader());
        System.out.println(strings);
    }

}
