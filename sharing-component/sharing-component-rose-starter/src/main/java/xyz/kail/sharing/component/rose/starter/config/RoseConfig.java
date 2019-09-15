package xyz.kail.sharing.component.rose.starter.config;

import net.paoding.rose.RoseConstants;
import net.paoding.rose.RoseVersion;
import net.paoding.rose.scanner.ModuleResource;
import net.paoding.rose.scanner.ModuleResourceProvider;
import net.paoding.rose.scanner.ModuleResourceProviderImpl;
import net.paoding.rose.scanning.LoadScope;
import net.paoding.rose.util.PrinteHelper;
import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.mapping.ConstantMapping;
import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.impl.mapping.MappingNode;
import net.paoding.rose.web.impl.mapping.TreeBuilder;
import net.paoding.rose.web.impl.mapping.ignored.*;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.module.ModulesBuilder;
import net.paoding.rose.web.impl.module.ModulesBuilderImpl;
import net.paoding.rose.web.impl.thread.LinkedEngine;
import net.paoding.rose.web.impl.thread.RootEngine;
import net.paoding.rose.web.impl.thread.Rose;
import net.paoding.rose.web.instruction.InstructionExecutor;
import net.paoding.rose.web.instruction.InstructionExecutorImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.SpringVersion;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.util.NestedServletException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Configuration
public class RoseConfig extends WebMvcConfigurerAdapter {


