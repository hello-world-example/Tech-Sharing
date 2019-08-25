package xyz.kail.sharing.rose.jade.starter.temp;

import net.paoding.rose.jade.context.spring.JadeComponentProvider;

import java.net.URL;

public class JadeComponentProviderMain {

    public static void main(String[] args) {

        final URL resource = JadeComponentProviderMain.class.getResource("/");
        System.out.println(resource.toString());



        JadeComponentProvider provider = new JadeComponentProvider();
        provider.findCandidateComponents(resource.toString());
    }


}
