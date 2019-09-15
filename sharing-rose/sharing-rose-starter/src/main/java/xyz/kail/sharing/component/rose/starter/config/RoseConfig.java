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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class RoseConfig extends WebMvcConfigurerAdapter {


    @Bean
    @Primary
    public FilterRegistrationBean roseFilter() {
        final RoseBootFilter roseBootFilter = new RoseBootFilter();
        // 设置忽略的链接
        roseBootFilter.setIgnoredPaths(new String[]{"noting"});
        roseBootFilter.setLoad("xyz.kail.sharing.component.rose.starter.controllers");

        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new RoseBootFilter());
        filterRegistrationBean.setName("roseFilter");
        filterRegistrationBean.setUrlPatterns(Collections.singletonList("/*"));
        filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE);
        return filterRegistrationBean;
    }


    /**
     * @see net.paoding.rose.RoseFilter
     */
    public static class RoseBootFilter extends GenericFilterBean {

        private static final Logger LOGGER = LoggerFactory.getLogger(RoseBootFilter.class);

        private InstructionExecutor instructionExecutor = new InstructionExecutorImpl();

        private List<Module> modules;

        private MappingNode mappingTree;

        private Class<? extends ModuleResourceProvider> moduleResourceProviderClass = ModuleResourceProviderImpl.class;

        private Class<? extends ModulesBuilder> modulesBuilderClass = ModulesBuilderImpl.class;

        private LoadScope load = new LoadScope("", "controllers");

        private IgnoredPath[] ignoredPaths = new IgnoredPath[]{
                new IgnoredPathStarts(RoseConstants.VIEWS_PATH_WITH_END_SEP),
                new IgnoredPathEquals("/favicon.ico")
        };


        /**
         * <pre>
         * like: &quot;com.renren.myapp, com.renren.yourapp&quot; etc
         * </pre>
         */
        public void setLoad(String load) {
            this.load = new LoadScope(load, "controllers");
        }

        /**
         * 支持 正则、前缀、后缀、等值
         *
         * @see #quicklyPass(RequestPath)
         */
        public void setIgnoredPaths(String[] ignoredPathStrings) {
            List<IgnoredPath> list = new ArrayList<>(ignoredPathStrings.length + 2);

            for (String ignoredPath : ignoredPathStrings) {
                ignoredPath = ignoredPath.trim();
                if (StringUtils.isEmpty(ignoredPath)) {
                    continue;
                }
                // 忽略所有
                if (ignoredPath.equals("*")) {
                    list.add(new IgnoredPathEquals(""));
                    list.add(new IgnoredPathStarts("/"));
                    break;
                }
                // 忽略匹配到的正则表达式
                if (ignoredPath.startsWith("regex:")) {
                    list.add(new IgnoredPathRegexMatch(ignoredPath.substring("regex:".length())));
                } else {
                    // 补全分隔符
                    if (ignoredPath.length() > 0 && !ignoredPath.startsWith("/") && !ignoredPath.startsWith("*")) {
                        ignoredPath = "/" + ignoredPath;
                    }
                    if (ignoredPath.endsWith("*")) {
                        // 前缀匹配
                        list.add(new IgnoredPathStarts(ignoredPath.substring(0, ignoredPath.length() - 1)));
                    } else if (ignoredPath.startsWith("*")) {
                        // 后缀匹配
                        list.add(new IgnoredPathEnds(ignoredPath.substring(1)));
                    } else {
                        // 等值匹配
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
        protected void initFilterBean() throws ServletException {
            try {

                long startTime = System.currentTimeMillis();

                //
                this.initRose();

                long endTime = System.currentTimeMillis();

                // 【3】打印启动信息
                printRoseInfos(endTime - startTime);

            } catch (Exception e) {
                StringBuilder sb = new StringBuilder(1024);
                sb.append("[Rose-").append(RoseVersion.getVersion());
                sb.append("@Spring-").append(SpringVersion.getVersion()).append("]:");
                sb.append(e.getMessage());
                LOGGER.error(sb.toString(), e);
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

            //
            // 【!】删除对 Portal 的支持【!】
            //

            // 创建RequestPath对象，用于记录对地址解析的结果
            // 1. 如果是 POST 请求，支持通过 _method 参数，修改指定的请求类型
            // 2. ContextPath
            // 3. requestPath
            final RequestPath requestPath = new RequestPath(httpRequest);

            //  简单、快速判断本次请求，如果不应由Rose执行，返回true (IgnoredPaths)
            if (quicklyPass(requestPath)) {
                notMatched(filterChain, httpRequest, httpResponse, requestPath);
                return;
            }

            // matched 为 true 代表本次请求被 Rose 匹配，不需要转发给容器的其他 filter 或 servlet
            boolean matched = false;
            try {
                // rose 对象代表 Rose 框架对一次请求的执行：一朵玫瑰出墙来
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


        protected void initRose() throws Exception {
            /**
             * 【0】 构建 WebApplicationContext
             *  @see net.paoding.rose.scanning.context.RoseWebAppContext
             */
            WebApplicationContext rootContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[init] call 'init/module'");
            }

            // 【1】识别 Rose 程序模块
            this.modules = prepareModules(rootContext);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[init] exits from 'init/module'");
                LOGGER.info("[init] call 'init/mappingTree'");
            }

            // 【2】创建匹配树以及各个结点的上的执行逻辑(Engine)
            this.mappingTree = prepareMappingTree(modules);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[init] exits from 'init/mappingTree'");
                LOGGER.info("[init] exits from 'init'");
            }
        }

        /**
         * 【1】自动扫描识别 Web 层资源，纳入 Rose 管理
         */
        private List<Module> prepareModules(WebApplicationContext rootContext) throws Exception {

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[init/mudule] starting ...");
            }

            ModuleResourceProvider provider = moduleResourceProviderClass.newInstance();

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[init/module] using provider: {}", provider);
                LOGGER.info("[init/module] call 'moduleResource': to find all module resources.");
                LOGGER.info("[init/module] load {}", load);
            }

            // 找资源（controllers），详见返回值 注释
            List<ModuleResource> moduleResources = provider.findModuleResources(load);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[init/mudule] exits 'moduleResource'");
            }

            ModulesBuilder modulesBuilder = modulesBuilderClass.newInstance();

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[init/module] using modulesBuilder: {}", modulesBuilder);
                LOGGER.info("[init/module] call 'moduleBuild': to build modules.");
            }

            List<Module> modules = modulesBuilder.build(moduleResources, rootContext);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[init/module] exits from 'moduleBuild'");
                LOGGER.info("[init/module] found {} modules.", modules.size());
            }

            return modules;
        }

        /**
         * 【2】
         */
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

        /**
         * Rose 本身没有匹配到时，调用其它 Filter
         */
        protected void notMatched(FilterChain chain, HttpServletRequest req, HttpServletResponse resp, RequestPath path) throws IOException, ServletException {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("not rose uri: {}", path.getUri());
            }

            // 调用其它 Filter
            chain.doFilter(req, resp);
        }

        /**
         * 构建并抛出 ServletException
         */
        private void throwServletException(RequestPath requestPath, Throwable exception) throws ServletException {
            String msg = requestPath.getMethod() + " " + requestPath.getUri();
            ServletException servletException;
            if (exception instanceof ServletException) {
                servletException = (ServletException) exception;
            } else {
                servletException = new NestedServletException(msg, exception);
            }
            LOGGER.error(msg, exception);

            // 【!】【!】
            getServletContext().log(msg, exception);

            throw servletException;
        }

        /**
         * 打印 Rose 信息
         */
        private void printRoseInfos(long initTimeCost) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(PrinteHelper.dumpModules(modules));
                LOGGER.debug("mapping tree:{} ", PrinteHelper.list(mappingTree));
            }

            String msg = String.format(
                    "[init] rose initialized, %s modules loaded, cost %sms! (version=%s)",
                    modules.size(),
                    initTimeCost,
                    RoseVersion.getVersion());

            LOGGER.info(msg);

            getServletContext().log(msg);
        }


        @Override
        public void destroy() {

            try {
                mappingTree.destroy();
            } catch (Exception e) {
                LOGGER.error("", e);
                getServletContext().log("", e);
            }

            super.destroy();
        }

    }


}
