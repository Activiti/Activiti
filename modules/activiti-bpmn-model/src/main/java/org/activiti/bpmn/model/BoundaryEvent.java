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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Tijs Rademakers
 */
public class BoundaryEvent extends Event {

  @JsonIgnore
  protected Activity attachedToRef;
  protected String attachedToRefId;
  protected boolean cancelActivity = true;

  public Activity getAttachedToRef() {
    return attachedToRef;
  }
  public void setAttachedToRef(Activity attachedToRef) {
    this.attachedToRef = attachedToRef;
  }
  public String getAttachedToRefId() {
    return attachedToRefId;
  }
  public void setAttachedToRefId(String attachedToRefId) {
    this.attachedToRefId = attachedToRefId;
  }
  public boolean isCancelActivity() {
    return cancelActivity;
  }
  public void setCancelActivity(boolean cancelActivity) {
    this.cancelActivity = cancelActivity;
  }
  
  public BoundaryEvent clone() {
    BoundaryEvent clone = new BoundaryEvent();
    clone.setValues(this);
    return clone;
  }
  
  public void setValues(BoundaryEvent otherEvent) {
    super.setValues(otherEvent);
    setAttachedToRefId(otherEvent.getAttachedToRefId());
    setAttachedToRef(otherEvent.getAttachedToRef());
    setCancelActivity(otherEvent.isCancelActivity());
  }
}
