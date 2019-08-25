package xyz.kail.sharing.rose.jade.starter.temp;

import net.paoding.rose.scanning.ResourceRef;
import net.paoding.rose.scanning.RoseScanner;

import java.io.IOException;
import java.util.List;

public class RoseScannerMain1 {

    public static void main(String[] args) throws IOException {
        List<ResourceRef> resources = RoseScanner.getInstance().getJarOrClassesFolderResources();
        resources.forEach(resourceRef -> {
            System.out.println();
            System.out.println(resourceRef);

        });

    }

}
