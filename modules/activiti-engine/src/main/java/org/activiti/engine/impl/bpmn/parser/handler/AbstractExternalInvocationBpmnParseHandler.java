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

import org.activiti.bpmn.model.DataAssociation;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.data.AbstractDataAssociation;
import org.activiti.engine.impl.bpmn.data.Assignment;
import org.activiti.engine.impl.bpmn.data.SimpleDataInputAssociation;
import org.activiti.engine.impl.bpmn.data.TransformationDataOutputAssociation;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.webservice.MessageImplicitDataInputAssociation;
import org.activiti.engine.impl.bpmn.webservice.MessageImplicitDataOutputAssociation;
import org.apache.commons.lang3.StringUtils;


/**
 * @author Joram Barrez
 */
public abstract class AbstractExternalInvocationBpmnParseHandler<T extends FlowNode> extends AbstractActivityBpmnParseHandler<T> {
  
  public AbstractDataAssociation createDataInputAssociation(BpmnParse bpmnParse, DataAssociation dataAssociationElement) {
    if (dataAssociationElement.getAssignments().isEmpty()) {
      return new MessageImplicitDataInputAssociation(dataAssociationElement.getSourceRef(), dataAssociationElement.getTargetRef());
    } else {
      SimpleDataInputAssociation dataAssociation = new SimpleDataInputAssociation(
          dataAssociationElement.getSourceRef(), dataAssociationElement.getTargetRef());

      for (org.activiti.bpmn.model.Assignment assigmentElement : dataAssociationElement.getAssignments()) {
        if (StringUtils.isNotEmpty(assigmentElement.getFrom()) && StringUtils.isNotEmpty(assigmentElement.getTo())) {
          Expression from = bpmnParse.getExpressionManager().createExpression(assigmentElement.getFrom());
          Expression to = bpmnParse.getExpressionManager().createExpression(assigmentElement.getTo());
          Assignment assignment = new Assignment(from, to);
          dataAssociation.addAssignment(assignment);
        }
      }
      return dataAssociation;
    }
  }
  
  public AbstractDataAssociation createDataOutputAssociation(BpmnParse bpmnParse, DataAssociation dataAssociationElement) {
    if (StringUtils.isNotEmpty(dataAssociationElement.getSourceRef())) {
      return new MessageImplicitDataOutputAssociation(dataAssociationElement.getTargetRef(), dataAssociationElement.getSourceRef());
    } else {
      Expression transformation = bpmnParse.getExpressionManager().createExpression(dataAssociationElement.getTransformation());
      AbstractDataAssociation dataOutputAssociation = new TransformationDataOutputAssociation(null, dataAssociationElement.getTargetRef(), transformation);
      return dataOutputAssociation;
    }
  }


}
