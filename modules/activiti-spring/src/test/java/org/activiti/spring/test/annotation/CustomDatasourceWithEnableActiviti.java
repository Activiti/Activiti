package org.activiti.spring.test.annotation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.spring.annotations.EnableActiviti;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Joram Barrez
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CustomDatasourceWithEnableActiviti {
	
  @Configuration
  @EnableActiviti
  public static class Config {
  	
  	@Bean
  	public DataSource dataSource() {
  		BasicDataSource basicDataSource = new BasicDataSource();
  		basicDataSource.setUsername("sa");
  		basicDataSource.setUrl("jdbc:h2:mem:anotherDatabase");
  		basicDataSource.setDefaultAutoCommit(false);
  		basicDataSource.setDriverClassName(org.h2.Driver.class.getName());
  		basicDataSource.setPassword("");
  		return basicDataSource;
  	}
  	
  }
  
  @Autowired
  private ProcessEngine processEngine;
  
  @Autowired
  private RuntimeService runtimeService;
  
  @Autowired
  private TaskService taskService;
  
  @Autowired
  private HistoryService historyService;
  
  @Autowired
  private RepositoryService repositoryService;
  
  @Autowired
  private IdentityService identityService;
  
  @Autowired
  private ManagementService managementService;
  
  @Autowired
  private FormService formService;
  
  @Test
  public void testAutoWiring() {
  	Assert.assertNotNull(processEngine);
  	Assert.assertNotNull(runtimeService);
  	Assert.assertNotNull(taskService);
  	Assert.assertNotNull(historyService);
  	Assert.assertNotNull(repositoryService);
    Assert.assertNotNull(identityService);
  	Assert.assertNotNull(managementService);
  	Assert.assertNotNull(formService);
  }
  
  @Test
  public void testCustomDatasourceUsed() throws Exception {
  	Connection connection = DriverManager.getConnection("jdbc:h2:mem:anotherDatabase", "sa", "");
  	Assert.assertNotNull(connection);
  	
  	Statement statement = connection.createStatement();
  	ResultSet rs = statement.executeQuery("SELECT * FROM ACT_GE_PROPERTY");
  	int count = 0;
  	while (rs.next()) {
  		count++;
  	}
  	Assert.assertTrue(count > 0);
  }

}
