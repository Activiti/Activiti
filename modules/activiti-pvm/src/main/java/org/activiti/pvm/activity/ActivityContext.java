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

package org.activiti.pvm.activity;

import java.util.Map;

import org.activiti.pvm.event.EventContext;
import org.activiti.pvm.process.PvmActivity;
import org.activiti.pvm.process.PvmProcessDefinition;
import org.activiti.pvm.process.PvmTransition;
import org.activiti.pvm.runtime.PvmProcessInstance;


/**
 * @author Tom Baeyens
 */
public interface ActivityContext extends EventContext {
  
  // runtime structure methods
  DelegateActivityInstance getActivityInstance();

  // execution operations
  void take(PvmTransition transition);
  void executeActivity(PvmActivity startActivity);
  void endActivityInstance();
  void endProcessInstance();
  void keepAlive();
  PvmProcessInstance createSubProcessInstance(PvmProcessDefinition processDefinition);

  // user variables
  void setVariable(String variableName, Object value);
  Object getVariable(String variableName);
  Map<String, Object> getVariables();

  // scoped activity variables
  Object getSystemVariable(String variableName);
  void setSystemVariable(String variableName, Object value);

  // activity and whole process definition model
  PvmActivity getActivity();
}
