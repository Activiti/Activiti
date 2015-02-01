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
import java.util.List;

import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.pvm.PvmTransition;


/**
 * @author Tom Baeyens
 */
public class TransitionImpl extends ProcessElementImpl implements PvmTransition {

  private static final long serialVersionUID = 1L;
  
  protected ActivityImpl source;
  protected ActivityImpl destination;
  protected List<ExecutionListener> executionListeners;
  protected Expression skipExpression;
  
  /** Graphical information: a list of waypoints: x1, y1, x2, y2, x3, y3, .. */
  protected List<Integer> waypoints = new ArrayList<Integer>();

  public TransitionImpl(String id, Expression skipExpression, ProcessDefinitionImpl processDefinition) {
    super(id, processDefinition);
    this.skipExpression = skipExpression;
  }

  public ActivityImpl getSource() {
    return source;
  }

  public void setDestination(ActivityImpl destination) {
    this.destination = destination;
    destination.getIncomingTransitions().add(this);
  }
  
  public void addExecutionListener(ExecutionListener executionListener) {
    if (executionListeners==null) {
      executionListeners = new ArrayList<ExecutionListener>();
    }
    executionListeners.add(executionListener);
  }

  public String toString() {
    return "("+source.getId()+")--"+(id!=null?id+"-->(":">(")+destination.getId()+")";
  }

  @SuppressWarnings("unchecked")
  public List<ExecutionListener> getExecutionListeners() {
    if (executionListeners==null) {
      return Collections.EMPTY_LIST;
    }
    return executionListeners;
  }

  // getters and setters //////////////////////////////////////////////////////

  protected void setSource(ActivityImpl source) {
    this.source = source;
  }

  public ActivityImpl getDestination() {
    return destination;
  }
  
  public void setExecutionListeners(List<ExecutionListener> executionListeners) {
    this.executionListeners = executionListeners;
  }

  public List<Integer> getWaypoints() {
    return waypoints;
  }
  
  public void setWaypoints(List<Integer> waypoints) {
    this.waypoints = waypoints;
  }

  public Expression getSkipExpression() {
    return skipExpression;
  }
  
  public void setSkipExpression(Expression skipExpression) {
    this.skipExpression = skipExpression;
  }
}
