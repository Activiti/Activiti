package org.activiti.rest.conf;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.AbstractFormType;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.rest.form.MonthFormType;
import org.activiti.rest.form.ProcessDefinitionFormType;
import org.activiti.rest.form.UserFormType;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class ActivitiEngineConfiguration {

  private final Logger log = LoggerFactory.getLogger(ActivitiEngineConfiguration.class);

  @Autowired
  protected Environment environment;

  @Bean
  public DataSource dataSource() {
    
    String jdbcUrl = environment.getProperty("jdbc.url", "jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000");
    String jdbcDriver = environment.getProperty("jdbc.driver", "org.h2.Driver");
    String jdbcUsername = environment.getProperty("jdbc.username", "sa");
    String jdbcPassword = environment.getProperty("jdbc.password", "");
    
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(jdbcUrl);
    dataSource.setDriverClassName(jdbcDriver);
    dataSource.setUsername(jdbcUsername);
    dataSource.setPassword(jdbcPassword);
    
    // Connection pool settings (see https://github.com/brettwooldridge/HikariCP)
    Long connectionTimeout = environment.getProperty("datasource.connection.timeout", Long.class);
    if (connectionTimeout != null) {
      dataSource.setConnectionTimeout(connectionTimeout);
    }
    
    Long idleTimeout = environment.getProperty("datasource.connection.idletimeout", Long.class);
    if (idleTimeout != null) {
      dataSource.setIdleTimeout(idleTimeout);
    }
    
    Long maxLifetime = environment.getProperty("datasource.connection.maxlifetime", Long.class);
    if (maxLifetime != null) {
      dataSource.setMaxLifetime(maxLifetime);
    }
    
    Integer minIdle = environment.getProperty("datasource.connection.minidle", Integer.class);
    if (minIdle != null) {
      dataSource.setMinimumIdle(minIdle);
    }
    
    Integer maxPoolSize = environment.getProperty("datasource.connection.maxpoolsize", Integer.class);
    if (maxPoolSize != null) {
      dataSource.setMaximumPoolSize(maxPoolSize);
    }
    
    return dataSource;
  }
  
  @Bean(name = "transactionManager")
  public PlatformTransactionManager annotationDrivenTransactionManager() {
    DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
    transactionManager.setDataSource(dataSource());
    return transactionManager;
  }

  @Bean(name = "processEngineFactoryBean")
  public ProcessEngineFactoryBean processEngineFactoryBean() {
    ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
    factoryBean.setProcessEngineConfiguration(processEngineConfiguration());
    return factoryBean;
  }

  @Bean(name = "processEngine")
  public ProcessEngine processEngine() {
    // Safe to call the getObject() on the @Bean annotated
    // processEngineFactoryBean(), will be
    // the fully initialized object instanced from the factory and will NOT
    // be created more than once
    try {
      return processEngineFactoryBean().getObject();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  @Bean(name = "processEngineConfiguration")
  public ProcessEngineConfigurationImpl processEngineConfiguration() {
    SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
    processEngineConfiguration.setDataSource(dataSource());
    processEngineConfiguration.setDatabaseSchemaUpdate(environment.getProperty("engine.schema.update", "true"));
    processEngineConfiguration.setTransactionManager(annotationDrivenTransactionManager());
    processEngineConfiguration.setAsyncExecutorActivate(Boolean.valueOf(environment.getProperty("engine.asyncexecutor.activate", "true")));
    processEngineConfiguration.setHistory(environment.getProperty("engine.history.level", "full"));

    List<AbstractFormType> formTypes = new ArrayList<AbstractFormType>();
    formTypes.add(new UserFormType());
    formTypes.add(new ProcessDefinitionFormType());
    formTypes.add(new MonthFormType());
    processEngineConfiguration.setCustomFormTypes(formTypes);

    return processEngineConfiguration;
  }

  @Bean
  public RepositoryService repositoryService() {
    return processEngine().getRepositoryService();
  }

  @Bean
  public RuntimeService runtimeService() {
    return processEngine().getRuntimeService();
  }

  @Bean
  public TaskService taskService() {
    return processEngine().getTaskService();
  }

  @Bean
  public HistoryService historyService() {
    return processEngine().getHistoryService();
  }

  @Bean
  public FormService formService() {
    return processEngine().getFormService();
  }

  @Bean
  public IdentityService identityService() {
    return processEngine().getIdentityService();
  }

  @Bean
  public ManagementService managementService() {
    return processEngine().getManagementService();
  }
}
