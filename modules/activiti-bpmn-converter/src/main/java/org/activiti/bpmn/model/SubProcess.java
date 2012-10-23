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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tijs Rademakers
 */
public class SubProcess extends Activity {

  protected Map<String, FlowElement> flowElementMap = new HashMap<String, FlowElement>();
  protected List<Artifact> artifacts = new ArrayList<Artifact>();

  public FlowElement getFlowElement(String id) {
    return flowElementMap.get(id);
  }
  
  public Collection<FlowElement> getFlowElements() {
    return flowElementMap.values();
  }
  
  public void addFlowElement(FlowElement element) {
    flowElementMap.put(element.getId(), element);
  }
  
  public List<Artifact> getArtifacts() {
    return artifacts;
  }

  public void setArtifacts(List<Artifact> artifacts) {
    this.artifacts = artifacts;
  }
}
