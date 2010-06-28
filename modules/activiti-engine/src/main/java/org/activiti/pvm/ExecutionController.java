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
package org.activiti.pvm;

import java.util.List;


/**
 * <b>Important: </b> this class is experimental, and is likely to change in newer releases.
 *  
 * controls concurrent executions for an ActivityExecution.
 * 
 * In case there is just 1 path of execution, a concurrency scope 
 * is represented by 1 execution.  In case there are multiple paths 
 * of execution, there is one parent and many concurrent children.
 * This interface provides convenience for dealing with those 
 * two situations.
 * 
 * The distinction between all executions and the active executions 
 * is created in order to simplify the implement of joins.  Concurrent 
 * executions that are ended will not be deleted/removed when they arrive,
 * but only when the parent is ended.  
 * 
 * 
 * @see ActivityExecution#getExecutionController()
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface ExecutionController {

  List<? extends ActivityExecution> getExecutions();
  
  List<? extends ActivityExecution> getActiveExecutions();
  
  ActivityExecution createExecution();
  
  void end();

  void setActive(boolean isActive);

  void setActivity(Activity activity);

}
