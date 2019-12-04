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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.AbstractCommandInterceptor;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**


 */
public class SpringTransactionInterceptor extends AbstractCommandInterceptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(SpringTransactionInterceptor.class);

  protected PlatformTransactionManager transactionManager;

  public SpringTransactionInterceptor(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public <T> T execute(final CommandConfig config, final Command<T> command) {
    LOGGER.debug("Running command with propagation {}", config.getTransactionPropagation());

    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setPropagationBehavior(getPropagation(config));

    T result = transactionTemplate.execute(new TransactionCallback<T>() {
      public T doInTransaction(TransactionStatus status) {
        return next.execute(config, command);
      }
    });

    return result;
  }

  private int getPropagation(CommandConfig config) {
    switch (config.getTransactionPropagation()) {
    case NOT_SUPPORTED:
      return TransactionTemplate.PROPAGATION_NOT_SUPPORTED;
    case REQUIRED:
      return TransactionTemplate.PROPAGATION_REQUIRED;
    case REQUIRES_NEW:
      return TransactionTemplate.PROPAGATION_REQUIRES_NEW;
    default:
      throw new ActivitiIllegalArgumentException("Unsupported transaction propagation: " + config.getTransactionPropagation());
    }
  }
}