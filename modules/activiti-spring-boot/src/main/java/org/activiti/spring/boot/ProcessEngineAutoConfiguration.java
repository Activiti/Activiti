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

import org.activiti.engine.*;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringJobExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

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
 * @author Josh Long
 * @author Joram Barrez
 */
@Configuration
public class ProcessEngineAutoConfiguration {

    @Configuration
    public static class DefaultProcessEngineConfiguration
            extends AbstractProcessEngineConfiguration {

        @Bean
        public SpringJobExecutor springJobExecutor(TaskExecutor taskExecutor) {
            return new SpringJobExecutor(taskExecutor);
        }


        // @Value("${activiti.spring.processes.prefix:'classpath:/processes/'}")
        private String prefix = "classpath:/processes/";

        // @Value("${activiti.spring.processes.suffix:'**.bpmn20.xml'}")
        private String suffix = "**.bpmn20.xml";

        // @Value("${activiti.spring.processes.checkDeployed:'false'}")
        protected void setCheckProcessDefinitionDeployments(String check) {
            this.checkProcessDefinitionDeployments = Boolean.parseBoolean(check);
        }

        private boolean checkProcessDefinitionDeployments = true;

        @Autowired
        private ResourcePatternResolver resourceLoader;

        @Bean
        @ConditionalOnBean(DataSource.class)
        public SpringProcessEngineConfiguration activitiConfiguration(
                DataSource dataSource, PlatformTransactionManager transactionManager, SpringJobExecutor springJobExecutor) throws IOException {
            List<Resource> procDefResources = this.discoverProcessDefinitionResources(
                    this.resourceLoader, prefix, suffix, checkProcessDefinitionDeployments);
            return super.processEngineConfigurationBean(
                    procDefResources.toArray(new Resource[procDefResources.size()]), dataSource, transactionManager, springJobExecutor);
        }

        @Bean
        public ProcessEngine processEngine(SpringProcessEngineConfiguration configuration) throws Exception {
            ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
            processEngineFactoryBean.setProcessEngineConfiguration(configuration);
            return processEngineFactoryBean.getObject();
        }

        @Bean
        @ConditionalOnMissingBean
        @Override
        public RuntimeService runtimeServiceBean(ProcessEngine processEngine) {
            return super.runtimeServiceBean(processEngine);
        }

        @Bean
        @ConditionalOnMissingBean
        @Override
        public RepositoryService repositoryServiceBean(ProcessEngine processEngine) {
            return super.repositoryServiceBean(processEngine);
        }

        @Bean
        @ConditionalOnMissingBean
        @Override
        public TaskService taskServiceBean(ProcessEngine processEngine) {
            return super.taskServiceBean(processEngine);
        }

        @Bean
        @ConditionalOnMissingBean
        @Override
        public HistoryService historyServiceBean(ProcessEngine processEngine) {
            return super.historyServiceBean(processEngine);
        }

        @Bean
        @ConditionalOnMissingBean
        @Override
        public ManagementService managementServiceBeanBean(ProcessEngine processEngine) {
            return super.managementServiceBeanBean(processEngine);
        }

        @Bean
        @ConditionalOnMissingBean
        @Override
        public FormService formServiceBean(ProcessEngine processEngine) {
            return super.formServiceBean(processEngine);
        }

        @Bean
        @ConditionalOnMissingBean
        @Override
        public IdentityService identityServiceBean(ProcessEngine processEngine) {
            return super.identityServiceBean(processEngine);
        }

    }



/*


    @Configuration
    @ConditionalOnClass({EntityManagerFactory.class})
    public static class JpaAutoConfiguration {

        @Bean
        InitializingBean configureJpaForActiviti(
                final EntityManagerFactory emf,
                final ActivitiProperties activitiProperties,
                final SpringProcessEngineConfiguration processEngineAutoConfiguration) {
            return new InitializingBean() {

                @Override
                public void afterPropertiesSet() throws Exception {
                    if (activitiProperties.isJpa()) {
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

    */
/**
 * This installs the Activiti REST endpoints. The REST endpoints can be used to
 * query the running workflow engine.
 *//*

    @Configuration
    @ConditionalOnClass({ServletRegistrationBean.class, ServerServlet.class})
    public static class RestServiceAutoConfiguration {

        @Bean
        ServletRegistrationBean activitiRestRegistration(
                @Value("${spring.activiti.rest.mapping:activiti-rest}") String name,
                @Value("${spring.activiti.rest.mapping:'/activiti*/
/*'}") String mapping) {
            ServerServlet servlet = new ServerServlet();
            ServletRegistrationBean registration = new ServletRegistrationBean(servlet, mapping);
            registration.addInitParameter("org.restlet.application", "org.activiti.rest.service.application.ActivitiRestServicesApplication");
            registration.setName(name);

            return registration;
        }
    }
*/

}


/*


@Configuration
class ActivitiConfiguration {

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
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(

    ) {
        ActivitiConfigurer configurer = activitiConfigurer(activitiConfigurers);
        List<Resource> processDefinitionResources = new ArrayList<Resource>();
        configurer.processDefinitionResources(processDefinitionResources);
        SpringProcessEngineConfiguration engine = new SpringProcessEngineConfiguration();
        if (!processDefinitionResources.isEmpty()) {
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

	*/
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
	 *//*


*/
/*	protected PlatformTransactionManager platformTransactionManager(final DataSource dataSource) {
        return first(this.platformTransactionManagers,
		    new ObjectFactory<PlatformTransactionManager>() {
			    @Override
			    public PlatformTransactionManager getObject() throws BeansException {
				    return new DataSourceTransactionManager(dataSource);
			    }
		    });
	}*//*


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
                Resource defaultClassPathResourceMatcher = new ClassPathResource("classpath:/processes*/
/**bpmn20.xml");

 if (defaultClassPathResourceMatcher.exists()) {
 resources.add(defaultClassPathResourceMatcher);
 }

 if (activitiConfigurers != null && !activitiConfigurers.isEmpty()) {
 for (ActivitiConfigurer ac : activitiConfigurers) {
 ac.processDefinitionResources(resources);
 }
 }

 resourceList.addAll(resources);
 }

 @Override public void postProcessSpringProcessEngineConfiguration(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
 if (activitiConfigurers != null) {
 for (ActivitiConfigurer configurer : activitiConfigurers) {
 configurer.postProcessSpringProcessEngineConfiguration(springProcessEngineConfiguration);
 }
 }
 }


 };
 }


 private static <T> T first(List<T> tList, ObjectFactory<T> tObjectFactory) {
 T rt;
 if (tList != null && !tList.isEmpty()) {
 rt = tList.iterator().next();
 } else {
 rt = tObjectFactory.getObject();
 }
 return rt;
 }
 }
 */
