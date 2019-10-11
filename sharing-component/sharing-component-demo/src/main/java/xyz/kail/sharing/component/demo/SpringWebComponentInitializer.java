package xyz.kail.sharing.component.demo;

import org.springframework.web.WebApplicationInitializer;
import xyz.kail.sharing.component.demo.servlet.IndexSpringServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class SpringWebComponentInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.addServlet(
                IndexSpringServlet.class.getSimpleName(),
                IndexSpringServlet.class
        ).addMapping("/demo/spring/index");
    }
}
