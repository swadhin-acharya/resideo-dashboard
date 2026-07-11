package com.resideo.dashboard;

import com.resideo.dashboard.config.ResideoProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.resideo.dashboard.repository")
@ComponentScan(basePackages = "com.resideo.dashboard")
@EnableConfigurationProperties(ResideoProperties.class)
@ConditionalOnProperty(prefix = "resideo", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ResideoDashboardAutoConfiguration {
}
