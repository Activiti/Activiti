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

package org.activiti.engine.delegate;

import org.activiti.bpmn.model.Task;

import java.util.Map;

/**
 * Callback interface to be notified of transaction events.
 *

 */
public interface TransactionDependentTaskListener extends BaseTaskListener {

  String ON_TRANSACTION_COMMITTING = "before-commit";
  String ON_TRANSACTION_COMMITTED = "committed";
  String ON_TRANSACTION_ROLLED_BACK = "rolled-back";

  void notify(String processInstanceId, String executionId, Task task,
          Map<String, Object> executionVariables, Map<String, Object> customPropertiesMap);
}
