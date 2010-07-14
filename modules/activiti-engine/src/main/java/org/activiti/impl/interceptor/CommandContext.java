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

import org.activiti.impl.msg.MessageSession;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.timer.TimerSession;
import org.activiti.impl.tx.TransactionContext;

/**
 * @author Agim Emruli
 */
public interface CommandContext {
  PersistenceSession getPersistenceSession();
  MessageSession getMessageSession();
  TimerSession getTimerSession();
  TransactionContext getTransactionContext();

  //TODO: Probably remove
  <T> T getSession(Class<T> sessionClass);

  void close();
  void exception(Throwable exception);
}
