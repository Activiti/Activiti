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
package org.activiti.form.engine.impl.persistence.entity;

import java.util.Date;

import org.activiti.form.api.SubmittedForm;
import org.activiti.form.engine.impl.db.Entity;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public interface SubmittedFormEntity extends SubmittedForm, Entity {

  void setFormId(String formId);

  void setTaskId(String taskId);

  void setProcessInstanceId(String processInstanceId);

  void setProcessDefinitionId(String processDefinitionId);

  void setSubmittedDate(Date submittedDate);

  void setSubmittedBy(String submittedBy);
  
  void setFormValuesId(String formValuesId);

  void setTenantId(String tenantId);
  
  void setFormValueBytes(byte[] formValueBytes);
  
}
