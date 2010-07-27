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

package org.activiti.pvm.impl.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.pvm.impl.process.ActivityImpl;
import org.activiti.pvm.impl.process.ProcessDefinitionImpl;
import org.activiti.pvm.impl.process.ScopeImpl;



/**
 * @author Tom Baeyens
 */
public class ScopeInstanceImpl {

  protected ProcessDefinitionImpl processDefinition;
  protected ScopeImpl scope;
  protected Set<ActivityInstanceImpl> activityInstances = new HashSet<ActivityInstanceImpl>();
  protected Map<String, Object> variables;

  public ScopeInstanceImpl(ProcessDefinitionImpl processDefinition, ScopeImpl scope) {
    this.processDefinition = processDefinition;
    this.scope = scope;
  }

  protected ActivityInstanceImpl createActivityInstance(ActivityImpl activity) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl(activity, this);
    activityInstances.add(activityInstance);
    return activityInstance;
  }

  public void removeActivityInstance(ActivityInstanceImpl activityInstance) {
    activityInstances.remove(activityInstance);
    activityInstance.setParent(null);
  }
  
  public List<ActivityInstanceImpl> findActivityInstances(String activityId) {
    List<ActivityInstanceImpl> foundActivityInstances = new ArrayList<ActivityInstanceImpl>();
    collectActivityInstances(foundActivityInstances, activityId);
    return foundActivityInstances;
  }
  
  public ActivityInstanceImpl findActivityInstance(String activityId) {
    List<ActivityInstanceImpl> activityInstances = findActivityInstances(activityId);
    if (activityInstances.isEmpty()) {
      return null;
    }
    return activityInstances.get(0);
  }

  protected void collectActivityInstances(List<ActivityInstanceImpl> activityInstanceCollection, String activityId) {
    if (activityId==null) {
      throw new RuntimeException("activitId is null");
    }
    for (ActivityInstanceImpl activityInstance: activityInstances) {
      if (activityId.equals(activityInstance.getActivity().getId())) {
        activityInstanceCollection.add(activityInstance);
      }
      activityInstance.collectActivityInstances(activityInstanceCollection, activityId);
    }
  }
  
  // variables ////////////////////////////////////////////////////////////////
  
  public void setVariable(String variableName, Object value) {
    if (variables==null) {
      variables = new HashMap<String, Object>();
    }
    variables.put(variableName, value);
  }

  public Object getVariable(String variableName) {
    if (variables==null) {
      return null;
    }
    return variables.get(variableName);
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getVariables() {
    if (variables==null) {
      return Collections.EMPTY_MAP;
    }
    return variables;
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public ScopeImpl getScope() {
    return scope;
  }

  
  public void setScope(ScopeImpl scope) {
    this.scope = scope;
  }

  
  public Set<ActivityInstanceImpl> getActivityInstances() {
    return activityInstances;
  }

  
  public void setActivityInstances(Set<ActivityInstanceImpl> activityInstances) {
    this.activityInstances = activityInstances;
  }

  
  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }

  
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }
}
