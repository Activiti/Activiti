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

package org.activiti.form.engine.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.activiti.form.api.SubmittedForm;
import org.activiti.form.api.SubmittedFormQuery;
import org.activiti.form.engine.ActivitiFormIllegalArgumentException;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.engine.impl.interceptor.CommandExecutor;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class SubmittedFormQueryImpl extends AbstractQuery<SubmittedFormQuery, SubmittedForm> implements SubmittedFormQuery, Serializable {

  private static final long serialVersionUID = 1L;
  protected String id;
  protected Set<String> ids;
  protected String formId;
  protected String formIdLike;
  protected String taskId;
  protected String taskIdLike;
  protected String processInstanceId;
  protected String processInstanceIdLike;
  protected String processDefinitionId;
  protected String processDefinitionIdLike;
  protected Date submittedDate;
  protected Date submittedDateBefore;
  protected Date submittedDateAfter;
  protected String submittedBy;
  protected String submittedByLike;
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;

  public SubmittedFormQueryImpl() {
  }

  public SubmittedFormQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public SubmittedFormQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public SubmittedFormQueryImpl id(String id) {
    this.id = id;
    return this;
  }
  
  public SubmittedFormQueryImpl ids(Set<String> ids) {
    this.ids = ids;
    return this;
  }

  public SubmittedFormQueryImpl formId(String formId) {
    this.formId = formId;
    return this;
  }

  public SubmittedFormQueryImpl formIdLike(String formIdLike) {
    this.formIdLike = formIdLike;
    return this;
  }
  
  public SubmittedFormQueryImpl taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }
  
  public SubmittedFormQueryImpl taskIdLike(String taskIdLike) {
    this.taskIdLike = taskIdLike;
    return this;
  }
  
  public SubmittedFormQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public SubmittedFormQueryImpl processInstanceIdLike(String processInstanceIdLike) {
    this.processInstanceIdLike = processInstanceIdLike;
    return this;
  }
  
  public SubmittedFormQueryImpl processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public SubmittedFormQueryImpl processDefinitionIdLike(String processDefinitionIdLike) {
    this.processDefinitionIdLike = processDefinitionIdLike;
    return this;
  }
  
  public SubmittedFormQueryImpl submittedDate(Date submittedDate) {
    this.submittedDate = submittedDate;
    return this;
  }
  
  public SubmittedFormQueryImpl submittedDateBefore(Date submittedDateBefore) {
    this.submittedDateBefore = submittedDateBefore;
    return this;
  }
  
  public SubmittedFormQueryImpl submittedDateAfter(Date submittedDateAfter) {
    this.submittedDateAfter = submittedDateAfter;
    return this;
  }
  
  public SubmittedFormQueryImpl submittedBy(String submittedBy) {
    this.submittedBy = submittedBy;
    return this;
  }

  public SubmittedFormQueryImpl submittedByLike(String submittedByLike) {
    this.submittedByLike = submittedByLike;
    return this;
  }

  public SubmittedFormQueryImpl deploymentTenantId(String tenantId) {
    if (tenantId == null) {
      throw new ActivitiFormIllegalArgumentException("deploymentTenantId is null");
    }
    this.tenantId = tenantId;
    return this;
  }

  public SubmittedFormQueryImpl deploymentTenantIdLike(String tenantIdLike) {
    if (tenantIdLike == null) {
      throw new ActivitiFormIllegalArgumentException("deploymentTenantIdLike is null");
    }
    this.tenantIdLike = tenantIdLike;
    return this;
  }

  public SubmittedFormQueryImpl deploymentWithoutTenantId() {
    this.withoutTenantId = true;
    return this;
  }

  // sorting ////////////////////////////////////////////////////////

  public SubmittedFormQuery orderBySubmittedDate() {
    return orderBy(SubmittedFormQueryProperty.SUBMITTED_DATE);
  }

  public SubmittedFormQuery orderByTenantId() {
    return orderBy(SubmittedFormQueryProperty.TENANT_ID);
  }

  // results ////////////////////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext.getSubmittedFormEntityManager().findSubmittedFormCountByQueryCriteria(this);
  }

  @Override
  public List<SubmittedForm> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext.getSubmittedFormEntityManager().findSubmittedFormsByQueryCriteria(this, page);
  }

  // getters ////////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public Set<String> getIds() {
    return ids;
  }

  public String getFormId() {
    return formId;
  }

  public String getFormIdLike() {
    return formIdLike;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getTaskIdLike() {
    return taskIdLike;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessInstanceIdLike() {
    return processInstanceIdLike;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionIdLike() {
    return processDefinitionIdLike;
  }

  public Date getSubmittedDate() {
    return submittedDate;
  }

  public Date getSubmittedDateBefore() {
    return submittedDateBefore;
  }

  public Date getSubmittedDateAfter() {
    return submittedDateAfter;
  }

  public String getSubmittedBy() {
    return submittedBy;
  }

  public String getSubmittedByLike() {
    return submittedByLike;
  }
  
  public String getTenantId() {
    return tenantId;
  }

  public String getTenantIdLike() {
    return tenantIdLike;
  }

  public boolean isWithoutTenantId() {
    return withoutTenantId;
  }
}
