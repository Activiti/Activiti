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
package org.activiti.engine.impl.cfg;

import javax.transaction.TransactionManager;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.jta.JtaTransactionContextFactory;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.JtaTransactionInterceptor;

/**
 * @author Tom Baeyens
 */
public class JtaProcessEngineConfiguration extends ProcessEngineConfigurationImpl {

  protected TransactionManager transactionManager;
  
  public JtaProcessEngineConfiguration() {
    this.transactionsExternallyManaged = true;
  }

  @Override
  protected CommandInterceptor createTransactionInterceptor() {
    if (transactionManager==null) {
      throw new ActivitiException("transactionManager is required property for JtaProcessEngineConfiguration, use "+StandaloneProcessEngineConfiguration.class.getName()+" otherwise");
    }
    
    return new JtaTransactionInterceptor(transactionManager);
  }

  @Override
  protected void initTransactionContextFactory() {
    if (transactionContextFactory == null) {
      transactionContextFactory = new JtaTransactionContextFactory(transactionManager);
    }
  }
  
  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void setTransactionManager(TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
}
