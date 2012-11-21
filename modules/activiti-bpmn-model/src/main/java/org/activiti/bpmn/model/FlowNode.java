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
public class FlowNode extends FlowElement {

  protected List<SequenceFlow> incoming = new ArrayList<SequenceFlow>();
  protected List<SequenceFlow> outgoing = new ArrayList<SequenceFlow>();

  public List<SequenceFlow> getIncoming() {
    return incoming;
  }
  public void setIncoming(List<SequenceFlow> incoming) {
    this.incoming = incoming;
  }
  public List<SequenceFlow> getOutgoing() {
    return outgoing;
  }
  public void setOutgoing(List<SequenceFlow> outgoing) {
    this.outgoing = outgoing;
  }
}
