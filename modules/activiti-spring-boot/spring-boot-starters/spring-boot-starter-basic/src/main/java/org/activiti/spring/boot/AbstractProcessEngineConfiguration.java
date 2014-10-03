package org.activiti.spring.boot;

import org.activiti.engine.*;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringJobExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides sane definitions for the various beans required to be productive with Activiti in Spring.
 *
 * @author Josh Long
 */
public abstract class AbstractProcessEngineConfiguration {

    public ProcessEngineFactoryBean springProcessEngineBean(SpringProcessEngineConfiguration configuration) {
        ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
        processEngineFactoryBean.setProcessEngineConfiguration(configuration);
        return processEngineFactoryBean;
    }

    public SpringProcessEngineConfiguration processEngineConfigurationBean(Resource[] processDefinitions,
                                                                           DataSource dataSource,
                                                                           PlatformTransactionManager transactionManager,
                                                                           SpringJobExecutor springJobExecutor)
            throws IOException {

        SpringProcessEngineConfiguration engine = new SpringProcessEngineConfiguration();
        if (processDefinitions != null && processDefinitions.length > 0) {
            engine.setDeploymentResources(processDefinitions);
        }
        engine.setDataSource(dataSource);
        engine.setTransactionManager(transactionManager);

        if (null != springJobExecutor)
            engine.setJobExecutor(springJobExecutor);

        return engine;
    }

    public List<Resource> discoverProcessDefinitionResources(ResourcePatternResolver applicationContext, String prefix, String suffix, boolean checkPDs) throws IOException {
        String path = prefix + suffix;
        if (checkPDs) {
            Assert.state(applicationContext.getResource(prefix).exists(),
                    String.format("No process definitions were found using the specified " +
                            "path (%s). Are you sure " +
                            "you're using Activiti?", path));

            return Arrays.asList(applicationContext.getResources(path));
        }
        return new ArrayList<Resource>();
    }

    public RuntimeService runtimeServiceBean(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    public RepositoryService repositoryServiceBean(ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    public TaskService taskServiceBean(ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    public HistoryService historyServiceBean(ProcessEngine processEngine) {
        return processEngine.getHistoryService();
    }

    public ManagementService managementServiceBeanBean(ProcessEngine processEngine) {
        return processEngine.getManagementService();
    }

    public FormService formServiceBean(ProcessEngine processEngine) {
        return processEngine.getFormService();
    }

    public IdentityService identityServiceBean(ProcessEngine processEngine) {
        return processEngine.getIdentityService();
    }
}
