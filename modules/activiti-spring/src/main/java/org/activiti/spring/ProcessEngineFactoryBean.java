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

package org.activiti.spring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import javax.sql.DataSource;

import org.activiti.engine.DbSchemaStrategy;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.engine.impl.interceptor.CommandExecutorImpl;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.LogInterceptor;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.variable.VariableTypes;
import org.activiti.engine.repository.DeploymentBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Dave Syer
 * @author Christian Stettler
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ProcessEngineFactoryBean implements FactoryBean<ProcessEngine>, DisposableBean, ApplicationContextAware {

  protected ProcessEngineConfiguration processEngineConfiguration = new ProcessEngineConfiguration();
  protected PlatformTransactionManager transactionManager;
  protected ApplicationContext applicationContext;
  protected String deploymentName = "SpringAutoDeployment";
  protected Resource[] deploymentResources = new Resource[0];
  protected ProcessEngineImpl processEngine;

  public void destroy() throws Exception {
    if (processEngine != null) {
      processEngine.close();
    }
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public ProcessEngine getObject() throws Exception {
    initializeSpringTransactionInterceptor();
    initializeExpressionManager();

    processEngine = (ProcessEngineImpl) processEngineConfiguration.buildProcessEngine();

    if (deploymentResources.length > 0) {
      autoDeployResources();
    }
    
    return processEngine;

  }

  private void initializeSpringTransactionInterceptor() {
    processEngineConfiguration.setLocalTransactions(transactionManager == null);

    if (transactionManager != null) {
      List<CommandInterceptor> commandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
      commandInterceptorsTxRequired.add(new LogInterceptor());
      commandInterceptorsTxRequired.add(new SpringTransactionInterceptor(transactionManager, TransactionTemplate.PROPAGATION_REQUIRED));
      commandInterceptorsTxRequired.add(new CommandContextInterceptor());
      commandInterceptorsTxRequired.add(new CommandExecutorImpl());
      processEngineConfiguration.setCommandInterceptorsTxRequired(commandInterceptorsTxRequired);
      
      List<CommandInterceptor> commandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>();
      commandInterceptorsTxRequiresNew.add(new LogInterceptor());
      commandInterceptorsTxRequiresNew.add(new SpringTransactionInterceptor(transactionManager, TransactionTemplate.PROPAGATION_REQUIRES_NEW));
      commandInterceptorsTxRequiresNew.add(new CommandContextInterceptor());
      commandInterceptorsTxRequiresNew.add(new CommandExecutorImpl());
      processEngineConfiguration.setCommandInterceptorsTxRequiresNew(commandInterceptorsTxRequiresNew);
    }
  }

  protected void initializeExpressionManager() {
    processEngineConfiguration.setExpressionManager(new SpringExpressionManager(applicationContext));
  }
  
  public Class< ? > getObjectType() {
    return ProcessEngine.class;
  }

  public boolean isSingleton() {
    return true;
  }

  protected void autoDeployResources() throws IOException {
    RepositoryService repositoryService = processEngine.getRepositoryService();
    
    DeploymentBuilder deploymentBuilder = repositoryService
      .createDeployment()
      .enableDuplicateFiltering()
      .name(deploymentName);
    
    for (Resource resource : deploymentResources) {
      String resourceName = null;
      
      if (resource instanceof ContextResource) {
        resourceName = ((ContextResource) resource).getPathWithinContext();
        
      } else if (resource instanceof ByteArrayResource) {
        resourceName = resource.getDescription();
        
      } else {
        try {
          resourceName = resource.getFile().getAbsolutePath();
        } catch (IOException e) {
          resourceName = resource.getFilename();
        }
      }
      
      if ( resourceName.endsWith(".bar")
           || resourceName.endsWith(".zip")
           || resourceName.endsWith(".jar") ) {
        deploymentBuilder.addZipInputStream(new ZipInputStream(resource.getInputStream()));
      } else {
        deploymentBuilder.addInputStream(resourceName, resource.getInputStream());
      }
    }
    
    deploymentBuilder.deploy();
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
  
  public void setDeploymentName(String deploymentName) {
    this.deploymentName = deploymentName;
  }

  public void setDeploymentResources(Resource[] deploymentResources) {
    this.deploymentResources = deploymentResources;
  }

  public void setDataBaseName(String dataBaseName) {
    processEngineConfiguration.setDatabaseName(dataBaseName);
  }

  public void setDataSource(DataSource dataSource) {
    processEngineConfiguration.setDataSource(dataSource);
  }

  public void setDbSchemaStrategy(DbSchemaStrategy dbSchemaStrategy) {
    processEngineConfiguration.setDbSchemaStrategy(dbSchemaStrategy);
  }

  public void setHistoricDataService(HistoryService historicDataService) {
    processEngineConfiguration.setHistoricDataService(historicDataService);
  }

  public void setIdentityService(IdentityService identityService) {
    processEngineConfiguration.setIdentityService(identityService);
  }

  public void setIdGenerator(IdGenerator idGenerator) {
    processEngineConfiguration.setIdGenerator(idGenerator);
  }

  public void setJobExecutor(JobExecutor jobExecutor) {
    processEngineConfiguration.setJobExecutor(jobExecutor);
  }

  public void setJobExecutorAutoActivate(boolean jobExecutorAutoActivate) {
    processEngineConfiguration.setJobExecutorAutoActivate(jobExecutorAutoActivate);
  }

  public void setProcessEngineName(String processEngineName) {
    processEngineConfiguration.setProcessEngineName(processEngineName);
  }

  public void setVariableTypes(VariableTypes variableTypes) {
    processEngineConfiguration.setVariableTypes(variableTypes);
  }
  
  public void setMailServerHost(String mailServerHost) {
    processEngineConfiguration.setMailServerSmtpHost(mailServerHost);
  }
  
  public void setMailServerPort(int mailServerPort) {
    processEngineConfiguration.setMailServerSmtpPort(mailServerPort);
  }
  
  public void setMailServerUserName(String userName) {
    processEngineConfiguration.setMailServerSmtpUserName(userName);
  }
  
  public void setMailServerPassword(String password) {
    processEngineConfiguration.setMailServerSmtpPassword(password);
  }
  
  public void setMailServerDefaultFromAddress(String defaultFromAddress) {
    processEngineConfiguration.setMailServerDefaultFrom(defaultFromAddress);
  }
}
