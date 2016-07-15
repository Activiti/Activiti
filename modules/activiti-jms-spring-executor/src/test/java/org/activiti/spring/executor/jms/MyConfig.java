package org.activiti.spring.executor.jms;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;

import org.activiti.engine.ProcessEngine;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class MyConfig {
  
  @Bean
  public DataSource dataSource() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000");
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    return dataSource;
  }
  
  @Bean(name = "transactionManager")
  public PlatformTransactionManager transactionManager() {
    DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
    transactionManager.setDataSource(dataSource());
    return transactionManager;
  }
  
  @Bean
  public SpringProcessEngineConfiguration processEngineConfiguration() {
    SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
    configuration.setDataSource(dataSource());
    configuration.setTransactionManager(transactionManager());
    configuration.setDatabaseSchemaUpdate(SpringProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    configuration.setAsyncExecutorMessageQueueMode(true);
    
    MessageBasedJobManager messageBasedJobManager = jobManager();
    configuration.setJobManager(messageBasedJobManager);
    
    return configuration;        
  }
  
  @Bean
  public ProcessEngine ProcessEngine() {
    return processEngineConfiguration().buildProcessEngine();
  }
  
  @Bean
  public MessageBasedJobManager jobManager() {
    MessageBasedJobManager jobManager = new MessageBasedJobManager(null);
    jobManager.setJmsTemplate(jmsTemplate());
    return jobManager;
  }
  
  @Bean
  public ConnectionFactory connectionFactory() {
      ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
      activeMQConnectionFactory.setBrokerURL("tcp://localhost:61616");
      activeMQConnectionFactory.setUseAsyncSend(true);
      CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(activeMQConnectionFactory);
      return cachingConnectionFactory;
  }
  
  @Bean
  public JmsTemplate jmsTemplate() {
      JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setDefaultDestination(new ActiveMQQueue("activiti-jobs"));
      jmsTemplate.setConnectionFactory(connectionFactory());
      return jmsTemplate;
  }
  
  @Bean
  public MessageListenerContainer messageListenerContainer() {
      DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
      messageListenerContainer.setConnectionFactory(connectionFactory());
      messageListenerContainer.setDestinationName("activiti-jobs");
      messageListenerContainer.setMessageListener(jobMessageListener());
      messageListenerContainer.setConcurrentConsumers(10);
      messageListenerContainer.start();
      return messageListenerContainer;
  }
  
  @Bean 
  public JobMessageListener jobMessageListener() {
    JobMessageListener jobMessageListener = new JobMessageListener();
    jobMessageListener.setProcessEngineConfiguration(processEngineConfiguration());
    return jobMessageListener;
  }
  
  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

}
