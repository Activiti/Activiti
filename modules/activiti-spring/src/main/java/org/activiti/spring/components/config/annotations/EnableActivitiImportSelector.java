package org.activiti.spring.components.config.annotations;

import org.activiti.engine.*;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringJobExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.components.support.ProcessStartingBeanPostProcessor;
import org.activiti.spring.components.support.StateHandlerBeanFactoryPostProcessor;
import org.activiti.spring.components.registry.StateHandlerRegistry;
import org.activiti.spring.components.support.ProcessScopeBeanFactoryPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.*;

/**
 * Sets up all the same things in a given configuration class as the equivalent
 * XML namespace usage would, with some conveniences.
 * <p/>
 * We automatically detect types in the context that match key objects like un-ambiguous {@link javax.sql.DataSource dataSources}, and delegate to those.
 * <p/>
 * We'll also look at supporting a configurer class.
 * <p/>
 * We at
 *
 * @author Josh Long
 */
public class EnableActivitiImportSelector
        implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{DefaultEnableActivitiConfiguration.class.getName()};
    }


    @Configuration
    public static class DefaultEnableActivitiConfiguration {


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

            final DataSource finalDataSource = dataSource(configurer, dataSources);
            final SpringJobExecutor springJobExecutor = springJobExecutor();
            final SpringProcessEngineConfiguration springProcessEngineConfiguration = new SpringProcessEngineConfiguration();
            final PlatformTransactionManager platformTransactionManager = platformTransactionManager(finalDataSource);

            springProcessEngineConfiguration.setDataSource(finalDataSource);
            springProcessEngineConfiguration.setTransactionManager(platformTransactionManager);
            springProcessEngineConfiguration.setJobExecutor(springJobExecutor);
            springProcessEngineConfiguration.setDeploymentResources(resources(configurer));

            configurer.postProcessSpringProcessEngineConfiguration(springProcessEngineConfiguration);

            return springProcessEngineConfiguration;
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
        public static StateHandlerBeanFactoryPostProcessor stateHandlerAnnotationBeanFactoryPostProcessor() {
            return new StateHandlerBeanFactoryPostProcessor();
        }

        @Bean
        public ManagementService managementService(ProcessEngine processEngine) {
            return processEngine.getManagementService();
        }

        @Bean
        public StateHandlerRegistry stateHandlerRegistry(ProcessEngine processEngine) {
            return new StateHandlerRegistry(processEngine);
        }

        @Bean
        public static ProcessScopeBeanFactoryPostProcessor processScope() {
            return new ProcessScopeBeanFactoryPostProcessor();
        }

        @Bean
        public ProcessStartingBeanPostProcessor processStartingBeanPostProcessor(ProcessEngine processEngine) {
            return new ProcessStartingBeanPostProcessor(processEngine);
        }

        private PlatformTransactionManager platformTransactionManager(final DataSource dataSource) {
            return first(this.platformTransactionManagers, new ObjectFactory<PlatformTransactionManager>() {
                @Override
                public PlatformTransactionManager getObject() throws BeansException {
                    return new DataSourceTransactionManager(dataSource);
                }
            });
        }

        private SpringJobExecutor springJobExecutor() {
            return first(this.springJobExecutors, new ObjectFactory<SpringJobExecutor>() {
                @Override
                public SpringJobExecutor getObject() throws BeansException {
                    TaskExecutor taskExecutor = first(executors, new ObjectFactory<TaskExecutor>() {
                        @Override
                        public TaskExecutor getObject() throws BeansException {
                            return new SyncTaskExecutor();
                        }
                    });
                    return new SpringJobExecutor(taskExecutor);
                }
            });
        }

        private ActivitiConfigurer activitiConfigurer(final List<ActivitiConfigurer> activitiConfigurers) {

            return new ActivitiConfigurer() {
                @Override
                public void processDefinitionResources(List<Resource> resourceList) {

                    List<Resource> resources = new ArrayList<Resource>();
                    resources.add(new ClassPathResource("classpath:/processes/**bpmn20.xml"));
                    if (activitiConfigurers != null && activitiConfigurers.size() > 0)
                        for (ActivitiConfigurer ac : activitiConfigurers)
                            ac.processDefinitionResources(resources);
                }

                @Override
                public void postProcessSpringProcessEngineConfiguration(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
                    for (ActivitiConfigurer configurer : activitiConfigurers)
                        configurer.postProcessSpringProcessEngineConfiguration(springProcessEngineConfiguration);
                }

                @Override
                public DataSource dataSource() {
                    return null;
                }
            };
        }

        /**
         * sifts through beans available and returns the right one based on some common heuristics:
         */
        private DataSource dataSource(ActivitiConfigurer activitiConfigurer, Map<String, DataSource> dataSourceMap) {
            DataSource ds = null;
            if (activitiConfigurer != null) {
                DataSource dataSource = activitiConfigurer.dataSource();
                if (null != dataSource) {
                    ds = dataSource;
                }
            }

            String defaultName = "activitiDataSource";
            if (dataSourceMap.size() > 0) {
                for (DataSource d : dataSourceMap.values()) {
                    ds = d;
                }
            }

            if (dataSourceMap.containsKey(defaultName)) {
                ds = dataSourceMap.get(defaultName);
            }

            Assert.notNull(ds, "there must be at least one valid DataSource");
            return ds;
        }

        private static <T> T first(List<T> tList, ObjectFactory<T> tObjectFactory) {
            T rt = null;
            if (tList != null && tList.size() > 0) {
                rt = tList.iterator().next();
            } else {
                rt = tObjectFactory.getObject();
            }
            return rt;
        }

        private Resource[] resources(ActivitiConfigurer activitiConfigurer) {
            List<Resource> defaultResources = new ArrayList<Resource>();
            if (null != activitiConfigurer) {
                activitiConfigurer.processDefinitionResources(defaultResources);
            }
            return defaultResources.toArray(new Resource[defaultResources.size()]);
        }
    }
}