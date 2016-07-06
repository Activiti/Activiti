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
package org.activiti5.engine.impl.persistence.entity;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti5.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti5.engine.impl.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class SuspendedJobEntity extends AbstractJobEntity {

  private static final long serialVersionUID = 1L;

  private static Logger log = LoggerFactory.getLogger(SuspendedJobEntity.class);
  
  public SuspendedJobEntity() {}

  public SuspendedJobEntity(AbstractJobEntity te) {
    this.id = te.getId();
    this.jobType = te.getJobType();
    this.revision = te.getRevision();
    this.jobHandlerConfiguration = te.getJobHandlerConfiguration();
    this.jobHandlerType = te.getJobHandlerType();
    this.isExclusive = te.isExclusive();
    this.duedate = te.getDuedate();
    this.repeat = te.getRepeat();
    this.retries = te.getRetries();
    this.endDate = te.getEndDate();
    this.executionId = te.getExecutionId();
    this.processInstanceId = te.getProcessInstanceId();
    this.processDefinitionId = te.getProcessDefinitionId();
    this.exceptionMessage = te.getExceptionMessage();
    setExceptionStacktrace(te.getExceptionStacktrace());

    // Inherit tenant
    this.tenantId = te.getTenantId();
  }
  
  public void insert() {
    Context.getCommandContext()
      .getDbSqlSession()
      .insert(this);
    
    // add link to execution
    if (executionId != null) {
      ExecutionEntity execution = Context.getCommandContext()
        .getExecutionEntityManager()
        .findExecutionById(executionId);
      
      // Inherit tenant if (if applicable)
      if (execution.getTenantId() != null) {
        setTenantId(execution.getTenantId());
      }
    }
    
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, this));
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, this));
    }
  }
  
  public void delete() {
    Context.getCommandContext()
      .getDbSqlSession()
      .delete(this);

    // Also delete the job's exception byte array
    exceptionByteArrayRef.delete();

    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, this));
    }
  }
}
