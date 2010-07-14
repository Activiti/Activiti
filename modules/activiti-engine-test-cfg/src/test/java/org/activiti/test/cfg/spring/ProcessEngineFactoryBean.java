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
import org.activiti.impl.cfg.ProcessEngineFactory;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.interceptor.CommandInterceptor;
import org.activiti.impl.interceptor.DefaultCommandExecutor;
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

  private String dataBaseName;
  private DataSource dataSource;
  private PlatformTransactionManager transactionManager;
  private DbSchemaStrategy dbSchemaStrategy;
  private boolean jobExecutorAutoActivate;
  private String processEngineName;
  private ProcessEngineImpl processEngine;

  public void destroy() throws Exception {
    if (processEngine != null) {
      processEngine.close();
    }
  }

  public ProcessEngine getObject() throws Exception {

    Assert.state(dataBaseName != null, "A database name must be provided (e.g. 'h2')");

    ProcessEngineFactory factory = new ProcessEngineFactory();
    factory.setDbSchemaStrategy(dbSchemaStrategy);
    factory.setJobExecutorAutoActivate(jobExecutorAutoActivate);
    factory.setProcessEngineName(processEngineName);
    factory.setDataSource(dataSource);
    factory.setDataBaseName(dataBaseName);
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
    return processEngine;

  }
  public Class< ? > getObjectType() {
    return ProcessEngine.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setDataBaseName(String databaseName) {
    this.dataBaseName = databaseName;
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
