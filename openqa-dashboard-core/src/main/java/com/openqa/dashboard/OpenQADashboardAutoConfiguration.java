package com.openqa.dashboard;

import com.openqa.dashboard.config.OpenQAProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.openqa.dashboard.repository")
@ComponentScan(basePackages = "com.openqa.dashboard")
@EnableConfigurationProperties({OpenQAProperties.class})
@ConditionalOnProperty(prefix = "openqa", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpenQADashboardAutoConfiguration {
}
