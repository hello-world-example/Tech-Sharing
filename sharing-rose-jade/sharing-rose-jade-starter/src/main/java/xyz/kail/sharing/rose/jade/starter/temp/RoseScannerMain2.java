package xyz.kail.sharing.rose.jade.starter.temp;

import net.paoding.rose.scanning.ResourceRef;
import net.paoding.rose.scanning.RoseScanner;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class RoseScannerMain2 {

    public static void main(String[] args) throws IOException {
        List<ResourceRef> resources = RoseScanner.getInstance().getJarOrClassesFolderResources();

        List<String> urls = new LinkedList<>();
        for (ResourceRef ref : resources) {
            if (ref.hasModifier("dao") || ref.hasModifier("DAO")) {
                try {
                    Resource resource = ref.getResource();
                    File resourceFile = resource.getFile();
                    if (resourceFile.isFile()) {
                        urls.add("jar:file:" + resourceFile.toURI().getPath()
                                + ResourceUtils.JAR_URL_SEPARATOR);
                    } else if (resourceFile.isDirectory()) {
                        urls.add(resourceFile.toURI().toString());
                    }
                } catch (IOException e) {
                    throw new ApplicationContextException("error on resource.getFile", e);
                }
            }
        }

        System.out.println(urls);


    }

}
