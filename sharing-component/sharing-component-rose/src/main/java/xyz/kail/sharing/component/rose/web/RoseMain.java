package xyz.kail.sharing.component.rose.web;

import net.paoding.rose.scanning.context.core.RoseResources;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public class RoseMain {

    public RoseMain() {
        System.out.println("RoseMain");
    }

    public static void main(String[] args) throws IOException {
//        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
//        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(registry);
//        xmlBeanDefinitionReader.loadBeanDefinitions(RoseWebAppContext.DEFAULT_CONFIG_LOCATION);


        final List<Resource> contextResources = RoseResources.findContextResources((String[]) null);
        for (Resource resource : contextResources) {
            System.out.println(resource.getURI());
        }


    }

}
