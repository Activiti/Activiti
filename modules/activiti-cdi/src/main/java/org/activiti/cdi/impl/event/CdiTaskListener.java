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
package org.activiti.cdi.impl.event;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Date;

import javax.enterprise.inject.spi.BeanManager;

import org.activiti.cdi.BusinessProcessEvent;
import org.activiti.cdi.BusinessProcessEventType;
import org.activiti.cdi.annotation.event.AssignTaskLiteral;
import org.activiti.cdi.annotation.event.BusinessProcessLiteral;
import org.activiti.cdi.annotation.event.CompleteTaskLiteral;
import org.activiti.cdi.annotation.event.CreateTaskLiteral;
import org.activiti.cdi.annotation.event.DeleteTaskLiteral;
import org.activiti.cdi.impl.util.BeanManagerLookup;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * Generic {@link TaskListener} publishing events using the cdi event
 * infrastructure.
 * 
 * @author Dimitris Mandalidis 
 */
public class CdiTaskListener implements TaskListener, Serializable {

  private static final long serialVersionUID = 1L;
  
  protected final BusinessProcessEventType type;
  protected final String transitionName;
  protected final String activityId;

  public CdiTaskListener(String transitionName) {
    this.type = BusinessProcessEventType.TAKE;
    this.transitionName = transitionName;
    this.activityId = null;
  }

  public CdiTaskListener(String activityId, BusinessProcessEventType type) {
    this.type = type;
    this.transitionName = null;
    this.activityId = activityId;
  }

  @Override
  public void notify(DelegateTask task) {    
    // test whether cdi is setup correclty. (if not, just do not deliver the event)    
    try {
      ProgrammaticBeanLookup.lookup(ProcessEngine.class);
    }catch (Exception e) {
      return;
    }
    
    BusinessProcessEvent event = createEvent(task);
    Annotation[] qualifiers = getQualifiers(event);           
    getBeanManager().fireEvent(event, qualifiers);    
  }

  protected BusinessProcessEvent createEvent(DelegateTask task) {
    ProcessDefinition processDefinition = Context.getProcessEngineConfiguration().getProcessDefinitionCache().get(task.getExecution().getProcessDefinitionId());
    Date now = Context.getProcessEngineConfiguration().getClock().getCurrentTime();
    return new CdiBusinessProcessEvent(activityId, transitionName, processDefinition, task, type, task.getExecution().getProcessInstanceId(), task.getExecutionId(), now);
  }

  protected BeanManager getBeanManager() {
    BeanManager bm = BeanManagerLookup.getBeanManager();
    if (bm == null) {
      throw new ActivitiException("No cdi bean manager avaiable, cannot publish event.");
    }
    return bm;
  }

  protected Annotation[] getQualifiers(BusinessProcessEvent event) {
    Annotation businessProcessQualifier = new BusinessProcessLiteral(event.getProcessDefinition().getKey());
    if (type == BusinessProcessEventType.CREATE_TASK) {
      return new Annotation[] {businessProcessQualifier, new CreateTaskLiteral(activityId) };
    }
    if (type == BusinessProcessEventType.ASSIGN_TASK) {
      return new Annotation[] {businessProcessQualifier, new AssignTaskLiteral(activityId) };
    }
    if (type == BusinessProcessEventType.COMPLETE_TASK) {
      return new Annotation[] {businessProcessQualifier, new CompleteTaskLiteral(activityId) };
    }
    if (type == BusinessProcessEventType.DELETE_TASK) {
      return new Annotation[] {businessProcessQualifier, new DeleteTaskLiteral(activityId) };
    }
    return new Annotation[] {};
  }
}
