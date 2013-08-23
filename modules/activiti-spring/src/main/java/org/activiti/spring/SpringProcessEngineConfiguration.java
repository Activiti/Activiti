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
import java.util.zip.ZipInputStream;

import javax.sql.DataSource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.variable.EntityManagerSession;
import org.activiti.engine.repository.DeploymentBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;


/**
 * @author Tom Baeyens
 * @author David Syer
 * @author Joram Barrez
 */
public class SpringProcessEngineConfiguration extends ProcessEngineConfigurationImpl implements ApplicationContextAware {

  protected PlatformTransactionManager transactionManager;
  protected String deploymentName = "SpringAutoDeployment";
  protected Resource[] deploymentResources = new Resource[0];
  protected ApplicationContext applicationContext;
  protected Integer transactionSynchronizationAdapterOrder = null;
  
  
  public SpringProcessEngineConfiguration() {
    this.transactionsExternallyManaged = true;
  }
  
  @Override
  public ProcessEngine buildProcessEngine() {
    ProcessEngine processEngine = super.buildProcessEngine();
    autoDeployResources(processEngine);
    return processEngine;
  }
  
  public void setTransactionSynchronizationAdapterOrder(Integer transactionSynchronizationAdapterOrder) {
    this.transactionSynchronizationAdapterOrder = transactionSynchronizationAdapterOrder;
  }

  @Override
  protected void initDefaultCommandConfig() {
    if (defaultCommandConfig==null) {
      defaultCommandConfig = new CommandConfig().setContextReusePossible(true);
    }
  }

  @Override
  protected CommandInterceptor createTransactionInterceptor() {
    if (transactionManager==null) {
      throw new ActivitiException("transactionManager is required property for SpringProcessEngineConfiguration, use "+StandaloneProcessEngineConfiguration.class.getName()+" otherwise");
    }
    
    return new SpringTransactionInterceptor(transactionManager);
  }

  @Override
  protected void initTransactionContextFactory() {
    if(transactionContextFactory == null && transactionManager != null) {
      transactionContextFactory = new SpringTransactionContextFactory(transactionManager, transactionSynchronizationAdapterOrder);
    }
  }
  
  @Override
  protected void initJpa() {
    super.initJpa();
    if (jpaEntityManagerFactory != null) {
      sessionFactories.put(EntityManagerSession.class, 
              new SpringEntityManagerSessionFactory(jpaEntityManagerFactory, jpaHandleTransaction, jpaCloseEntityManager));
    }
  }

  protected void autoDeployResources(ProcessEngine processEngine) {
    if (deploymentResources!=null && deploymentResources.length>0) {
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
        
        try {
          if ( resourceName.endsWith(".bar")
               || resourceName.endsWith(".zip")
               || resourceName.endsWith(".jar") ) {
            deploymentBuilder.addZipInputStream(new ZipInputStream(resource.getInputStream()));
          } else {
            deploymentBuilder.addInputStream(resourceName, resource.getInputStream());
          }
        } catch (IOException e) {
          throw new ActivitiException("couldn't auto deploy resource '"+resource+"': "+e.getMessage(), e);
        }
      }
      
      deploymentBuilder.deploy();
    }
  }
  
  @Override
  public ProcessEngineConfiguration setDataSource(DataSource dataSource) {
    if(dataSource instanceof TransactionAwareDataSourceProxy) {
      return super.setDataSource(dataSource);
    } else {
      // Wrap datasource in Transaction-aware proxy
      DataSource proxiedDataSource = new TransactionAwareDataSourceProxy(dataSource);
      return super.setDataSource(proxiedDataSource);
    }
  }
  
  public PlatformTransactionManager getTransactionManager() {
    return transactionManager;
  }
  
  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public String getDeploymentName() {
    return deploymentName;
  }
  
  public void setDeploymentName(String deploymentName) {
    this.deploymentName = deploymentName;
  }
  
  public Resource[] getDeploymentResources() {
    return deploymentResources;
  }
  
  public void setDeploymentResources(Resource[] deploymentResources) {
    this.deploymentResources = deploymentResources;
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
