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
import java.util.List;

import org.activiti.impl.calendar.BusinessCalendar;
import org.activiti.impl.timer.TimerDeclarationImpl;
import org.activiti.pvm.Activity;
import org.activiti.pvm.ActivityBehavior;

/**
 * @author Tom Baeyens
 */
public class ActivityImpl extends ScopeElementImpl implements Activity {

  private static final long serialVersionUID = 1L;
  
  protected ScopeElementImpl parent;
  protected List<TransitionImpl> outgoingTransitions = new ArrayList<TransitionImpl>();
  protected List<TransitionImpl> incomingTransitions = new ArrayList<TransitionImpl>();
  protected ActivityBehavior activityBehavior;
  protected boolean isScope = false;
  protected FormReference formReference;
  protected String type;

  protected ActivityImpl() {
  }
  
  public TransitionImpl createTransition() {
    TransitionImpl transition = new TransitionImpl();
    outgoingTransitions.add(transition);
    transition.setSource(this);
    return transition;
  }

  public TimerDeclarationImpl createTimerDeclaration(BusinessCalendar businessCalendar, String duedate, String jobHandlerType) {
    setScope(true);
    return super.createTimerDeclaration(businessCalendar, duedate, jobHandlerType);
  }

  public VariableDeclarationImpl createVariableDeclaration(String name, String type) {
    setScope(true);
    return super.createVariableDeclaration(name, type);
  }

  // restricted setters
  
  void setParent(ScopeElementImpl parent) {
    this.parent = parent;
  }

  public TransitionImpl findOutgoingTransition(String transitionId) {
    for (TransitionImpl transition : outgoingTransitions) {
      if ( ( (transitionId==null)
             && (transition.getId()==null)
           ) 
           || (transitionId.equals(transition.getId()))
         ) {
        return transition;
      }
    }
    return null;
  }
  
  public TransitionImpl getDefaultOutgoingTransition() {
    if (outgoingTransitions.isEmpty()) {
      return null;
    }
    return outgoingTransitions.get(0);
  }

  public ActivityImpl getParentActivity() {
    return (parent instanceof ActivityImpl ? (ActivityImpl)parent : null);
  }

  public String toString() {
    return "activity[" + getId() + "]";
  }

  // public getters and setters

  public ScopeElementImpl getParent() {
    return parent;
  }
  public ActivityBehavior getActivityBehavior() {
    return activityBehavior;
  }
  public void setActivityBehavior(ActivityBehavior activityBehavior) {
    this.activityBehavior = activityBehavior;
  }
  public List<TransitionImpl> getOutgoingTransitions() {
    return outgoingTransitions;
  }
  public List<TransitionImpl> getIncomingTransitions() {
    return incomingTransitions;
  }
  public boolean isScope() {
    return isScope;
  }
  public void setScope(boolean isScope) {
    this.isScope = isScope;
  }
  public FormReference getFormReference() {
    return formReference;
  }
  public void setFormReference(FormReference formReference) {
    this.formReference = formReference;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
}
