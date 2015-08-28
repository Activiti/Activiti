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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmException;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;


/**
 * @author Tom Baeyens
 */
public class ActivityImpl extends ScopeImpl implements PvmActivity, HasDIBounds {

  private static final long serialVersionUID = 1L;
  protected List<TransitionImpl> outgoingTransitions = new ArrayList<TransitionImpl>();
  protected Map<String, TransitionImpl> namedOutgoingTransitions = new HashMap<String, TransitionImpl>();
  protected Map<String, Object> variables;
  protected List<TransitionImpl> incomingTransitions = new ArrayList<TransitionImpl>();
  protected ActivityBehavior activityBehavior;
  protected ScopeImpl parent;
  protected boolean isScope;
  protected boolean isAsync;
  protected boolean isExclusive;
  protected String failedJobRetryTimeCycleValue;
  
  // Graphical information
  protected int x = -1;
  protected int y = -1;
  protected int width = -1;
  protected int height = -1;
  
  public ActivityImpl(String id, ProcessDefinitionImpl processDefinition) {
    super(id, processDefinition);
  }
  
  public String getFailedJobRetryTimeCycleValue() {
		return failedJobRetryTimeCycleValue;
  }
  
  public void setFailedJobRetryTimeCycleValue(String failedJobRetryTimeCycleValue) {
	  this.failedJobRetryTimeCycleValue = failedJobRetryTimeCycleValue;
  }
  
  public TransitionImpl createOutgoingTransition() {
    return createOutgoingTransition(null);
  }
  
  public TransitionImpl createOutgoingTransition(String transitionId) {
    return createOutgoingTransition(transitionId, null);
  }

  public TransitionImpl createOutgoingTransition(String transitionId, Expression skipExpression) {
    TransitionImpl transition = new TransitionImpl(transitionId, skipExpression, processDefinition);
    transition.setSource(this);
    outgoingTransitions.add(transition);
    
    if (transitionId != null) {
      if (namedOutgoingTransitions.containsKey(transitionId)) {
        throw new PvmException("activity '"+id+" has duplicate transition '"+transitionId+"'");
      }
      namedOutgoingTransitions.put(transitionId, transition);
    }
    
    return transition;
  }
  
  public TransitionImpl findOutgoingTransition(String transitionId) {
    return namedOutgoingTransitions.get(transitionId);
  }
  
  public String toString() {
    return "Activity("+id+")";
  }
  
  public ActivityImpl getParentActivity() {
    if (parent instanceof ActivityImpl) {
      return (ActivityImpl) parent;
    }
    return null;
  }


  // restricted setters ///////////////////////////////////////////////////////
  
  protected void setOutgoingTransitions(List<TransitionImpl> outgoingTransitions) {
    this.outgoingTransitions = outgoingTransitions;
  }

  protected void setParent(ScopeImpl parent) {
    this.parent = parent;
  }

  protected void setIncomingTransitions(List<TransitionImpl> incomingTransitions) {
    this.incomingTransitions = incomingTransitions;
  }

  // getters and setters //////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public List<PvmTransition> getOutgoingTransitions() {
    return (List) outgoingTransitions;
  }

  public ActivityBehavior getActivityBehavior() {
    return activityBehavior;
  }

  public void setActivityBehavior(ActivityBehavior activityBehavior) {
    this.activityBehavior = activityBehavior;
  }

  public ScopeImpl getParent() {
    return parent;
  }

  @SuppressWarnings("unchecked")
  public List<PvmTransition> getIncomingTransitions() {
    return (List) incomingTransitions;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }

  public boolean isScope() {
    return isScope;
  }

  public void setScope(boolean isScope) {
    this.isScope = isScope;
  }

  public int getX() {
    return x;
  }
  
  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }
  
  public boolean isAsync() {
    return isAsync;
  }
  
  public void setAsync(boolean isAsync) {
    this.isAsync = isAsync;
  }
  
  public boolean isExclusive() {
    return isExclusive;
  }
    
  public void setExclusive(boolean isExclusive) {
    this.isExclusive = isExclusive;
  }
}
