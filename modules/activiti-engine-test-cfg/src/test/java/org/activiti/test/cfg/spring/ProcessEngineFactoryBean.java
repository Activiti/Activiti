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
import org.activiti.impl.cfg.ProcessEngineConfiguration;
import org.activiti.impl.db.IdGenerator;
import org.activiti.impl.persistence.IbatisPersistenceSessionFactory;
import org.activiti.impl.persistence.PersistenceSessionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

/**
 * @author Dave Syer
 */
public class ProcessEngineFactoryBean implements FactoryBean {

  private ProcessEngineConfiguration configuration = new ProcessEngineConfiguration();
  private String databaseName;
  private DataSource dataSource;
  private PlatformTransactionManager transactionManager;

  public Object getObject() throws Exception {
    Assert.state(databaseName != null, "A database name must be provided (e.g. 'h2')");
    IdGenerator idGenerator = configuration.getIdGenerator();
    PersistenceSessionFactory persistenceSessionFactory = new IbatisPersistenceSessionFactory(idGenerator, databaseName, dataSource, transactionManager == null);
    configuration.setPersistenceSessionFactory(persistenceSessionFactory);
    if (transactionManager != null) {
      // configuration.setTransactionContextFactory(new SpringTransactionContextFactory(transactionManager));
    }
    return configuration.buildProcessEngine();
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
    configuration.setDbSchemaStrategy(dbSchemaStrategy);
  }

  public void setJobExecutorAutoActivation(boolean jobExecutorAutoActivate) {
    configuration.setJobExecutorAutoActivate(jobExecutorAutoActivate);
  }

  public void setProcessEngineName(String processEngineName) {
    configuration.setProcessEngineName(processEngineName);
  }

  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

}
