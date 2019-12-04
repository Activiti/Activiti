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

public class MessageEventDefinition extends EventDefinition {

  protected String messageRef;
  protected String messageExpression;
  protected String correlationKey;
  protected List<FieldExtension> fieldExtensions = new ArrayList<FieldExtension>();
  
  public List<FieldExtension> getFieldExtensions() {
    return fieldExtensions;
  }
  public void setFieldExtensions(List<FieldExtension> fieldExtensions) {
    this.fieldExtensions = fieldExtensions;
  }
  
  public String getMessageRef() {
    return messageRef;
  }

  public void setMessageRef(String messageRef) {
    this.messageRef = messageRef;
  }

  public String getMessageExpression() {
    return messageExpression;
  }

  public void setMessageExpression(String messageExpression) {
    this.messageExpression = messageExpression;
  }

  public String getCorrelationKey() {
      return correlationKey;
  }

  public void setCorrelationKey(String correlationKey) {
      this.correlationKey = correlationKey;
  }

  public MessageEventDefinition clone() {
    MessageEventDefinition clone = new MessageEventDefinition();
    clone.setValues(this);
    return clone;
  }

  public void setValues(MessageEventDefinition otherDefinition) {
    super.setValues(otherDefinition);
    setMessageRef(otherDefinition.getMessageRef());
    setMessageExpression(otherDefinition.getMessageExpression());
    setFieldExtensions(otherDefinition.getFieldExtensions());
    setCorrelationKey(otherDefinition.getCorrelationKey());
  }
}
