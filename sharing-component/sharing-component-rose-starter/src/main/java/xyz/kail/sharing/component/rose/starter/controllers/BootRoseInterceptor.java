package xyz.kail.sharing.component.rose.starter.controllers;


import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;

public class BootRoseInterceptor extends ControllerInterceptorAdapter {

    @Override
    protected Object before(Invocation inv) throws Exception {
        System.out.println(inv.getRequest().getRequestURI() + " ->> before");
        return super.before(inv);
    }

    @Override
    protected Object after(Invocation inv, Object instruction) throws Exception {
        System.out.println(inv.getRequest().getRequestURI() + " ->> after");
        return super.after(inv, instruction);
    }

    @Override
    public void afterCompletion(Invocation inv, Throwable ex) throws Exception {
        System.out.println(inv.getRequest().getRequestURI() + " ->> afterCompletion");
    }
}
