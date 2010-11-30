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

package org.activiti.engine.impl.history;

import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.util.ClockUtil;


/**
 * @author Tom Baeyens
 */
public class HistoricFormPropertyEntity extends HistoricDetailEntity implements HistoricFormProperty {

  protected String propertyId;
  protected String propertyValue;
  
  public HistoricFormPropertyEntity() {
  }

  public HistoricFormPropertyEntity(ExecutionEntity execution, String propertyId, String propertyValue) {
    this.processInstanceId = execution.getProcessInstanceId();
    this.executionId = execution.getId();
    this.propertyId = propertyId;
    this.propertyValue = propertyValue;
    this.time = ClockUtil.getCurrentTime();
  }

  public String getPropertyId() {
    return propertyId;
  }
  
  public void setPropertyId(String propertyId) {
    this.propertyId = propertyId;
  }
  
  public String getPropertyValue() {
    return propertyValue;
  }
  
  public void setPropertyValue(String propertyValue) {
    this.propertyValue = propertyValue;
  }
}
