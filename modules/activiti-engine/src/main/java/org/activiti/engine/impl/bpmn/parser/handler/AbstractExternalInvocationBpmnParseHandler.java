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

import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Task;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;


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

}