    @Bean
    @Primary
    public FilterRegistrationBean roseFilter() {
        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new RoseBootFilter());
        filterRegistrationBean.setName("roseFilter");
        filterRegistrationBean.setUrlPatterns(Collections.singletonList("/*"));
        filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE);
        filterRegistrationBean.setOrder(1);
        return filterRegistrationBean;
    }


    /**
     * @see net.paoding.rose.RoseFilter
     */
    public static class RoseBootFilter extends GenericFilterBean {

        private static final String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE;

        private InstructionExecutor instructionExecutor = new InstructionExecutorImpl();

        private List<Module> modules;

        private MappingNode mappingTree;

        private Class<? extends ModuleResourceProvider> moduleResourceProviderClass = ModuleResourceProviderImpl.class;

        private Class<? extends ModulesBuilder> modulesBuilderClass = ModulesBuilderImpl.class;

        private LoadScope load = new LoadScope("", "controllers");

        private IgnoredPath[] ignoredPaths = new IgnoredPath[]{
                new IgnoredPathStarts(RoseConstants.VIEWS_PATH_WITH_END_SEP),
                new IgnoredPathEquals("/favicon.ico")};

        public void setInstructionExecutor(InstructionExecutor instructionExecutor) {
            this.instructionExecutor = instructionExecutor;
        }

        public void setModuleResourceProviderClass(Class<? extends ModuleResourceProvider> moduleResourceProviderClass) {
            this.moduleResourceProviderClass = moduleResourceProviderClass;
        }

        public void setModulesBuilderClass(Class<? extends ModulesBuilder> modulesBuilderClass) {
            this.modulesBuilderClass = modulesBuilderClass;
        }

        /**
         * <pre>
         * like: &quot;com.renren.myapp, com.renren.yourapp&quot; etc
         * </pre>
         *
         * @param load
         */
        public void setLoad(String load) {
            this.load = new LoadScope(load, "controllers");
        }

        /**
         * @param ignoredPathStrings
         * @see #quicklyPass(RequestPath)
         */
        public void setIgnoredPaths(String[] ignoredPathStrings) {
            List<IgnoredPath> list = new ArrayList<>(ignoredPathStrings.length + 2);
            for (String ignoredPath : ignoredPathStrings) {
                ignoredPath = ignoredPath.trim();
                if (StringUtils.isEmpty(ignoredPath)) {
                    continue;
                }
                if (ignoredPath.equals("*")) {
                    list.add(new IgnoredPathEquals(""));
                    list.add(new IgnoredPathStarts("/"));
                    break;
                }
                if (ignoredPath.startsWith("regex:")) {
                    list.add(new IgnoredPathRegexMatch(ignoredPath.substring("regex:".length())));
                } else {
                    if (ignoredPath.length() > 0 && !ignoredPath.startsWith("/")
                            && !ignoredPath.startsWith("*")) {
                        ignoredPath = "/" + ignoredPath;
                    }
                    if (ignoredPath.endsWith("*")) {
                        list.add(new IgnoredPathStarts(ignoredPath.substring(0,
                                ignoredPath.length() - 1)));
                    } else if (ignoredPath.startsWith("*")) {
                        list.add(new IgnoredPathEnds(ignoredPath.substring(1)));
                    } else {
                        list.add(new IgnoredPathEquals(ignoredPath));
                    }
                }
            }
            IgnoredPath[] ignoredPaths = Arrays.copyOf(this.ignoredPaths, this.ignoredPaths.length + list.size());
            for (int i = this.ignoredPaths.length; i < ignoredPaths.length; i++) {
                ignoredPaths[i] = list.get(i - this.ignoredPaths.length);
            }
            this.ignoredPaths = ignoredPaths;
        }

        /**
         * 实现 {@link GenericFilterBean#initFilterBean()}，对 Rose 进行初始化
         */
        @Override
        protected final void initFilterBean() throws ServletException {
            try {

                long startTime = System.currentTimeMillis();

                if (logger.isInfoEnabled()) {
                    logger.info("[init] call 'init/rootContext'");
                }

                if (logger.isDebugEnabled()) {
                    StringBuilder sb = new StringBuilder();
                    @SuppressWarnings("unchecked")
                    Enumeration<String> iter = getFilterConfig().getInitParameterNames();
                    while (iter.hasMoreElements()) {
                        String name = iter.nextElement();
                        sb.append(name).append("='").append(getFilterConfig().getInitParameter(name)).append("'\n");
                    }
                    logger.debug("[init] parameters: " + sb);
                }

                // TODO
                WebApplicationContext rootContext = (WebApplicationContext) getServletContext().getAttribute(ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

                if (logger.isInfoEnabled()) {
                    logger.info("[init] exits from 'init/rootContext'");
                    logger.info("[init] call 'init/module'");
                }

                // 识别 Rose 程序模块
                this.modules = prepareModules(rootContext);

                if (logger.isInfoEnabled()) {
                    logger.info("[init] exits from 'init/module'");
                    logger.info("[init] call 'init/mappingTree'");
                }

                // 创建匹配树以及各个结点的上的执行逻辑(Engine)
                this.mappingTree = prepareMappingTree(modules);

                if (logger.isInfoEnabled()) {
                    logger.info("[init] exits from 'init/mappingTree'");
                    logger.info("[init] exits from 'init'");
                }

                long endTime = System.currentTimeMillis();

                // 打印启动信息
                printRoseInfos(endTime - startTime);

                //
            } catch (final Throwable e) {
                StringBuilder sb = new StringBuilder(1024);
                sb.append("[Rose-").append(RoseVersion.getVersion());
                sb.append("@Spring-").append(SpringVersion.getVersion()).append("]:");
                sb.append(e.getMessage());
                logger.error(sb.toString(), e);
                throw new NestedServletException(sb.toString(), e);
            }
        }

        /**
         * 接收所有进入 RoseFilter 的请求进行匹配，如果匹配到有相应的处理类处理它则由这个类来处理他、渲染并响应给客户端。
         * 如果没有找到匹配的处理器，Rose将把请求转交给整个过滤链的下一个组件，让web容器的其他组件来处理它。
         */
        @Override
        public void doFilter(ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
            // cast
            final HttpServletRequest httpRequest = (HttpServletRequest) request;
            final HttpServletResponse httpResponse = (HttpServletResponse) response;

            // 打开DEBUG级别信息能看到所有进入RoseFilter的请求
            if (logger.isDebugEnabled()) {
                StringBuffer sb = httpRequest.getRequestURL();
                String query = httpRequest.getQueryString();
                if (query != null && query.length() > 0) {
                    sb.append("?").append(query);
                }
                logger.debug(httpRequest.getMethod() + " " + sb.toString());
            }

            supportsRosePipe(httpRequest);

            // 创建RequestPath对象，用于记录对地址解析的结果
            final RequestPath requestPath = new RequestPath(httpRequest);

            //  简单、快速判断本次请求，如果不应由Rose执行，返回true
            if (quicklyPass(requestPath)) {
                notMatched(filterChain, httpRequest, httpResponse, requestPath);
                return;
            }

            // matched为true代表本次请求被Rose匹配，不需要转发给容器的其他 flter 或 servlet
            boolean matched = false;
            try {
                // rose 对象代表Rose框架对一次请求的执行：一朵玫瑰出墙来
                final Rose rose = new Rose(modules, mappingTree, httpRequest, httpResponse, requestPath);

                // 对请求进行匹配、处理、渲染以及渲染后的操作，如果找不到映配则返回false
                matched = rose.start();

            } catch (Throwable exception) {
                throwServletException(requestPath, exception);
            }

            // 非Rose的请求转发给WEB容器的其他组件处理，而且不放到上面的try-catch块中
            if (!matched) {
                notMatched(filterChain, httpRequest, httpResponse, requestPath);
            }
        }

        // @see net.paoding.rose.web.portal.impl.PortalWaitInterceptor#waitForWindows
        protected void supportsRosePipe(final HttpServletRequest httpRequest) {
            // 这个代码为rosepipe所用，以避免rosepipe的"Cannot forward after response has been committed"异常
            // @see net.paoding.rose.web.portal.impl.PortalWaitInterceptor
            Object window = httpRequest.getAttribute(RoseConstants.WINDOW_ATTR);
            if (window != null && window.getClass().getName().startsWith("net.paoding.rose.web.portal")) {
                httpRequest.setAttribute(RoseConstants.PIPE_WINDOW_IN, Boolean.TRUE);
                if (logger.isDebugEnabled()) {
                    try {
                        logger.debug("notify window '"
                                + httpRequest.getAttribute("$$paoding-rose-portal.window.name") + "'");
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
                synchronized (window) {
                    window.notifyAll();
                }
            }
        }


        private List<Module> prepareModules(WebApplicationContext rootContext) throws Exception {
            // 自动扫描识别web层资源，纳入Rose管理
            if (logger.isInfoEnabled()) {
                logger.info("[init/mudule] starting ...");
            }

            ModuleResourceProvider provider = moduleResourceProviderClass.newInstance();

            if (logger.isInfoEnabled()) {
                logger.info("[init/module] using provider: " + provider);
                logger.info("[init/module] call 'moduleResource': to find all module resources.");
                logger.info("[init/module] load " + load);
            }
            List<ModuleResource> moduleResources = provider.findModuleResources(load);

            if (logger.isInfoEnabled()) {
                logger.info("[init/mudule] exits 'moduleResource'");
            }

            ModulesBuilder modulesBuilder = modulesBuilderClass.newInstance();

            if (logger.isInfoEnabled()) {
                logger.info("[init/module] using modulesBuilder: " + modulesBuilder);
                logger.info("[init/module] call 'moduleBuild': to build modules.");
            }

            List<Module> modules = modulesBuilder.build(moduleResources, rootContext);

            if (logger.isInfoEnabled()) {
                logger.info("[init/module] exits from 'moduleBuild'");
                logger.info("[init/mudule] found " + modules.size() + " modules.");
            }

            return modules;
        }

        private MappingNode prepareMappingTree(List<Module> modules) {
            Mapping rootMapping = new ConstantMapping("");
            MappingNode mappingTree = new MappingNode(rootMapping);
            LinkedEngine rootEngine = new LinkedEngine(null, new RootEngine(instructionExecutor), mappingTree);
            mappingTree.getMiddleEngines().addEngine(ReqMethod.ALL, rootEngine);

            TreeBuilder treeBuilder = new TreeBuilder();
            treeBuilder.create(mappingTree, modules);

            return mappingTree;
        }

        /**
         * 简单、快速判断本次请求，如果不应由Rose执行，返回true
         */
        private boolean quicklyPass(final RequestPath requestPath) {
            for (IgnoredPath p : ignoredPaths) {
                if (p.hit(requestPath)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void destroy() {
            WebApplicationContext rootContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
            if (rootContext != null) {
                try {
                    if (rootContext instanceof AbstractApplicationContext) {
                        // rose.root
                        ((AbstractApplicationContext) rootContext).close();
                    }
                } catch (Throwable e) {
                    logger.error("", e);
                    getServletContext().log("", e);
                }
            }

            try {
                mappingTree.destroy();
            } catch (Throwable e) {
                logger.error("", e);
                getServletContext().log("", e);
            }

            super.destroy();
        }

        protected void notMatched(FilterChain filterChain, //
                                  HttpServletRequest httpRequest,//
                                  HttpServletResponse httpResponse,//
                                  RequestPath path) throws IOException, ServletException {

            if (logger.isDebugEnabled()) {
                logger.debug("not rose uri: " + path.getUri());
            }
            // 调用其它Filter
            filterChain.doFilter(httpRequest, httpResponse);
        }

        private void throwServletException(RequestPath requestPath, Throwable exception) throws ServletException {
            String msg = requestPath.getMethod() + " " + requestPath.getUri();
            ServletException servletException;
            if (exception instanceof ServletException) {
                servletException = (ServletException) exception;
            } else {
                servletException = new NestedServletException(msg, exception);
            }
            logger.error(msg, exception);
            getServletContext().log(msg, exception);
            throw servletException;
        }

        private void printRoseInfos(long initTimeCost) {
            if (logger.isDebugEnabled()) {
                logger.debug(PrinteHelper.dumpModules(modules));
                logger.debug("mapping tree:\n" + PrinteHelper.list(mappingTree));
            }

            String msg = String.format(
                    "[init] rose initialized, %s modules loaded, cost %sms! (version=%s)",
                    modules.size(),
                    initTimeCost,
                    RoseVersion.getVersion());

            logger.info(msg);

            getServletContext().log(msg);
        }
    }


}
