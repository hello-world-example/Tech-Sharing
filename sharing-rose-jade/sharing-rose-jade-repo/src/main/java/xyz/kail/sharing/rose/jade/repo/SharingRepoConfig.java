package xyz.kail.sharing.rose.jade.repo;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

/**
 * @author kail
 */
@Configuration
public class SharingRepoConfig implements BeanFactoryPostProcessor {

    private static final String DATA_SOURCE_NAME = "jade.dataSource.xyz.kail.sharing.rose.jade.repo.dao";

    private static final String ERROR_MESSAGE = "未找到 test 的数据源，请确认或者排除 vehiclecheck-repo 依赖";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 获取所有的数据源
        Map<String, DataSource> dataSourceMap = beanFactory.getBeansOfType(DataSource.class);

        // 没有数据源抛出错误
        if (dataSourceMap.isEmpty()) {
            throw new Error(ERROR_MESSAGE);
        }

        for (Map.Entry<String, DataSource> ds : dataSourceMap.entrySet()) {
            try (Connection conn = ds.getValue().getConnection()) {
                // 必须是链接到 test 数据库的数据源
                if (Objects.equals(conn.getCatalog(), "test")) {
                    // 数据源起一个别名
                    beanFactory.registerAlias(ds.getKey(), DATA_SOURCE_NAME);
                    return;
                }
            } catch (SQLException e) {
                throw new Error(ERROR_MESSAGE, e);
            }
        }

        // 没找到抛出异常
        throw new Error(ERROR_MESSAGE);
    }
}
