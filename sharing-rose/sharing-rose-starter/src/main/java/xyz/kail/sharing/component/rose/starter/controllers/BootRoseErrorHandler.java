package xyz.kail.sharing.component.rose.starter.controllers;


import net.paoding.rose.web.ControllerErrorHandler;
import net.paoding.rose.web.Invocation;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class BootRoseErrorHandler implements ControllerErrorHandler {

    private static final String APACHE_TOMCAT = "Apache Tomcat";
    private static final String STATUS_CODE_ATTR = "javax.servlet.error.status_code";

    private static final String EXCEPTION_ATTR = "javax.servlet.error.exception";
    private static final String EXCEPTION_TYPE_ATTR = "javax.servlet.error.exception_type";

    @Override
    public Object onError(Invocation inv, Throwable ex) throws Throwable {
        HttpServletRequest req = inv.getRequest();
        // 处理 Tomcat 容器自带异常报告
        String serverInfo = inv.getApplicationContext().getServletContext().getServerInfo();
        Object state = req.getAttribute(STATUS_CODE_ATTR);
        if (StringUtils.containsIgnoreCase(serverInfo, APACHE_TOMCAT) && null != state && "200".equals(state.toString())) {
            req.removeAttribute(EXCEPTION_TYPE_ATTR);
            req.removeAttribute(EXCEPTION_ATTR);
        }

        return "@error";
    }
}
