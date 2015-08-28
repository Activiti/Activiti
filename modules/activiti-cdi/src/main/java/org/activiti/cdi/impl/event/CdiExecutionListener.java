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
import org.activiti.cdi.annotation.event.BusinessProcessLiteral;
import org.activiti.cdi.annotation.event.EndActivityLiteral;
import org.activiti.cdi.annotation.event.StartActivityLiteral;
import org.activiti.cdi.annotation.event.TakeTransitionLiteral;
import org.activiti.cdi.impl.util.BeanManagerLookup;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * Generic {@link ExecutionListener} publishing events using the cdi event
 * infrastructure.
 * 
 * @author Daniel Meyer
 */
public class CdiExecutionListener implements ExecutionListener, Serializable {

  private static final long serialVersionUID = 1L;
  
  protected final BusinessProcessEventType type;
  protected final String transitionName;
  protected final String activityId;

  public CdiExecutionListener(String transitionName) {
    this.type = BusinessProcessEventType.TAKE;
    this.transitionName = transitionName;
    this.activityId = null;
  }

  public CdiExecutionListener(String activityId, BusinessProcessEventType type) {
    this.type = type;
    this.transitionName = null;
    this.activityId = activityId;
  }

  @Override
  public void notify(DelegateExecution execution) throws Exception {    
    // test whether cdi is setup correclty. (if not, just do not deliver the event)    
    try {
      ProgrammaticBeanLookup.lookup(ProcessEngine.class);
    }catch (Exception e) {
      return;
    }
    
    BusinessProcessEvent event = createEvent(execution);
    Annotation[] qualifiers = getQualifiers(event);           
    getBeanManager().fireEvent(event, qualifiers);    
  }

  protected BusinessProcessEvent createEvent(DelegateExecution execution) {
    ProcessDefinition processDefinition = Context.getExecutionContext().getProcessDefinition();
    Date now = Context.getProcessEngineConfiguration().getClock().getCurrentTime();
    return new CdiBusinessProcessEvent(activityId, transitionName, processDefinition, execution, type, execution.getProcessInstanceId(), execution.getId(), now);
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
    if (type == BusinessProcessEventType.TAKE) {
      return new Annotation[] {businessProcessQualifier, new TakeTransitionLiteral(transitionName) };
    }
    if (type == BusinessProcessEventType.START_ACTIVITY) {
      return new Annotation[] {businessProcessQualifier, new StartActivityLiteral(activityId) };
    }
    if (type == BusinessProcessEventType.END_ACTIVITY) {
      return new Annotation[] {businessProcessQualifier, new EndActivityLiteral(activityId) };
    }
    return new Annotation[] {};
  }
}
