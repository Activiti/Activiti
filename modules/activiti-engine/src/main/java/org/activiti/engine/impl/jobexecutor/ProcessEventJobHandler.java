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

package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;


/**
 * @author Daniel Meyer
 */
public class ProcessEventJobHandler implements JobHandler {
  
  public final static String TYPE = "event"; 

  public String getType() {
    return TYPE;
  }

  public void execute(JobEntity job, String configuration, ExecutionEntity execution, CommandContext commandContext) {
    // lookup subscription:    
    EventSubscriptionEntity eventSubscription = commandContext.getEventSubscriptionEntityManager()
      .findEventSubscriptionbyId(configuration);
    
    // if event subscription is null, ignore 
    if(eventSubscription != null) {      
      eventSubscription.eventReceived(null, false);      
    }
    
  }

}
