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

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Dave Syer
 * @author Tom Baeyens
 */
public class SpringTransactionInterceptor extends CommandInterceptor {
  
  protected PlatformTransactionManager transactionManager;
  protected int transactionPropagation;
  
  public SpringTransactionInterceptor(PlatformTransactionManager transactionManager, int transactionPropagation) {
    this.transactionManager = transactionManager;
    this.transactionPropagation = transactionPropagation;
  }
  
  @SuppressWarnings("unchecked")
  public <T> T execute(final Command<T> command) {
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setPropagationBehavior(transactionPropagation);
    T result = (T) transactionTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        return next.execute(command);
      }
    });
    return result;
  }
}