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
package org.activiti.engine.impl.bpmn.parser.handler;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Transaction;
import org.activiti.engine.impl.bpmn.data.IOSpecification;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;


/**
 * @author Joram Barrez
 */
public class TransactionParseHandler extends AbstractMultiInstanceEnabledParseHandler<Transaction> {
  
  public Class< ? extends BaseElement> getHandledType() {
    return Transaction.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, Transaction transaction, ScopeImpl scope, ActivityImpl activityImpl, SubProcess subProcess) {
    
    ActivityImpl activity = createActivityOnScope(bpmnParse, transaction, BpmnXMLConstants.ELEMENT_TRANSACTION, scope);
    
    activity.setAsync(transaction.isAsynchronous());
    activity.setExclusive(!transaction.isNotExclusive());
    
    activity.setScope(true);
    activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createTransactionActivityBehavior(transaction));
    
    bpmnParse.processFlowElements(transaction.getFlowElements(), activity, subProcess);
    bpmnParse.processArtifacts(transaction.getArtifacts(), activity);
    
    if (transaction.getIoSpecification() != null) {
      IOSpecification ioSpecification = bpmnParse.createIOSpecification(transaction.getIoSpecification());
      activity.setIoSpecification(ioSpecification);
    }

  }

}
