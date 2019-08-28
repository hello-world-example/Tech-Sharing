package xyz.kail.sharing.rose.jade.starter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SharingRepoConfig.class)
public class DaoConfig {
}
