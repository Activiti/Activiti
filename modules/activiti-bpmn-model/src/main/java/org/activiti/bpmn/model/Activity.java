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
public abstract class Activity extends FlowNode {

  protected boolean asynchronous;
  protected boolean notExclusive;
  protected String defaultFlow;
  protected boolean forCompensation;
  protected MultiInstanceLoopCharacteristics loopCharacteristics;
  protected IOSpecification ioSpecification;
  protected List<DataAssociation> dataInputAssociations = new ArrayList<DataAssociation>();
  protected List<DataAssociation> dataOutputAssociations = new ArrayList<DataAssociation>();
  protected List<BoundaryEvent> boundaryEvents = new ArrayList<BoundaryEvent>();

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
  public boolean isForCompensation() {
    return forCompensation;
  }
  public void setForCompensation(boolean forCompensation) {
    this.forCompensation = forCompensation;
  }
  public List<BoundaryEvent> getBoundaryEvents() {
    return boundaryEvents;
  }
  public void setBoundaryEvents(List<BoundaryEvent> boundaryEvents) {
    this.boundaryEvents = boundaryEvents;
  }
  public String getDefaultFlow() {
    return defaultFlow;
  }
  public void setDefaultFlow(String defaultFlow) {
    this.defaultFlow = defaultFlow;
  }
  public MultiInstanceLoopCharacteristics getLoopCharacteristics() {
    return loopCharacteristics;
  }
  public void setLoopCharacteristics(MultiInstanceLoopCharacteristics loopCharacteristics) {
    this.loopCharacteristics = loopCharacteristics;
  }
  public IOSpecification getIoSpecification() {
    return ioSpecification;
  }
  public void setIoSpecification(IOSpecification ioSpecification) {
    this.ioSpecification = ioSpecification;
  }
  public List<DataAssociation> getDataInputAssociations() {
    return dataInputAssociations;
  }
  public void setDataInputAssociations(List<DataAssociation> dataInputAssociations) {
    this.dataInputAssociations = dataInputAssociations;
  }
  public List<DataAssociation> getDataOutputAssociations() {
    return dataOutputAssociations;
  }
  public void setDataOutputAssociations(List<DataAssociation> dataOutputAssociations) {
    this.dataOutputAssociations = dataOutputAssociations;
  }
  
  public void setValues(Activity otherActivity) {
    super.setValues(otherActivity);
    setAsynchronous(otherActivity.isAsynchronous());
    setNotExclusive(otherActivity.isNotExclusive());
    setDefaultFlow(otherActivity.getDefaultFlow());
    setForCompensation(otherActivity.isForCompensation());
    if (otherActivity.getLoopCharacteristics() != null) {
      setLoopCharacteristics(otherActivity.getLoopCharacteristics().clone());
    }
    if (otherActivity.getIoSpecification() != null) {
      setIoSpecification(otherActivity.getIoSpecification().clone());
    }
    
    dataInputAssociations = new ArrayList<DataAssociation>();
    if (otherActivity.getDataInputAssociations() != null && otherActivity.getDataInputAssociations().size() > 0) {
      for (DataAssociation association : otherActivity.getDataInputAssociations()) {
        dataInputAssociations.add(association.clone());
      }
    }
    
    dataOutputAssociations = new ArrayList<DataAssociation>();
    if (otherActivity.getDataOutputAssociations() != null && otherActivity.getDataOutputAssociations().size() > 0) {
      for (DataAssociation association : otherActivity.getDataOutputAssociations()) {
        dataOutputAssociations.add(association.clone());
      }
    }
    
    boundaryEvents = new ArrayList<BoundaryEvent>();
    if (otherActivity.getBoundaryEvents() != null && otherActivity.getBoundaryEvents().size() > 0) {
      for (BoundaryEvent event : otherActivity.getBoundaryEvents()) {
        boundaryEvents.add(event.clone());
      }
    }
  }
}
