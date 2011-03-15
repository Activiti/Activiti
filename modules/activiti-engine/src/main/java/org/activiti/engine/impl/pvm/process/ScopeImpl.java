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

package org.activiti.engine.impl.pvm.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.data.IOSpecification;
import org.activiti.engine.impl.pvm.PvmException;
import org.activiti.engine.impl.pvm.PvmScope;


/**
 * @author Tom Baeyens
 */
public abstract class ScopeImpl extends ProcessElementImpl implements PvmScope {

  private static final long serialVersionUID = 1L;
  
  protected List<ActivityImpl> activities = new ArrayList<ActivityImpl>();
  protected Map<String, ActivityImpl> namedActivities = new HashMap<String, ActivityImpl>();
  protected Map<String, List<ExecutionListener>> executionListeners = new HashMap<String, List<ExecutionListener>>();
  protected IOSpecification ioSpecification;
  
  public ScopeImpl(String id, ProcessDefinitionImpl processDefinition) {
    super(id, processDefinition);
  }
  
  public ActivityImpl findActivity(String activityId) {
    ActivityImpl localActivity = namedActivities.get(activityId);
    if (localActivity!=null) {
      return localActivity;
    }
    for (ActivityImpl activity: activities) {
      ActivityImpl nestedActivity = activity.findActivity(activityId);
      if (nestedActivity!=null) {
        return nestedActivity;
      }
    }
    return null;
  }

  public ActivityImpl createActivity() {
    return createActivity(null);
  }

  public ActivityImpl createActivity(String activityId) {
    ActivityImpl activity = new ActivityImpl(activityId, processDefinition);
    if (activityId!=null) {
      if (processDefinition.findActivity(activityId) != null) {
        throw new PvmException("duplicate activity id '" + activityId + "'");
      }
      namedActivities.put(activityId, activity);
    }
    activity.setParent(this);
    activities.add(activity);
    return  activity;
  }

  public boolean contains(ActivityImpl activity) {
    if (namedActivities.containsKey(activity.getId())) {
      return true;
    }
    for (ActivityImpl nestedActivity : activities) {
      if (nestedActivity.contains(activity)) {
        return true;
      }
    }
    return false;
  }
  
  // event listeners //////////////////////////////////////////////////////////
  
  @SuppressWarnings("unchecked")
  public List<ExecutionListener> getExecutionListeners(String eventName) {
    List<ExecutionListener> executionListenerList = getExecutionListeners().get(eventName);
    if (executionListenerList!=null) {
      return executionListenerList;
    }
    return Collections.EMPTY_LIST;
  }
  
  public void addExecutionListener(String eventName, ExecutionListener executionListener) {
    addExecutionListener(eventName, executionListener, -1);
  }
  
  public void addExecutionListener(String eventName, ExecutionListener executionListener, int index) {
    List<ExecutionListener> listeners = executionListeners.get(eventName);
    if (listeners==null) {
      listeners = new ArrayList<ExecutionListener>();
      executionListeners.put(eventName, listeners);
    }
    if (index<0) {
      listeners.add(executionListener);
    } else {
      listeners.add(index, executionListener);
    }
  }
  
  public Map<String, List<ExecutionListener>> getExecutionListeners() {
    return executionListeners;
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public List<ActivityImpl> getActivities() {
    return activities;
  }

  public IOSpecification getIoSpecification() {
    return ioSpecification;
  }
  
  public void setIoSpecification(IOSpecification ioSpecification) {
    this.ioSpecification = ioSpecification;
  }
}
