/* Licensed under the Apache License, Version 2.0 (the "License");
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
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Provides sane definitions for the various beans required to be productive with Activiti in Spring.
 *
 * @author Josh Long
 */
public abstract class AbstractProcessEngineConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractProcessEngineConfiguration.class);

  public ProcessEngineFactoryBean springProcessEngineBean(SpringProcessEngineConfiguration configuration) {
    ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
    processEngineFactoryBean.setProcessEngineConfiguration(configuration);
    return processEngineFactoryBean;
  }

  public SpringProcessEngineConfiguration processEngineConfigurationBean(Resource[] processDefinitions,
                                                                         DataSource dataSource,
                                                                         PlatformTransactionManager transactionManager,
                                                                         SpringAsyncExecutor springAsyncExecutor)
        throws IOException {

    SpringProcessEngineConfiguration engine = new SpringProcessEngineConfiguration();
    if (processDefinitions != null && processDefinitions.length > 0) {
      engine.setDeploymentResources(processDefinitions);
    }
    engine.setDataSource(dataSource);
    engine.setTransactionManager(transactionManager);

    if (null != springAsyncExecutor) {
      engine.setAsyncExecutorEnabled(true);
      engine.setAsyncExecutor(springAsyncExecutor);
    }

    return engine;
  }

  public List<Resource> discoverProcessDefinitionResources(ResourcePatternResolver applicationContext, String prefix, List<String> suffixes, boolean checkPDs) throws IOException {
    if (checkPDs) {

    	List<Resource> result = new ArrayList<Resource>();
    	for (String suffix : suffixes) {
    		String path = prefix + suffix;
    		Resource[] resources = applicationContext.getResources(path);
    		if (resources != null && resources.length > 0) {
    			for (Resource resource : resources) {
    				result.add(resource);
    			}
    		}
    	}
    	
    	if (result.isEmpty()) {
      	logger.info(String.format("No process definitions were found for autodeployment"));
    	}
    	
      return result;
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
