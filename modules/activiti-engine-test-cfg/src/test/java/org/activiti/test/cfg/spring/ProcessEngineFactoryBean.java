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

package org.activiti.test.cfg.spring;

import javax.sql.DataSource;

import org.activiti.DbSchemaStrategy;
import org.activiti.ProcessEngine;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.cfg.ProcessEngineConfiguration;
import org.activiti.impl.db.IdGenerator;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.interceptor.CommandInterceptor;
import org.activiti.impl.interceptor.DefaultCommandExecutor;
import org.activiti.impl.persistence.IbatisPersistenceSessionFactory;
import org.activiti.impl.persistence.PersistenceSessionFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * @author Dave Syer
 */
public class ProcessEngineFactoryBean implements FactoryBean<ProcessEngine>, DisposableBean {

  private String databaseName;
  private DataSource dataSource;
  private PlatformTransactionManager transactionManager;
  private DbSchemaStrategy dbSchemaStrategy;
  private boolean jobExecutorAutoActivate;
  private String processEngineName;
  private ProcessEngineImpl processEngine;
  
  public void destroy() throws Exception {
    if (processEngine!=null) {
      processEngine.close();
    }
  }

  public ProcessEngine getObject() throws Exception {

    Assert.state(databaseName != null, "A database name must be provided (e.g. 'h2')");

    ProcessEngineConfiguration configuration = new ProcessEngineConfiguration();
    configuration.setDbSchemaStrategy(dbSchemaStrategy);
    configuration.setJobExecutorAutoActivate(jobExecutorAutoActivate);
    configuration.setProcessEngineName(processEngineName);
    IdGenerator idGenerator = configuration.getIdGenerator();
    PersistenceSessionFactory persistenceSessionFactory = new IbatisPersistenceSessionFactory(configuration.getVariableTypes(), idGenerator, databaseName, dataSource, transactionManager == null);
    configuration.setPersistenceSessionFactory(persistenceSessionFactory);

    if (transactionManager != null) {
      // FIXME: downcast
      DefaultCommandExecutor commandExecutor = (DefaultCommandExecutor) configuration.getCommandExecutor();
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
    }

    processEngine = configuration.buildProcessEngine();
    return processEngine;

  }
  public Class< ? > getObjectType() {
    return ProcessEngine.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setDbSchemaStrategy(DbSchemaStrategy dbSchemaStrategy) {
    this.dbSchemaStrategy = dbSchemaStrategy;
  }

  public void setJobExecutorAutoActivation(boolean jobExecutorAutoActivate) {
    this.jobExecutorAutoActivate = jobExecutorAutoActivate;
  }

  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }

  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

}
