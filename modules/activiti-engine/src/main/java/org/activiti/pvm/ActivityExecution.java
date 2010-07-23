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

import org.activiti.ProcessDefinition;
import org.activiti.ProcessInstance;


/**
 * @author Tom Baeyens
 */
public interface ActivityExecution {
  
  Activity getActivity();
  List<Transition> getOutgoingTransitions();
  List<Transition> getIncomingTransitions();
  Transition getDefaultOutgoingTransition();
  List<Activity> getActivities();

  void takeDefaultOutgoingTransition();
  void take(Transition transition);
  void take(String transitionId);
  
  Object getVariable(String variableName);
  void setVariable(String variableName, Object value);
  
  ActivityExecution createExecution();
  ObjectProcessInstance createSubProcessInstance(ProcessDefinition processDefinition);
  ActivityExecution getParent();
  List<? extends ActivityExecution> getExecutions();
  void end();
  void setActivity(Activity activity);

  void setActive(boolean isActive);
  boolean isActive();
  
  void setConcurrent(boolean isConcurrent);
  boolean isConcurrent();
  
  boolean isProcessInstance();
  
}
