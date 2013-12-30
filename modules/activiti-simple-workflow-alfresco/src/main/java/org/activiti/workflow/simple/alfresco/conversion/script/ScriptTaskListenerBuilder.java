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
package org.activiti.workflow.simple.alfresco.conversion.script;

import java.text.MessageFormat;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionConstants;

/**
 * A builder class that creates a task-listener that runs alfresco-scripts when the task-event occurs.
 *  
 * @author Frederik Heremans
 */
public class ScriptTaskListenerBuilder {

	protected static final String INCOMING_VARIABLE_SCRIPT_TEMPLATE = "if (typeof execution.getVariable(''{1}'') != 'undefined') task.setVariableLocal(''{0}'', execution.getVariable(''{1}''));\n";
	protected static final String OUTGOING_VARIABLE_SCRIPT_TEMPLATE = "if (typeof task.getVariableLocal(''{0}'') != 'undefined') execution.setVariable(''{1}'', task.getVariableLocal(''{0}''));\n";
	protected static final String INCOMING_DUE_DATE_TEMPLATE = "if (typeof bpm_workflowDueDate != 'undefined') task.dueDate = bpm_workflowDueDate;\n";
	protected static final String INCOMING_PRIORITY_TEMPLATE = "if (typeof bpm_workflowPriority != 'undefined') task.priority = bpm_workflowPriority;;\n";
	
	protected ActivitiListener listener;
	protected StringBuilder finalScript;
	protected String event;
	
	public ScriptTaskListenerBuilder() {
		finalScript = new StringBuilder("\n");
	}
	
	public void addScript(String script) {
		if(script != null) {
			finalScript.append(script);
		}
	}
	
	public void addLine(String script) {
		if(script != null) {
			finalScript.append(script).append("\n");
		}
	}
	
	public void addIncomingProperty(String workflowPropertyName, String taskPropertyName) {
		addScript(MessageFormat.format(INCOMING_VARIABLE_SCRIPT_TEMPLATE, getVariableName(workflowPropertyName),
				getVariableName(taskPropertyName)));
	}
	
	public void addOutgoingProperty(String workflowPropertyName, String taskPropertyName) {
		addScript(MessageFormat.format(OUTGOING_VARIABLE_SCRIPT_TEMPLATE, getVariableName(workflowPropertyName),
				getVariableName(taskPropertyName)));
	}
	
	public void addDueDateInheritance() {
		addScript(INCOMING_DUE_DATE_TEMPLATE);
	}
	
	public void addPriorityInheritance() {
		addScript(INCOMING_PRIORITY_TEMPLATE);
	}
	
	public ActivitiListener build() {
		if(listener == null) {
			listener = new ActivitiListener();
			listener.setEvent(event);
			listener.setImplementationType(BpmnXMLConstants.ATTRIBUTE_LISTENER_CLASS);
			listener.setImplementation(AlfrescoConversionConstants.CLASSNAME_SCRIPT_TASK_LISTENER);
			
			FieldExtension scriptFieldElement = new FieldExtension();
			scriptFieldElement.setFieldName(AlfrescoConversionConstants.SCRIPT_TASK_LISTENER_SCRIPT_FIELD_NAME);
			scriptFieldElement.setStringValue(finalScript.toString());
			listener.getFieldExtensions().add(scriptFieldElement);
		}
		return listener;
	}
	
	public void setEvent(String event) {
	  this.event = event;
  }
	
	protected String getVariableName(String propertyName) {
		if(propertyName != null) {
			return propertyName.replace(':', '_');
		}
		return null;
	}
}
