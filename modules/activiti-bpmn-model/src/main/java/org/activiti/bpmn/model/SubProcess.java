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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Tijs Rademakers
 */
public class SubProcess extends Activity {

  protected Map<String, FlowElement> flowElementMap = new HashMap<String, FlowElement>();
  protected Map<String, Artifact> artifactMap = new LinkedHashMap<String, Artifact>();

  public FlowElement getFlowElement(String id) {
    return flowElementMap.get(id);
  }
  
  public Collection<FlowElement> getFlowElements() {
    return flowElementMap.values();
  }
  
  public Map<String, FlowElement> getFlowElementMap() {
    return flowElementMap;
  }
  
  public void addFlowElement(FlowElement element) {
    flowElementMap.put(element.getId(), element);
  }
  
  public void removeFlowElement(String elementId) {
    flowElementMap.remove(elementId);
  }
  
  public Artifact getArtifact(String id) {
    return artifactMap.get(id);
  }
  
  public Collection<Artifact> getArtifacts() {
    return artifactMap.values();
  }
  
  public Map<String, Artifact> getArtifactMap() {
    return artifactMap;
  }
  
  public void addArtifact(Artifact artifact) {
    artifactMap.put(artifact.getId(), artifact);
  }
  
  public void removeArtifact(String artifactId) {
    artifactMap.remove(artifactId);
  }
}
