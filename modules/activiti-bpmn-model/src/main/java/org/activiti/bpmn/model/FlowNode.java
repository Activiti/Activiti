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
package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tijs Rademakers
 */
public abstract class FlowNode extends FlowElement {

  protected boolean asynchronous;
  protected boolean notExclusive;
  protected List<SequenceFlow> incomingFlows = new ArrayList<SequenceFlow>();
  protected List<SequenceFlow> outgoingFlows = new ArrayList<SequenceFlow>();

  public boolean isAsynchronous() {
    return asynchronous;
  }

  public void setAsynchronous(boolean asynchronous) {
    this.asynchronous = asynchronous;
  }
  
  public boolean isNotExclusive() {
    return notExclusive;
  }
  public void setNotExclusive(boolean notExclusive) {
    this.notExclusive = notExclusive;
  }

  public List<SequenceFlow> getIncomingFlows() {
    return incomingFlows;
  }

  public void setIncomingFlows(List<SequenceFlow> incomingFlows) {
    this.incomingFlows = incomingFlows;
  }

  public List<SequenceFlow> getOutgoingFlows() {
    return outgoingFlows;
  }

  public void setOutgoingFlows(List<SequenceFlow> outgoingFlows) {
    this.outgoingFlows = outgoingFlows;
  }
  
  public void setValues(FlowNode otherNode) {
    super.setValues(otherNode);
    setAsynchronous(otherNode.isAsynchronous());
    setNotExclusive(otherNode.isNotExclusive());
  }
}
