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
package org.activiti.test.pvm.embeddedorder;

import java.lang.reflect.Field;

import org.activiti.engine.ActivitiException;
import org.activiti.impl.definition.ActivityImpl;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.pvm.ObjectProcessDefinition;


/**
 * @author Tom Baeyens
 */
public class StateManager {

  Object target;
  String stateFieldName;
  ProcessDefinitionImpl processDefinition;
  ExecutionImpl processInstance;
  
  public StateManager(Object target, String stateFieldName, ObjectProcessDefinition processDefinition) {
    this(target, stateFieldName, processDefinition, false);
  }

  public StateManager(Object target, String stateFieldName, ObjectProcessDefinition processDefinition, boolean initialize) {
    this.target = target;
    this.stateFieldName = stateFieldName;
    this.processDefinition = (ProcessDefinitionImpl) processDefinition;
    if (initialize) {
      processInstance = (ExecutionImpl) processDefinition.createProcessInstance();
      processInstance.setVariable("this", target);
      processInstance.start();
      serialize();
    }
  }

  void serialize() {
    // get the current activity id of the process instance
    String state = ((ExecutionImpl)processInstance).getActivity().getId();
    // update state memberfield
    try {
      Field field = target.getClass().getDeclaredField(stateFieldName);
      field.setAccessible(true);
      field.set(target, state);
    } catch (Exception e) {
      throw new ActivitiException("couldn't set field "+stateFieldName, e);
    }
  }

  void deserialize() {
    if (processInstance==null) {
      String state = null;
      try {
        Field field = target.getClass().getDeclaredField(stateFieldName);
        field.setAccessible(true);
        state = (String) field.get(target);
      } catch (Exception e) {
        throw new ActivitiException("couldn't get field "+stateFieldName, e);
      }
      processInstance = processDefinition.createProcessInstance();
      ActivityImpl activity = processDefinition.getActivity(state);
      processInstance.setActivity(activity);
    }
  }

  public void event(String event) {
    deserialize();
    processInstance.event(event);
    serialize();
  }
}
