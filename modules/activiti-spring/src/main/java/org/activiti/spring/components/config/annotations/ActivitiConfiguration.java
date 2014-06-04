package org.activiti.spring.components.config.annotations;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringJobExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.annotations.ActivitiConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * {@code @Configuration} class that registers a
 * {@link org.activiti.engine.ProcessEngine process engine} which has the
 * {@code bean} name {@code processEngine}. The result is comparable to
 * {@code <activiti:annotation-driven>}.
 * 
 * @author Josh Long
 * @see org.activiti.spring.annotations.EnableActiviti
 */
@Configuration
public class ActivitiConfiguration {

	@Autowired(required = false)
	private List<TaskExecutor> executors;

	@Autowired(required = false)
	private List<ActivitiConfigurer> activitiConfigurers;

	@Autowired(required = false)
	private Map<String, DataSource> dataSources;

	@Autowired(required = false)
	private List<PlatformTransactionManager> platformTransactionManagers;

	@Autowired(required = false)
	private List<SpringJobExecutor> springJobExecutors;

	@Bean
	public SpringProcessEngineConfiguration springProcessEngineConfiguration() {
		ActivitiConfigurer configurer = activitiConfigurer(activitiConfigurers);
		List<Resource> processDefinitionResources = new ArrayList<Resource>();
		configurer.processDefinitionResources(processDefinitionResources);
		SpringProcessEngineConfiguration engine = new SpringProcessEngineConfiguration();
		if (processDefinitionResources.size() > 0) {
			engine.setDeploymentResources(processDefinitionResources
			    .toArray(new Resource[processDefinitionResources.size()]));
		}
		DataSource dataSource = dataSource(configurer, dataSources);
		engine.setDataSource(dataSource);
		engine.setTransactionManager(platformTransactionManager(dataSource));
		engine.setJobExecutor(springJobExecutor());
		engine.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		configurer.postProcessSpringProcessEngineConfiguration(engine);
		return engine;
	}

	@Bean
	public ProcessEngineFactoryBean processEngine(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
		ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
		processEngineFactoryBean.setProcessEngineConfiguration(springProcessEngineConfiguration);
		return processEngineFactoryBean;
	}

	@Bean
	public RuntimeService runtimeService(ProcessEngine processEngine) {
		return processEngine.getRuntimeService();
	}

	@Bean
	public RepositoryService repositoryService(ProcessEngine processEngine) {
		return processEngine.getRepositoryService();
	}

	@Bean
	public TaskService taskService(ProcessEngine processEngine) {
		return processEngine.getTaskService();
	}

	@Bean
	public HistoryService historyService(ProcessEngine processEngine) {
		return processEngine.getHistoryService();
	}

	@Bean
	public ManagementService managementService(ProcessEngine processEngine) {
		return processEngine.getManagementService();
	}

	@Bean
	public FormService formService(ProcessEngine processEngine) {
		return processEngine.getFormService();
	}

	@Bean
	public IdentityService identityService(ProcessEngine processEngine) {
	  return processEngine.getIdentityService();
	}

	/*
	 * @Bean public static ProcessScopeBeanFactoryPostProcessor processScope() {
	 * return new ProcessScopeBeanFactoryPostProcessor(); }
	 * 
	 * @Bean public SharedProcessInstanceFactoryBean
	 * processInstanceFactoryBean(SharedProcessInstanceHolder
	 * sharedProcessInstanceHolder) { return new
	 * SharedProcessInstanceFactoryBean(sharedProcessInstanceHolder); }
	 * 
	 * @Bean public SharedProcessInstanceHolder processScopeContextHolder() {
	 * return new SharedProcessInstanceHolder(); }
	 */

	protected PlatformTransactionManager platformTransactionManager(final DataSource dataSource) {
		return first(this.platformTransactionManagers,
		    new ObjectFactory<PlatformTransactionManager>() {
			    @Override
			    public PlatformTransactionManager getObject() throws BeansException {
				    return new DataSourceTransactionManager(dataSource);
			    }
		    });
	}

	protected SpringJobExecutor springJobExecutor() {
		return first(this.springJobExecutors,
		    new ObjectFactory<SpringJobExecutor>() {
			    @Override
			    public SpringJobExecutor getObject() throws BeansException {
				    TaskExecutor taskExecutor = first(executors,
				        new ObjectFactory<TaskExecutor>() {
					        @Override
					        public TaskExecutor getObject() throws BeansException {
						        return new SyncTaskExecutor();
					        }
				        });
				    return new SpringJobExecutor(taskExecutor);
			    }
		    });
	}

	protected ActivitiConfigurer activitiConfigurer(final List<ActivitiConfigurer> activitiConfigurers) {

		return new ActivitiConfigurer() {
			@Override
			public void processDefinitionResources(List<Resource> resourceList) {
				List<Resource> resources = new ArrayList<Resource>();

				// lets first see if any exist in the default place:
				Resource defaultClassPathResourceMatcher = new ClassPathResource("classpath:/processes/**bpmn20.xml");

				if (defaultClassPathResourceMatcher.exists()) {
					resources.add(defaultClassPathResourceMatcher);
				}

				if (activitiConfigurers != null && activitiConfigurers.size() > 0) {
					for (ActivitiConfigurer ac : activitiConfigurers) {
						ac.processDefinitionResources(resources);
					}
				}

				resourceList.addAll(resources);
			}

			@Override
			public void postProcessSpringProcessEngineConfiguration(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
				if (activitiConfigurers != null) {
					for (ActivitiConfigurer configurer : activitiConfigurers) {
						configurer.postProcessSpringProcessEngineConfiguration(springProcessEngineConfiguration);
					}
				}
			}

			@Override
			public DataSource dataSource() {
				return null;
			}
		};
	}

	/**
	 * Sifts through beans available and returns the right one based on some common heuristics
	 */
	@SuppressWarnings("unchecked")
  private DataSource dataSource(ActivitiConfigurer activitiConfigurer, Map<String, DataSource> dataSourceMap) {
		DataSource ds = null;
		if (activitiConfigurer != null) {
			DataSource dataSource = activitiConfigurer.dataSource();
			if (null != dataSource) {
				ds = dataSource;
			}
		}

		if (dataSourceMap != null) {
		
			if (dataSourceMap.size() > 0) {
				for (DataSource d : dataSourceMap.values()) {
					ds = d;
				}
			}
	
			String defaultName = "activitiDataSource";
			if (dataSourceMap.containsKey(defaultName)) {
				ds = dataSourceMap.get(defaultName);
			}
			
		}

		// If no datasource is found at this point, we create a simple in-memory H2
		if (ds == null) {
			
			try {
				SimpleDriverDataSource simpleDriverDataSource = new SimpleDriverDataSource();
				simpleDriverDataSource.setDriverClass((Class<? extends Driver>) Class.forName("org.h2.Driver"));
				simpleDriverDataSource.setUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000");
				simpleDriverDataSource.setUsername("sa");
				simpleDriverDataSource.setPassword("");
				ds = simpleDriverDataSource;
			}
            catch (ClassNotFoundException e) {
				throw new ActivitiException("No dataSource bean was found. Tried to create default H2 in memory database, "
						+ "but couldn't find the driver on the classpath");
			}
		}
		
		return ds;
	}

	private static <T> T first(List<T> tList, ObjectFactory<T> tObjectFactory) {
		T rt;
		if (tList != null && tList.size() > 0) {
			rt = tList.iterator().next();
		} else {
			rt = tObjectFactory.getObject();
		}
		return rt;
	}
}
