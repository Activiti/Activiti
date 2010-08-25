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
package org.activiti.engine.impl.cfg.spring;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Dave Syer
 * @author Tom Baeyens
 */
public class SpringTransactionInterceptor implements CommandInterceptor {
  
  protected PlatformTransactionManager transactionManager;
  
  public SpringTransactionInterceptor(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
  
  public <T> T invoke(final CommandExecutor next, final Command<T> command) {
    @SuppressWarnings("unchecked")
    T result = (T) new TransactionTemplate(transactionManager).execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        return next.execute(command);
      }
    });
    return result;
  }
}