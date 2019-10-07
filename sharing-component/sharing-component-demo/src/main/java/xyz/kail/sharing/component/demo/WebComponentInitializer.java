package xyz.kail.sharing.component.demo;

import xyz.kail.sharing.component.demo.servlet.IndexServlet;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import java.util.Set;

public class WebComponentInitializer implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) {
        servletContext.addServlet(
                IndexServlet.class.getSimpleName(),
                IndexServlet.class
        ).addMapping("/demo/index");
    }
}
