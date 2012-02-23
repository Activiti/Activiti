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

package org.activiti.engine.impl.bpmn.behavior;

import java.util.List;

import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;


/**
 * @author Daniel Meyer
 */
public class CancelBoundaryEventActivityBehavior  extends FlowNodeActivityBehavior {
    
  @Override
  public void execute(ActivityExecution execution) throws Exception {
           
      List<CompensateEventSubscriptionEntity> eventSubscriptions = ((ExecutionEntity)execution).getCompensateEventSubscriptions();
      
      if(eventSubscriptions.isEmpty()) {
        leave(execution);                
      } else {
        // cancel boundary is always sync
        ScopeUtil.throwCompensationEvent(eventSubscriptions, execution, false);        
      }
        
    
  }
  
  @Override
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    // join compensating executions    
    if(execution.getExecutions().isEmpty()) {
      leave(execution);   
    } else {      
      ((ExecutionEntity)execution).forceUpdate();  
    }
  }
  

}
