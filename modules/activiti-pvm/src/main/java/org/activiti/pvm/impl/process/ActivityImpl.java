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
import java.util.List;

import org.activiti.pvm.activity.ActivityBehavior;
import org.activiti.pvm.process.PvmActivity;



/**
 * @author Tom Baeyens
 */
public class ActivityImpl extends ScopeImpl implements PvmActivity {

  protected List<TransitionImpl> outgoingTransitions = new ArrayList<TransitionImpl>();
  protected List<TransitionImpl> incomingTransitions = new ArrayList<TransitionImpl>();
  protected ActivityBehavior activityBehaviour;
  protected ScopeImpl parent;
  
  public TransitionImpl createOutgoingTransition() {
    TransitionImpl transition = new TransitionImpl();
    transition.setSource(this);
    outgoingTransitions.add(transition);
    return transition;
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

  public List<TransitionImpl> getOutgoingTransitions() {
    return outgoingTransitions;
  }

  public ActivityBehavior getActivityBehaviour() {
    return activityBehaviour;
  }

  public void setActivityBehaviour(ActivityBehavior activityBehaviour) {
    this.activityBehaviour = activityBehaviour;
  }

  public ScopeImpl getParent() {
    return parent;
  }

  public List<TransitionImpl> getIncomingTransitions() {
    return incomingTransitions;
  }
}
