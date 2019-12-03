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

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.SpringBeanFactoryProxyMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**





 */
public class ProcessEngineFactoryBean implements FactoryBean<ProcessEngine>, DisposableBean, ApplicationContextAware {

  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  protected ApplicationContext applicationContext;
  protected ProcessEngine processEngine;

  public void destroy() throws Exception {
    if (processEngine != null) {
      processEngine.close();
    }
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public ProcessEngine getObject() throws Exception {
    configureExpressionManager();
    configureExternallyManagedTransactions();

    if (processEngineConfiguration.getBeans() == null) {
      processEngineConfiguration.setBeans(new SpringBeanFactoryProxyMap(applicationContext));
    }

    this.processEngine = processEngineConfiguration.buildProcessEngine();
    return this.processEngine;
  }

  protected void configureExpressionManager() {
    if (processEngineConfiguration.getExpressionManager() == null && applicationContext != null) {
      processEngineConfiguration.setExpressionManager(new SpringExpressionManager(applicationContext, processEngineConfiguration.getBeans()));
    }
  }

  protected void configureExternallyManagedTransactions() {
    if (processEngineConfiguration instanceof SpringProcessEngineConfiguration) { // remark: any config can be injected, so we cannot have SpringConfiguration as member
      SpringProcessEngineConfiguration engineConfiguration = (SpringProcessEngineConfiguration) processEngineConfiguration;
      if (engineConfiguration.getTransactionManager() != null) {
        processEngineConfiguration.setTransactionsExternallyManaged(true);
      }
    }
  }

  public Class<ProcessEngine> getObjectType() {
    return ProcessEngine.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  public void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }
}
