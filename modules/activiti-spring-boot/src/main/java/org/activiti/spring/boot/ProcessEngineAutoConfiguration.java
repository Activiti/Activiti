/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.spring.boot;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.servlet.Servlet;
import javax.sql.DataSource;

import org.activiti.engine.ProcessEngine;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.annotations.ActivitiConfigurer;
import org.activiti.spring.annotations.EnableActiviti;
import org.activiti.spring.boot.ProcessEngineAutoConfiguration.ActivitiProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.ext.servlet.ServerServlet;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <p>
 * Auto configuration for using Activiti from a <a
 * href="http://spring.io/projects/spring-boot">Spring Boot application</a>.
 * Provides a configured {@link org.activiti.engine.ProcessEngine} if none other
 * is detected.
 * <p>
 * Discovers any process definitions deployed in the
 * {@literal src/main/resources/process} folder, and uses the single
 * {@link javax.sql.DataSource} bean discovered in the Spring application
 * context..
 * 
 *
 * @author Josh Long
 * @author Joram Barrez
 */
@Configuration
@ConditionalOnClass({ ProcessEngine.class, EnableActiviti.class })
@ConditionalOnBean(DataSource.class)
@EnableConfigurationProperties(ActivitiProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ConditionalOnMissingBean(ProcessEngine.class)
public class ProcessEngineAutoConfiguration {

	@ConfigurationProperties("spring.activiti")
	public static class ActivitiProperties {
		private boolean checkProcessDefinitions;

		private String deploymentName;

		private String databaseSchemaUpdate;

		private String databaseSchema;

		private String processDefinitionLocationPrefix;

		private String processDefinitionLocationSuffix;

		private boolean enableJpa = true; // true by default

		public boolean isCheckProcessDefinitions() {
			return checkProcessDefinitions;
		}

		public void setCheckProcessDefinitions(boolean checkProcessDefinitions) {
			this.checkProcessDefinitions = checkProcessDefinitions;
		}

		public String getDeploymentName() {
			return deploymentName;
		}

		public void setDeploymentName(String deploymentName) {
			this.deploymentName = deploymentName;
		}

		public String getDatabaseSchemaUpdate() {
			return databaseSchemaUpdate;
		}

		public void setDatabaseSchemaUpdate(String databaseSchemaUpdate) {
			this.databaseSchemaUpdate = databaseSchemaUpdate;
		}

		public String getDatabaseSchema() {
			return databaseSchema;
		}

		public void setDatabaseSchema(String databaseSchema) {
			this.databaseSchema = databaseSchema;
		}

		public String getProcessDefinitionLocationPrefix() {
			return processDefinitionLocationPrefix;
		}

		public void setProcessDefinitionLocationPrefix(
		        String processDefinitionLocationPrefix) {
			this.processDefinitionLocationPrefix = processDefinitionLocationPrefix;
		}

		public String getProcessDefinitionLocationSuffix() {
			return processDefinitionLocationSuffix;
		}

		public void setProcessDefinitionLocationSuffix(
		        String processDefinitionLocationSuffix) {
			this.processDefinitionLocationSuffix = processDefinitionLocationSuffix;
		}

		public boolean isEnableJpa() {
			return enableJpa;
		}

		public void setEnableJpa(boolean enableJpa) {
			this.enableJpa = enableJpa;
		}

	}

	@Configuration
	@EnableActiviti
	public static class DefaultActivitiConfiguration implements
	        ActivitiConfigurer {

		private Log log = LogFactory.getLog(getClass());

		public static final String PROCESS_DEFINITIONS_DEFAULT_PREFIX = "classpath:/processes/";

		public static final String PROCESS_DEFINITIONS_DEFAULT_SUFFIX = "**.bpmn20.xml";

		@Autowired
		private DataSource[] dataSources;

		@Autowired
		private PlatformTransactionManager transactionManager;

		@Autowired
		private ApplicationContext applicationContext;

		@Autowired
		private ActivitiProperties activitiProperties;

		@Override
		public void processDefinitionResources(List<Resource> resourceList) {
			List<Resource> resources;
			String prefix, suffix;
			try {

				prefix = defaultText(
				        activitiProperties.getProcessDefinitionLocationPrefix(),
				        PROCESS_DEFINITIONS_DEFAULT_PREFIX);
				suffix = defaultText(
				        activitiProperties.getProcessDefinitionLocationSuffix(),
				        PROCESS_DEFINITIONS_DEFAULT_SUFFIX);
				String path = prefix + suffix;

				boolean checkProcDefs = activitiProperties
				        .isCheckProcessDefinitions();
				if (checkProcDefs) {
					Assert.state(
					        this.applicationContext.getResource(prefix)
					                .exists(),
					        String.format(
					                "No process definitions were found deployed at %s. Are you actually using Activiti? ",
					                path));
				}

				// loop through the process definitions discovered.
				resources = Arrays.asList(this.applicationContext
				        .getResources(path));

				if (log.isInfoEnabled()) {
					log.info(String.format(
					        "found %s process definitions in %s.",
					        resources.size(), prefix));
					for (Resource resource : resources) {
						log.info(String.format("found process definition: %s",
						        resource.getURI().toString()));
					}
				}

			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			resourceList.addAll(resources);
		}

		private final String defaultText(String i, String o) {
			return (StringUtils.hasText(i)) ? i : o;
		}

		@Override
		public void postProcessSpringProcessEngineConfiguration(
		        SpringProcessEngineConfiguration processEngineConfiguration) {

			// Deployment tweaks
			processEngineConfiguration.setDeploymentName(defaultText(
			        activitiProperties.getDeploymentName(),
			        processEngineConfiguration.getDeploymentName()));

			// Database tweaks
			processEngineConfiguration.setDatabaseSchema(defaultText(
			        activitiProperties.getDatabaseSchema(),
			        processEngineConfiguration.getDatabaseSchema()));

			processEngineConfiguration.setDatabaseSchema(defaultText(
			        activitiProperties.getDatabaseSchemaUpdate(),
			        processEngineConfiguration.getDatabaseSchemaUpdate()));

		}

		@Override
		public DataSource dataSource() {
			Assert.isTrue(
			        this.dataSources.length > 0,
			        "you must have configured at least one javax.sql.DataSource bean in your Spring application context.");
			return this.dataSources[0];
		}

	}

	@Configuration
	@ConditionalOnClass({ EntityManagerFactory.class })
	public static class ActivitiJpaConfiguration {

		@Bean
		InitializingBean configureJpaForActiviti(
		        final EntityManagerFactory emf,
		        final ActivitiProperties activitiProperties,
		        final SpringProcessEngineConfiguration processEngineAutoConfiguration) {
			return new InitializingBean() {

				@Override
				public void afterPropertiesSet() throws Exception {
					if (activitiProperties.isEnableJpa()) {
						processEngineAutoConfiguration
						        .setJpaEntityManagerFactory(emf);
						processEngineAutoConfiguration
						        .setJpaHandleTransaction(false);
						processEngineAutoConfiguration
						        .setJpaCloseEntityManager(false);
					}
				}
			};
		}

	}
	
	@Configuration
	@ConditionalOnClass({ ServerServlet.class })
	public static class ActivitiRestConfiguration {
		@Bean
		ServletRegistrationBean activitiRestRegistration() {
			ServerServlet servlet = new ServerServlet() ;
			ServletRegistrationBean registration = new ServletRegistrationBean( servlet , "/activiti/*");
			registration.addInitParameter("org.restlet.application", "org.activiti.rest.service.application.ActivitiRestServicesApplication");
			registration.setName( "activiti-rest");
			 
			return registration;
		}
	}

	  
	
}
