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
public class Process extends BaseElement implements FlowElementsContainer, HasExecutionListeners {

  protected String name;
  protected boolean executable = true;
  protected String documentation;
  protected IOSpecification ioSpecification;
  protected List<ActivitiListener> executionListeners = new ArrayList<ActivitiListener>();
  protected List<Lane> lanes = new ArrayList<Lane>();
  protected List<FlowElement> flowElementList = new ArrayList<FlowElement>();
  protected Map<String, FlowElement> flowElementMap = new HashMap<String, FlowElement>(); // The flow elements are twice stored. The map is used for performance when retrieving by id 
  protected List<Artifact> artifactList = new ArrayList<Artifact>();
  protected List<String> candidateStarterUsers = new ArrayList<String>();
  protected List<String> candidateStarterGroups = new ArrayList<String>();

  public String getDocumentation() {
    return documentation;
  }

  public void setDocumentation(String documentation) {
    this.documentation = documentation;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isExecutable() {
    return executable;
  }

  public void setExecutable(boolean executable) {
    this.executable = executable;
  }

  public IOSpecification getIoSpecification() {
    return ioSpecification;
  }

  public void setIoSpecification(IOSpecification ioSpecification) {
    this.ioSpecification = ioSpecification;
  }

  public List<ActivitiListener> getExecutionListeners() {
    return executionListeners;
  }

  public void setExecutionListeners(List<ActivitiListener> executionListeners) {
    this.executionListeners = executionListeners;
  }

  public List<Lane> getLanes() {
    return lanes;
  }

  public void setLanes(List<Lane> lanes) {
    this.lanes = lanes;
  }
  
  public FlowElement getFlowElement(String flowElementId) {
    FlowElement flowElement = flowElementMap.get(flowElementId);
    
    // It could be the id has changed after insertion into the map,
    // So if it is not found, we loop through the list
    if (flowElement == null) {
     flowElement = findFlowElementInList(flowElementId);
    }
    
    return flowElement;
  }
  
  protected FlowElement findFlowElementInList(String flowElementId) {
    for (FlowElement f : flowElementList) {
      if (f.getId() != null && f.getId().equals(flowElementId)) {
        return f;
      }
    }
    return null;
  }
  
  public Collection<FlowElement> getFlowElements() {
    return flowElementList;
  }
  
  public void addFlowElement(FlowElement element) {
    flowElementList.add(element);
    flowElementMap.put(element.getId(), element);
  }
  
  public void removeFlowElement(String elementId) {
    FlowElement element = flowElementMap.get(elementId);
    
    if (element == null) {
      element = findFlowElementInList(elementId);
    }
    
    if (element != null) {
      flowElementList.remove(element);
      flowElementMap.remove(elementId);
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

  public List<String> getCandidateStarterUsers() {
    return candidateStarterUsers;
  }

  public void setCandidateStarterUsers(List<String> candidateStarterUsers) {
    this.candidateStarterUsers = candidateStarterUsers;
  }

  public List<String> getCandidateStarterGroups() {
    return candidateStarterGroups;
  }

  public void setCandidateStarterGroups(List<String> candidateStarterGroups) {
    this.candidateStarterGroups = candidateStarterGroups;
  }
  
  @SuppressWarnings("unchecked")
  public <FlowElementType extends FlowElement> List<FlowElementType> findFlowElementsOfType(Class<FlowElementType> type) {
    List<FlowElementType> flowElements = new ArrayList<FlowElementType>();
    for (FlowElement flowElement : this.getFlowElements()) {
      if (type.isInstance(flowElement)) {
        flowElements.add((FlowElementType) flowElement);
      }
    }
    return flowElements;
  }
  
}
