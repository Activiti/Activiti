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

package org.activiti.pvm.impl.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.pvm.PvmException;
import org.activiti.pvm.event.EventListener;
import org.activiti.pvm.process.PvmScope;


/**
 * @author Tom Baeyens
 */
public abstract class ScopeImpl extends ProcessElementImpl implements PvmScope {

  private static final long serialVersionUID = 1L;
  
  protected List<ActivityImpl> activities = new ArrayList<ActivityImpl>();
  protected Map<String, ActivityImpl> namedActivities = new HashMap<String, ActivityImpl>();
  protected Map<String, List<EventListener>> eventListeners = new HashMap<String, List<EventListener>>();
  protected List<VariableDeclaration> variableDeclarations = new ArrayList<VariableDeclaration>();

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
  public List<EventListener> getEventListeners(String eventName) {
    List<EventListener> eventListenerList = getEventListeners().get(eventName);
    if (eventListenerList!=null) {
      return eventListenerList;
    }
    return Collections.EMPTY_LIST;
  }
  
  public void addEventListener(String eventName, EventListener eventListener) {
    List<EventListener> listeners = eventListeners.get(eventName);
    if (listeners==null) {
      listeners = new ArrayList<EventListener>();
      eventListeners.put(eventName, listeners);
    }
    listeners.add(eventListener);
  }
  
  @SuppressWarnings("unchecked")
  public Map<String, List<EventListener>> getEventListeners() {
    return eventListeners;
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public List<ActivityImpl> getActivities() {
    return activities;
  }

  public List<VariableDeclaration> getVariableDeclarations() {
    return variableDeclarations;
  }
}
