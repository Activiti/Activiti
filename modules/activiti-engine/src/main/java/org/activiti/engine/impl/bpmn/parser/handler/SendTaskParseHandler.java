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
import org.activiti.bpmn.model.DataAssociation;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SendTask;
import org.activiti.engine.impl.bpmn.behavior.WebServiceActivityBehavior;
import org.activiti.engine.impl.bpmn.data.AbstractDataAssociation;
import org.activiti.engine.impl.bpmn.data.IOSpecification;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.webservice.Operation;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Joram Barrez
 */
public class SendTaskParseHandler extends AbstractExternalInvocationBpmnParseHandler<SendTask> {
	
	private static final Logger logger = LoggerFactory.getLogger(SendTaskParseHandler.class);
  
  public Class< ? extends BaseElement> getHandledType() {
    return SendTask.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, SendTask sendTask) {
 
    ActivityImpl activity = createActivityOnCurrentScope(bpmnParse, sendTask, BpmnXMLConstants.ELEMENT_TASK_SEND);
    
    activity.setAsync(sendTask.isAsynchronous());
    activity.setExclusive(!sendTask.isNotExclusive());

    if (StringUtils.isNotEmpty(sendTask.getType())) {
      if (sendTask.getType().equalsIgnoreCase("mail")) {
        activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createMailActivityBehavior(sendTask));
      } else if (sendTask.getType().equalsIgnoreCase("mule")) {
        activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createMuleActivityBehavior(sendTask, bpmnParse.getBpmnModel()));
      } else if (sendTask.getType().equalsIgnoreCase("camel")) {
        activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createCamelActivityBehavior(sendTask, bpmnParse.getBpmnModel()));
      } 

      // for web service
    } else if (ImplementationType.IMPLEMENTATION_TYPE_WEBSERVICE.equalsIgnoreCase(sendTask.getImplementationType()) && 
        StringUtils.isNotEmpty(sendTask.getOperationRef())) {
      
      if (!bpmnParse.getOperations().containsKey(sendTask.getOperationRef())) {
        logger.warn(sendTask.getOperationRef() + " does not exist for sendTask " + sendTask.getId());
      } else {
        WebServiceActivityBehavior webServiceActivityBehavior = bpmnParse.getActivityBehaviorFactory().createWebServiceActivityBehavior(sendTask);
        Operation operation = bpmnParse.getOperations().get(sendTask.getOperationRef());
        webServiceActivityBehavior.setOperation(operation);

        if (sendTask.getIoSpecification() != null) {
          IOSpecification ioSpecification = createIOSpecification(bpmnParse, sendTask.getIoSpecification());
          webServiceActivityBehavior.setIoSpecification(ioSpecification);
        }

        for (DataAssociation dataAssociationElement : sendTask.getDataInputAssociations()) {
          AbstractDataAssociation dataAssociation = createDataInputAssociation(bpmnParse, dataAssociationElement);
          webServiceActivityBehavior.addDataInputAssociation(dataAssociation);
        }

        for (DataAssociation dataAssociationElement : sendTask.getDataOutputAssociations()) {
          AbstractDataAssociation dataAssociation = createDataOutputAssociation(bpmnParse, dataAssociationElement);
          webServiceActivityBehavior.addDataOutputAssociation(dataAssociation);
        }

        activity.setActivityBehavior(webServiceActivityBehavior);
      }
    } else {
      logger.warn("One of the attributes 'type' or 'operation' is mandatory on sendTask " + sendTask.getId());
    }
  }

}
