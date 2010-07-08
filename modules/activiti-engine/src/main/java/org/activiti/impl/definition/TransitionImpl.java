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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.activiti.pvm.Condition;
import org.activiti.pvm.Listener;
import org.activiti.pvm.Transition;

/**
 * @author Tom Baeyens
 */
public class TransitionImpl implements Transition {

  protected String id;
  protected String name;

  ActivityImpl source;
  ActivityImpl destination;
  List<Listener> listeners;
  Condition condition;

  public void addEventListener(String eventId, Listener listener) {
    if (listeners==null) {
      listeners = new ArrayList<Listener>();
    }
    listeners.add(listener);
  }
  
  public List<Listener> getListeners() {
    if (listeners==null) {
      return Collections.emptyList();
    }
    return listeners;
  }

  public void setDestination(ActivityImpl destination) {
    this.destination = destination;
    destination.incomingTransitions.add(this);
  }
  
  public String toString() {
    StringBuilder text = new StringBuilder();
    text.append("(");
    text.append(source.getId());
    text.append(")--");
    if (name!=null) {
      text.append(name);
      text.append("-->");
    } else {
      text.append(">");
    }
    text.append("(");
    text.append(destination.getId());
    text.append(")");
    return text.toString();
  }

  // public getters and setters
  
  public ActivityImpl getSource() {
    return source;
  }
  public void setSource(ActivityImpl source) {
    this.source = source;
  }
  public ActivityImpl getDestination() {
    return destination;
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
  public Condition getCondition() {
    return condition;
  }
  public void setCondition(Condition condition) {
    this.condition = condition;
  }
}
