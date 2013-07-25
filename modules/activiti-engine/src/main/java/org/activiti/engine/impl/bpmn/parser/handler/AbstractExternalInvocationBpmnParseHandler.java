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

import java.util.List;

import org.activiti.bpmn.model.DataAssociation;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Task;
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
  
  protected void validateFieldDeclarationsForEmail(BpmnParse bpmnParse, Task task, List<FieldExtension> fieldExtensions) {
    boolean toDefined = false;
    boolean textOrHtmlDefined = false;
    
    for (FieldExtension fieldExtension : fieldExtensions) {
      if (fieldExtension.getFieldName().equals("to")) {
        toDefined = true;
      }
      if (fieldExtension.getFieldName().equals("html")) {
        textOrHtmlDefined = true;
      }
      if (fieldExtension.getFieldName().equals("text")) {
        textOrHtmlDefined = true;
      }
    }

    if (!toDefined) {
      bpmnParse.getBpmnModel().addProblem("No recipient is defined on the mail activity", task);
    }
    if (!textOrHtmlDefined) {
      bpmnParse.getBpmnModel().addProblem("Text or html field should be provided", task);
    }
  }

  protected void validateFieldDeclarationsForShell(BpmnParse bpmnParse, Task task, List<FieldExtension> fieldExtensions) {
    boolean shellCommandDefined = false;

    for (FieldExtension fieldExtension : fieldExtensions) {
      String fieldName = fieldExtension.getFieldName();
      String fieldValue = fieldExtension.getStringValue();

      shellCommandDefined |= fieldName.equals("command");

      if ((fieldName.equals("wait") || fieldName.equals("redirectError") || fieldName.equals("cleanEnv")) && !fieldValue.toLowerCase().equals("true")
              && !fieldValue.toLowerCase().equals("false")) {
        bpmnParse.getBpmnModel().addProblem("undefined value for shell " + fieldName + " parameter :" + fieldValue.toString() + ".", task);
      }

    }

    if (!shellCommandDefined) {
      bpmnParse.getBpmnModel().addProblem("No shell command is defined on the shell activity", task);
    }
  }
  
  public AbstractDataAssociation createDataInputAssociation(BpmnParse bpmnParse, DataAssociation dataAssociationElement) {
    if (StringUtils.isEmpty(dataAssociationElement.getTargetRef())) {
      bpmnParse.getBpmnModel().addProblem("targetRef is required", dataAssociationElement);
    }
    
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
