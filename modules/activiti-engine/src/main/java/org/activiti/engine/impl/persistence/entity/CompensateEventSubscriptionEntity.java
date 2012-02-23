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

package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.impl.event.CompensationEventHandler;



/**
 * @author Daniel Meyer
 */
public class CompensateEventSubscriptionEntity extends EventSubscriptionEntity {
  
  @SuppressWarnings("unused") // used by mybatis
  private CompensateEventSubscriptionEntity() {
  }

  private CompensateEventSubscriptionEntity(ExecutionEntity executionEntity) {
    super(executionEntity);
    eventType=CompensationEventHandler.EVENT_HANDLER_TYPE;    
  }
  
  public static CompensateEventSubscriptionEntity createAndInsert(ExecutionEntity executionEntity) {
    CompensateEventSubscriptionEntity eventSubscription = new CompensateEventSubscriptionEntity(executionEntity);    
    eventSubscription.insert();
    return eventSubscription;
  }
  
  // custom processing behavior //////////////////////////////////////////////////////////////////////////////  
  
  @Override
  protected void processEventSync(Object payload) {
    delete();  
    super.processEventSync(payload);
  }
  
  // custom persistence behavior /////////////////////////////////////////////////////////////////////////////
  
  @Override
  public void insert() {
    addToExecution();
    super.insert();
  }
  
  @Override
  public void delete() {
    removeFromExecution();
    super.delete();
  }
  
  // referential integrity CompensateEventSubscription -> ExecutionEntity ////////////////////////////////////
  
  protected void addToExecution() {
    // add reference in execution
    ExecutionEntity execution = getExecution();
    if(execution != null) {
      execution.addCompensateEventSubscription(this);
    }
  }
  
  protected void removeFromExecution() {
    // remove reference in execution
    ExecutionEntity execution = getExecution();
    if(execution != null) {
      execution.removeCompensateEventSubscription(this);
    }
  }

  public CompensateEventSubscriptionEntity moveUnder(ExecutionEntity newExecution) {    
    
    delete();    
    
    CompensateEventSubscriptionEntity newSubscription = createAndInsert(newExecution);
    newSubscription.setActivity(getActivity());
    newSubscription.setConfiguration(configuration);    
    // use the original date
    newSubscription.setCreated(created);   
    
    return newSubscription;    
  }
      
}
