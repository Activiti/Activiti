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
import java.util.List;

import org.apache.commons.lang3.StringUtils;


/**
 * @author Tijs Rademakers
 */
public class SubProcess extends Activity implements FlowElementsContainer {

  protected List<FlowElement> flowElementList = new ArrayList<FlowElement>();
  protected List<Artifact> artifactList = new ArrayList<Artifact>();

  public FlowElement getFlowElement(String id) {
    FlowElement foundElement = null;
    if (StringUtils.isNotEmpty(id)) {  
      for (FlowElement element : flowElementList) {
        if (id.equals(element.getId())) {
          foundElement = element;
          break;
        }
      }
    }
    return foundElement;
  }
  
  public Collection<FlowElement> getFlowElements() {
    return flowElementList;
  }
  
  public void addFlowElement(FlowElement element) {
    flowElementList.add(element);
  }
  
  public void removeFlowElement(String elementId) {
    FlowElement element = getFlowElement(elementId);
    if (element != null) {
      flowElementList.remove(element);
    }
  }
  
  public Artifact getArtifact(String id) {
    Artifact foundArtifact = null;
    for (Artifact artifact : artifactList) {
      if (id.equals(artifact.getId())) {
        foundArtifact = artifact;
        break;
      }
    }
    return foundArtifact;
  }
  
  public Collection<Artifact> getArtifacts() {
    return artifactList;
  }
  
  public void addArtifact(Artifact artifact) {
    artifactList.add(artifact);
  }
  
  public void removeArtifact(String artifactId) {
    Artifact artifact = getArtifact(artifactId);
    if (artifact != null) {
      artifactList.remove(artifact);
    }
  }
}
