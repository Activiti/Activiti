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
package org.activiti.engine.test.impl.logger;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;

/**

 */
public class DebugInfoExecutionDeleted extends AbstractDebugInfo {

  protected ExecutionEntity executionEntity;

  public DebugInfoExecutionDeleted(ExecutionEntity executionEntity) {
    this.executionEntity = executionEntity;
  }

  @Override
  public void printOut(Logger logger) {
    logger.info("Execution " + executionEntity.getId() + " deleted");
  }

}
