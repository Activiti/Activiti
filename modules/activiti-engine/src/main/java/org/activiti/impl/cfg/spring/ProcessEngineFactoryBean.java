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

package org.activiti.impl.cfg.spring;

import java.io.IOException;
import java.util.zip.ZipInputStream;

import javax.el.ELResolver;
import javax.sql.DataSource;

import org.activiti.engine.DbSchemaStrategy;
import org.activiti.engine.DeploymentBuilder;
import org.activiti.engine.HistoricDataService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.impl.cfg.ProcessEngineFactory;
import org.activiti.impl.db.IdGenerator;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.interceptor.CommandInterceptor;
import org.activiti.impl.interceptor.DefaultCommandExecutor;
import org.activiti.impl.jobexecutor.JobExecutor;
import org.activiti.impl.variable.VariableTypes;
import org.activiti.pvm.event.ProcessEventBus;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Dave Syer
 * @author Christian Stettler
 */
public class ProcessEngineFactoryBean implements FactoryBean<ProcessEngine>, DisposableBean {

  private ProcessEngineFactory factory = new ProcessEngineFactory();

  private PlatformTransactionManager transactionManager;

  private Resource[] processResources = new Resource[0];

  private ProcessEngineImpl processEngine;

  public void destroy() throws Exception {
    if (processEngine != null) {
      processEngine.close();
    }
  }

  public ProcessEngine getObject() throws Exception {

    factory.setLocalTransactions(transactionManager == null);

    if (transactionManager != null) {
      DefaultCommandExecutor commandExecutor = new DefaultCommandExecutor(factory.getCommandContextFactory());
      commandExecutor.addCommandInterceptor(new CommandInterceptor() {

        public <T> T invoke(final CommandExecutor next, final Command<T> command) {
          // TODO: Add transaction attributes
          @SuppressWarnings("unchecked")
          T result = (T) new TransactionTemplate(transactionManager).execute(new TransactionCallback() {

            public Object doInTransaction(TransactionStatus status) {
              return next.execute(command);
            }
          });
          return result;
        }
      });
      factory.setCommandExecutor(commandExecutor);
    }

    processEngine = factory.createProcessEngine();
    refreshProcessResources(processEngine.getProcessService(), processResources);
    return processEngine;

  }
  public Class< ? > getObjectType() {
    return ProcessEngine.class;
  }

  public boolean isSingleton() {
    return true;
  }

  protected void refreshProcessResources(ProcessService processService, Resource[] processResources) throws IOException {
    for (Resource resource : processResources) {
      String name = getResourceName(resource);
      DeploymentBuilder deploymentBuilder = processService.createDeployment().name(name);
      deploy(deploymentBuilder, resource, name);
    }
  }

  private void deploy(DeploymentBuilder deploymentBuilder, Resource resource, String name) throws IOException {
    if (name.endsWith(".zip") || name.endsWith(".jar")) {
      deploymentBuilder.addZipInputStream(new ZipInputStream(resource.getInputStream()));
    } else {
      deploymentBuilder.addInputStream(name, resource.getInputStream());
    }
    deploymentBuilder.deploy();
  }

  private String getResourceName(Resource resource) {
    String name;
    if (resource instanceof ContextResource) {
      name = ((ContextResource) resource).getPathWithinContext();
    } else if (resource instanceof ByteArrayResource) {
      name = resource.getDescription();
    } else {
      try {
        name = resource.getFile().getAbsolutePath();
      } catch (IOException e) {
        name = resource.getFilename();
      }
    }
    return name;
  }

  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public void setProcessResources(Resource[] processResources) {
    this.processResources = processResources;
  }

  public void setProcessEventBus(ProcessEventBus processEventBus) {
    factory.setProcessEventBus(processEventBus);
  }
  
  public void setCommandExecutor(CommandExecutor commandExecutor) {
    factory.setCommandExecutor(commandExecutor);
  }

  public void setDataBaseName(String dataBaseName) {
    factory.setDatabaseName(dataBaseName);
  }

  public void setDataSource(DataSource dataSource) {
    factory.setDataSource(dataSource);
  }

  public void setDbSchemaStrategy(DbSchemaStrategy dbSchemaStrategy) {
    factory.setDbSchemaStrategy(dbSchemaStrategy);
  }

  public void setHistoricDataService(HistoricDataService historicDataService) {
    factory.setHistoricDataService(historicDataService);
  }

  public void setIdentityService(IdentityService identityService) {
    factory.setIdentityService(identityService);
  }

  public void setIdGenerator(IdGenerator idGenerator) {
    factory.setIdGenerator(idGenerator);
  }

  public void setJobExecutor(JobExecutor jobExecutor) {
    factory.setJobExecutor(jobExecutor);
  }

  public void setJobExecutorAutoActivate(boolean jobExecutorAutoActivate) {
    factory.setJobExecutorAutoActivate(jobExecutorAutoActivate);
  }

  public void setProcessEngineName(String processEngineName) {
    factory.setProcessEngineName(processEngineName);
  }

  public void setVariableTypes(VariableTypes variableTypes) {
    factory.setVariableTypes(variableTypes);
  }

  public void setElResolver(ELResolver elResolver) {
    factory.setElResolver(elResolver);
  }

}
