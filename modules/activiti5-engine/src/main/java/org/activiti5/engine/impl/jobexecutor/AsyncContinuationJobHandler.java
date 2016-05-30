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
package org.activiti5.engine.impl.jobexecutor;

import org.activiti.engine.runtime.Job;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti5.engine.impl.pvm.runtime.AtomicOperation;

/**
 * 
 * @author Daniel Meyer
 */
public class AsyncContinuationJobHandler implements JobHandler {
  
  public final static String TYPE = "async-continuation";

  public String getType() {
    return TYPE;
  }
  
  public void execute(Job job, String configuration, ExecutionEntity execution, CommandContext commandContext) {
    // ATM only AtomicOperationTransitionCreateScope can be performed asynchronously 
    AtomicOperation atomicOperation = AtomicOperation.TRANSITION_CREATE_SCOPE;
    commandContext.performOperation(atomicOperation, execution);
  }

}
