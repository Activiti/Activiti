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
package org.activiti.impl.interceptor;

import org.activiti.impl.Cmd;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.tx.TransactionContext;


/**
 * @author Tom Baeyens
 */
public class TransactionContextInterceptor extends Interceptor {

  String processManagerFactoryName;
  
  public <T> T execute(Cmd<T> cmd, ProcessEngineImpl processEngine) {
    TransactionContext transactionContext = processEngine.createTransactionContext();
    boolean success = false;
    RuntimeException exception = null;
    try {
      T result = next.execute(cmd, processEngine);
      success = true;
      return result;
    } catch (RuntimeException e) {
      exception = e;
      throw e;
    } finally {
      if (success) {
        transactionContext.commit();
      } else {
        transactionContext.rollback(exception);        
      }
      transactionContext.close();        
    }
  }
}
