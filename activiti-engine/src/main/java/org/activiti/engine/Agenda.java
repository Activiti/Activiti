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
package org.activiti.engine;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandExecutor;

/**
 * For each API call (and thus {@link Command}) being executed, a new agenda instance is created.
 * On this agenda, operations are put, which the {@link CommandExecutor} will keep executing until
 * all are executed.
 *
 * The agenda also gives easy access to methods to plan new operations when writing
 * {@link ActivityBehavior} implementations.
 *
 * During a {@link Command} execution, the agenda can always be fetched using {@link Context#getAgenda()}.
 */
@Internal
public interface Agenda {

  boolean isEmpty();

  Runnable getNextOperation();

  /**
   * Generic method to plan a {@link Runnable}.
   */
  void planOperation(Runnable operation);

}
