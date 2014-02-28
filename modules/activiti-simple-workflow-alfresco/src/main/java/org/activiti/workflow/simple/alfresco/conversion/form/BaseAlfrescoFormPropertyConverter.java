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
package org.activiti.workflow.simple.alfresco.conversion.form;

import java.util.Map;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.UserTask;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionConstants;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionUtil;
import org.activiti.workflow.simple.alfresco.conversion.PropertySharing;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;

public abstract class BaseAlfrescoFormPropertyConverter implements AlfrescoFormPropertyConverter {

	protected boolean extractBooleanFromParameters(Map<String, Object> parameters, String key, boolean defaultValue) {
		boolean result = defaultValue;
		if(parameters != null) {
			Object value = parameters.get(key);
			if(value != null) {
				if(value instanceof Boolean) {
					result = (Boolean) value;
				} else if(value instanceof String){
					result = Boolean.valueOf((Boolean) value);
				} else {
					result = Boolean.valueOf(value.toString());
				}
			}
		}
		return result;
	}
	
	protected String getPropertyName(FormPropertyDefinition definition, WorkflowDefinitionConversion conversion) {
		return AlfrescoConversionUtil.getQualifiedName(AlfrescoConversionUtil.getModelNamespacePrefix(conversion),
				definition.getName());
	}
	
	protected void addOutputProperty(FormPropertyDefinition definition, String propertyName, String contentType, WorkflowDefinitionConversion conversion) {
		boolean isOutputNeeded = extractBooleanFromParameters(definition.getParameters(), AlfrescoConversionConstants.PARAMETER_ADD_PROPERTY_TO_OUTPUT, false);
		
		if(isOutputNeeded) {
			String userTaskId = getUserTaskIdWithFormkey(contentType, conversion.getProcess());
			if(userTaskId != null) {
				PropertySharing propertySharing = AlfrescoConversionUtil.getPropertySharing(conversion, userTaskId);
				propertySharing.addOutgoingProperty(propertyName, propertyName);
			}
		}
	}

	protected String getUserTaskIdWithFormkey(String contentType, Process process) {
		for(FlowElement flowElement : process.getFlowElements()) {
			if(flowElement instanceof UserTask && contentType.equals(((UserTask) flowElement).getFormKey())) {
				return flowElement.getId();
			}
		}
	  return null;
  }
}
