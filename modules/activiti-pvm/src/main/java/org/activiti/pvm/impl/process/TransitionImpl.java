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

import org.activiti.pvm.process.PvmTransition;




/**
 * @author Tom Baeyens
 */
public class TransitionImpl extends ProcessElementImpl implements PvmTransition {

  private static final long serialVersionUID = 1L;
  
  protected ActivityImpl source;
  protected ActivityImpl destination;

  public TransitionImpl(String id, ProcessDefinitionImpl processDefinition) {
    super(id, processDefinition);
  }

  public ActivityImpl getSource() {
    return source;
  }

  public void setDestination(ActivityImpl destination) {
    this.destination = destination;
    destination.getIncomingTransitions().add(this);
  }
  
  protected void setSource(ActivityImpl source) {
    this.source = source;
  }

  public ActivityImpl getDestination() {
    return destination;
  }
  
  public String toString() {
    return "("+source.getId()+")--"+(id!=null?id+"-->(":">(")+destination.getId()+")";
  }
}
