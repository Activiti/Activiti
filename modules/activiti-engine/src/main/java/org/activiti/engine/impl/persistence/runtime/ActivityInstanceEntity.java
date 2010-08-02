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

package org.activiti.engine.impl.persistence.runtime;

import org.activiti.engine.ActivityInstance;
import org.activiti.engine.impl.persistence.PersistentObject;
import org.activiti.pvm.impl.process.ActivityImpl;
import org.activiti.pvm.impl.runtime.ActivityInstanceImpl;
import org.activiti.pvm.impl.runtime.ScopeInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class ActivityInstanceEntity extends ActivityInstanceImpl implements ActivityInstance, PersistentObject {

  protected String id;
  protected String parentId;
  protected String processInstanceId;
  protected String processDefinitionId;
  
  public ActivityInstanceEntity(ActivityImpl activity, ScopeInstanceImpl parent) {
    super(activity, parent);
  }
  
  public void delete() {
  }
  
  public Object getPersistentState() {
    throw new UnsupportedOperationException("please implement me");
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  
  public String getParentId() {
    return parentId;
  }

  
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

}
