package org.activiti.test.spring.boot;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import activiti.Application;
import activiti.mappers.CustomMybatisMapper;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.boot.DataSourceProcessEngineAutoConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * @author Dominik Bartos
 */
public class CustomMybatisMapperConfigurationTest {


    @Test
    public void executeCustomMybatisMapperQuery() throws Exception {
        AnnotationConfigApplicationContext applicationContext = this.context(Application.class);
        ManagementService managementService = applicationContext.getBean(ManagementService.class);
        String processDefinitionId = managementService.executeCustomSql(new AbstractCustomSqlExecution<CustomMybatisMapper, String>(CustomMybatisMapper.class) {
            @Override
            public String execute(CustomMybatisMapper customMybatisMapper) {
                return customMybatisMapper.loadProcessDefinitionIdByKey("waiter");
            }
        });
        Assert.assertNotNull("the processDefinitionId should not be null!", processDefinitionId);
    }

    @Test
    public void executeCustomMybatisXmlQuery() throws Exception {
        AnnotationConfigApplicationContext applicationContext = this.context(Application.class);
        ManagementService managementService = applicationContext.getBean(ManagementService.class);
        String processDefinitionDeploymentId = managementService.executeCommand(new Command<String>() {
            @Override
            public String execute(CommandContext commandContext) {
                return (String) commandContext.getDbSqlSession().selectOne("selectProcessDefinitionDeploymentIdByKey", "waiter");
            }
        });
        Assert.assertNotNull("the processDefinitionDeploymentId should not be null!", processDefinitionDeploymentId);
    }

    private AnnotationConfigApplicationContext context(Class<?>... clzz) throws IOException {
        AnnotationConfigApplicationContext annotationConfigApplicationContext
                = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.register(clzz);

        File springBootPropertiesFile = new File("src/test/resources/config/application.properties");
        Properties springBootProperties = new Properties();
        springBootProperties.load(new FileInputStream(springBootPropertiesFile));

        annotationConfigApplicationContext
                .getEnvironment()
                .getPropertySources()
                .addFirst(new PropertiesPropertySource("testProperties", springBootProperties));

        annotationConfigApplicationContext.refresh();
        return annotationConfigApplicationContext;
    }
}
