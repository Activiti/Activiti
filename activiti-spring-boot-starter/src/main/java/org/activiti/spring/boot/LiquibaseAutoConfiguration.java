package org.activiti.spring.boot;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;


@Configuration
@EnableConfigurationProperties({LiquibaseProperties.class})
public class LiquibaseAutoConfiguration {


    @Bean
    public SpringLiquibase liquibase(DataSource dataSource,
                                     LiquibaseProperties liquibaseProperties,
                                     ActivitiProperties activitiProperties) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
        liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());

        if (!activitiProperties.isUseLiquibase()) {
            liquibase.setShouldRun(false);
        } else {
            liquibase.setShouldRun(liquibaseProperties.isEnabled());
        }
        return liquibase;
    }

}
