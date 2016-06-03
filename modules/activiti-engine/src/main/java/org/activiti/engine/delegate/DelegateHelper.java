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

package org.activiti.engine.delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.TaskWithFieldExtensions;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.el.FixedValue;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.apache.commons.lang3.StringUtils;

/**
 * Class that provides helper operations for use in the {@link JavaDelegate},
 * {@link ActivityBehavior}, {@link ExecutionListener} and {@link TaskListener}
 * interfaces.
 * 
 * @author Joram Barrez
 */
public class DelegateHelper {
  
  /**
   * Returns the {@link BpmnModel} matching the process definition bpmn model
   * for the process definition of the passed {@link DelegateExecution}. 
   */
  public static BpmnModel getBpmnModel(DelegateExecution execution) {
    if (execution == null) {
      throw new ActivitiException("Null execution passed");
    }
    return Context.getCommandContext().getProcessEngineConfiguration()
      .getDeploymentManager()
      .getBpmnModelById(execution.getProcessDefinitionId());
  }
  
  /**
   * Returns the current {@link FlowElement} where the {@link DelegateExecution} is currently at.
   */
  public static FlowElement getFlowElement(DelegateExecution execution) {
    BpmnModel bpmnModel = getBpmnModel(execution);
    FlowElement flowElement = bpmnModel.getFlowElement(execution.getCurrentActivityId());
    if (flowElement == null) {
      throw new ActivitiException("Could not find a FlowElement for activityId " + execution.getCurrentActivityId());
    }
    return flowElement;
  }
  
  /**
   * Returns for the activityId of the passed {@link DelegateExecution} the
   * {@link Map} of {@link ExtensionElement} instances. These represent the
   * extension elements defined in the BPMN 2.0 XML as part of that particular
   * activity.
   */
  public static Map<String, List<ExtensionElement>> getExtensionElements(DelegateExecution execution) {
    return getFlowElement(execution).getExtensionElements();
  }
  
  /**
   * Returns the list of field extensions, represented as instances of {@link FieldExtension},
   * for the current activity of the passed {@link DelegateExecution}. 
   */
  public static List<FieldExtension> getFields(DelegateExecution execution) {
    FlowElement flowElement = getFlowElement(execution);
    if (flowElement instanceof TaskWithFieldExtensions) {
      return ((TaskWithFieldExtensions) flowElement).getFieldExtensions();
    }
    return new ArrayList<FieldExtension>();
  }
  
  /**
   * Returns the {@link FieldExtension} matching the provided 'fieldName' which is
   * defined for the current activity of the provided {@link DelegateExecution}.
   * 
   * Returns null if no such {@link FieldExtension} can be found.
   */
  public static FieldExtension getField(DelegateExecution execution, String fieldName) {
    List<FieldExtension> fieldExtensions = getFields(execution);
    if (fieldExtensions == null || fieldExtensions.size() == 0) {
      return null;
    }
    for (FieldExtension fieldExtension : fieldExtensions) {
      if (fieldExtension.getFieldName() != null && fieldExtension.getFieldName().equals(fieldName)) {
        return fieldExtension;
      }
    }
    return null;
  }
  
  /**
   * Creates an {@link Expression} for the {@link FieldExtension}. 
   */
  public static Expression createExpressionForField(FieldExtension fieldExtension) {
    if (StringUtils.isNotEmpty(fieldExtension.getExpression())) {
      ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
      return expressionManager.createExpression(fieldExtension.getExpression());
    } else {
      return new FixedValue(fieldExtension.getStringValue());
    }
  }
  
  /**
   * Returns the {@link Expression} for the field defined for the current activity
   * of the provided {@link DelegateExecution}.
   * 
   *  Returns null if no such field was found in the process definition xml.
   */
  public static Expression getFieldExpression(DelegateExecution execution, String fieldName) {
    FieldExtension fieldExtension = getField(execution, fieldName);
    if (fieldExtension != null) {
      return createExpressionForField(fieldExtension);
    }
    return null;
  }
  
}
