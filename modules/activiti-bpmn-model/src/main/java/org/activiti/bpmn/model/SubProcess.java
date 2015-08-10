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
  protected List<ValuedDataObject> dataObjects = new ArrayList<ValuedDataObject>();

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
  
  public SubProcess clone() {
    SubProcess clone = new SubProcess();
    clone.setValues(this);
    return clone;
  }
  
  public void setValues(SubProcess otherElement) {
    super.setValues(otherElement);

    /*
     * This is required because data objects in Designer have no DI info
     * and are added as properties, not flow elements
     *
     * Determine the differences between the 2 elements' data object
     */
    for (ValuedDataObject thisObject : getDataObjects()) {
      boolean exists = false;
      for (ValuedDataObject otherObject : otherElement.getDataObjects()) {
        if (thisObject.getId().equals(otherObject.getId())) {
          exists = true;
        }
      }
      if (!exists) {
        // missing object
        removeFlowElement(thisObject.getId());
      }
    }
    
    dataObjects = new ArrayList<ValuedDataObject>();
    if (otherElement.getDataObjects() != null && !otherElement.getDataObjects().isEmpty()) {
      for (ValuedDataObject dataObject : otherElement.getDataObjects()) {
          ValuedDataObject clone = dataObject.clone();
          dataObjects.add(clone);
          // add it to the list of FlowElements
          // if it is already there, remove it first so order is same as data object list
          removeFlowElement(clone.getId());
          addFlowElement(clone);
      }
    }
    
    flowElementList.clear();
    for (FlowElement flowElement : otherElement.getFlowElements()) {
      addFlowElement(flowElement);
    }
    
    artifactList.clear();
    for (Artifact artifact : otherElement.getArtifacts()) {
      addArtifact(artifact);
    }
  }
  
  public List<ValuedDataObject> getDataObjects() {
    return dataObjects;
  }

  public void setDataObjects(List<ValuedDataObject> dataObjects) {
    this.dataObjects = dataObjects;
  }
}
