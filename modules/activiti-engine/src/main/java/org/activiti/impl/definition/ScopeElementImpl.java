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
package org.activiti.impl.definition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.impl.calendar.BusinessCalendar;
import org.activiti.impl.timer.TimerDeclarationImpl;
import org.activiti.pvm.Listener;

/**
 * @author Tom Baeyens
 */
public class ScopeElementImpl implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;

  protected String name;

  protected Map<String, ActivityImpl> activities = new HashMap<String, ActivityImpl>();

  protected List<VariableDeclarationImpl> variableDeclarations = new ArrayList<VariableDeclarationImpl>();
  protected List<TimerDeclarationImpl> timerDeclarations = new ArrayList<TimerDeclarationImpl>();

  protected ProcessDefinitionImpl processDefinition;

  protected Map<String, List<Listener>> eventListeners;

  public VariableDeclarationImpl createVariableDeclaration(String name, String type) {
    VariableDeclarationImpl variableDeclaration = new VariableDeclarationImpl();
    variableDeclaration.setName(name);
    variableDeclaration.setType(type);      
    variableDeclarations.add(variableDeclaration);
    return variableDeclaration;
  }

  public TimerDeclarationImpl createTimerDeclaration(BusinessCalendar businessCalendar, String duedate, String jobHandlerType) {
    TimerDeclarationImpl timerDeclaration = new TimerDeclarationImpl(businessCalendar, duedate, jobHandlerType);
    timerDeclarations.add(timerDeclaration);
    return timerDeclaration;
  }

  public ActivityImpl createActivity(String id) {
    ActivityImpl activity = new ActivityImpl();
    activity.setId(id);
    activities.put(id, activity);
    activity.setParent(this);
    activity.setProcessDefinition(getProcessDefinition());
    return activity;
  }

  public void addEventListener(String eventId, Listener listener) {
    if (eventListeners == null) {
      eventListeners = new HashMap<String, List<Listener>>();
    }
    List<Listener> listeners = eventListeners.get(eventId);
    if (listeners == null) {
      listeners = new ArrayList<Listener>();
      eventListeners.put(eventId, listeners);
    }
    listeners.add(listener);
  }

  public List<Listener> getEventListeners(String eventId) {
    if (eventListeners == null) {
      return Collections.EMPTY_LIST;
    }
    List<Listener> listeners = eventListeners.get(eventId);
    if (listeners == null) {
      return Collections.EMPTY_LIST;
    }
    return listeners;
  }

  /** searches for a contained activity recursively */
  public ActivityImpl findActivity(String id) {
    ActivityImpl activity = activities.get(id);
    if (activity != null) {
      return activity;
    }
    for (ActivityImpl nestedActivity : activities.values()) {
      activity = nestedActivity.findActivity(id);
      if (activity != null) {
        return activity;
      }
    }
    return null;
  }

  public boolean contains(ActivityImpl destination) {
    if (activities.containsKey(destination.getId())) {
      return true;
    }
    for (ActivityImpl nestedActivity : activities.values()) {
      if (nestedActivity.contains(destination)) {
        return true;
      }
    }
    return false;
  }

  // restricted setters

  void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }

  // public getters and setters

  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }
  public ActivityImpl getActivity(String name) {
    return activities.get(name);
  }
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public List<VariableDeclarationImpl> getVariableDeclarations() {
    return variableDeclarations;
  }
  public List<TimerDeclarationImpl> getTimerDeclarations() {
    return timerDeclarations;
  }
}
