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
package org.activiti.engine.delegate.event.impl;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiMessageEvent;
import org.activiti.engine.delegate.event.ActivitiSignalEvent;

/**
 * An {@link ActivitiSignalEvent} implementation.
 * 

 */
public class ActivitiMessageEventImpl extends ActivitiActivityEventImpl implements ActivitiMessageEvent {

  protected String messageName;
  protected String correlationKey;
  protected Object messageData;
  protected String businessKey;

  public ActivitiMessageEventImpl(ActivitiEventType type) {
    super(type);
  }

  public void setMessageName(String messageName) {
    this.messageName = messageName;
  }

  public String getMessageName() {
    return messageName;
  }

  public void setMessageData(Object messageData) {
    this.messageData = messageData;
  }

  public Object getMessageData() {
    return messageData;
  }

  public String getMessageCorrelationKey() {
    return correlationKey;
  }

  public void setMessageCorrelationKey(String correlationKey) {
      this.correlationKey = correlationKey;
  }

  public String getMessageBusinessKey() {
      return businessKey;
  }

  public void setMessageBusinessKey(String businessKey) {
      this.businessKey = businessKey;
  }
  
}
