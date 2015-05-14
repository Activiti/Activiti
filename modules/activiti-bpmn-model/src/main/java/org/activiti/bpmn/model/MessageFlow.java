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

/**
 * @author Tijs Rademakers
 */
public class MessageFlow extends BaseElement {

  protected String name;
  protected String sourceRef;
  protected String targetRef;
  protected String messageRef;
  
  public MessageFlow() {
  	
  }
  
  public MessageFlow(String sourceRef, String targetRef) {
  	this.sourceRef = sourceRef;
  	this.targetRef = targetRef;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
  
  public String getMessageRef() {
    return messageRef;
  }

  public void setMessageRef(String messageRef) {
    this.messageRef = messageRef;
  }

  public String toString() {
    return sourceRef + " --> " + targetRef;
  }
  
  public MessageFlow clone() {
    MessageFlow clone = new MessageFlow();
    clone.setValues(this);
    return clone;
  }
  
  public void setValues(MessageFlow otherFlow) {
    super.setValues(otherFlow);
    setName(otherFlow.getName());
    setSourceRef(otherFlow.getSourceRef());
    setTargetRef(otherFlow.getTargetRef());
    setMessageRef(otherFlow.getMessageRef());
  }
}
