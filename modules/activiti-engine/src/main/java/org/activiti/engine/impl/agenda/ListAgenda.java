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
package org.activiti.engine.impl.agenda;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.runtime.Agenda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**

 * @author Joram Barrez
 */
public class ListAgenda implements Agenda {

  private static final Logger logger = LoggerFactory.getLogger(ListAgenda.class);

  protected LinkedList<Runnable> operations = new LinkedList<Runnable>();

  @Override
  public boolean isEmpty() {
    return operations.isEmpty();
  }

  @Override
  public Runnable getNextOperation() {
    return operations.poll();
  }

  /**
   * Generic method to plan a {@link Runnable}.
   */
  @Override
  public void planOperation(Runnable operation) {
    planOperation(operation, null);
  }

  /**
   * Generic method to plan a {@link Runnable}.
   */
  @Override
  public void planOperation(Runnable operation, ExecutionEntity executionEntity) {
    operations.add(operation);
    logger.debug("Operation {} added to agenda", operation.getClass());

    if (executionEntity != null) {
      Context.getCommandContext().addInvolvedExecution(executionEntity);
    }
  }

}
