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

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SequenceFlow extends FlowElement {

  protected String conditionExpression;
  protected String sourceRef;
  protected String targetRef;
  protected String skipExpression;

  // Actual flow elements that match the source and target ref
  // Set during process definition parsing
  @JsonIgnore
  protected FlowElement sourceFlowElement;
  
  @JsonIgnore
  protected FlowElement targetFlowElement;

  /**
   * Graphical information: a list of waypoints: x1, y1, x2, y2, x3, y3, ..
   * 
   * Added during parsing of a process definition.
   */
  protected List<Integer> waypoints = new ArrayList<Integer>();

  public SequenceFlow() {

  }

  public SequenceFlow(String sourceRef, String targetRef) {
    this.sourceRef = sourceRef;
    this.targetRef = targetRef;
  }

  public String getConditionExpression() {
    return conditionExpression;
  }

  public void setConditionExpression(String conditionExpression) {
    this.conditionExpression = conditionExpression;
  }

  public String getSourceRef() {
    return sourceRef;
  }

  public void setSourceRef(String sourceRef) {
    this.sourceRef = sourceRef;
  }

  public String getTargetRef() {
    return targetRef;
  }

  public void setTargetRef(String targetRef) {
    this.targetRef = targetRef;
  }

  public String getSkipExpression() {
    return skipExpression;
  }

  public void setSkipExpression(String skipExpression) {
    this.skipExpression = skipExpression;
  }

  public FlowElement getSourceFlowElement() {
    return sourceFlowElement;
  }

  public void setSourceFlowElement(FlowElement sourceFlowElement) {
    this.sourceFlowElement = sourceFlowElement;
  }

  public FlowElement getTargetFlowElement() {
    return targetFlowElement;
  }

  public void setTargetFlowElement(FlowElement targetFlowElement) {
    this.targetFlowElement = targetFlowElement;
  }

  public List<Integer> getWaypoints() {
    return waypoints;
  }

  public void setWaypoints(List<Integer> waypoints) {
    this.waypoints = waypoints;
  }

  public String toString() {
    return sourceRef + " --> " + targetRef;
  }

  public SequenceFlow clone() {
    SequenceFlow clone = new SequenceFlow();
    clone.setValues(this);
    return clone;
  }

  public void setValues(SequenceFlow otherFlow) {
    super.setValues(otherFlow);
    setConditionExpression(otherFlow.getConditionExpression());
    setSourceRef(otherFlow.getSourceRef());
    setTargetRef(otherFlow.getTargetRef());
    setSkipExpression(otherFlow.getSkipExpression());
  }
}
