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
package org.activiti.workflow.simple.alfresco.conversion;

import java.text.MessageFormat;
import java.util.UUID;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.workflow.simple.alfresco.configmodel.Configuration;
import org.activiti.workflow.simple.alfresco.configmodel.Module;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.M2Namespace;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.listener.WorkflowDefinitionConversionListener;

/**
 * A {@link WorkflowDefinitionConversionListener} that creates a {@link M2Model} and a {@link Configuration}
 * before conversion, that can be used to add any models and configuration needed throughout the conversion.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class InitializeAlfrescoModelsConversionListener implements WorkflowDefinitionConversionListener {

  private static final long serialVersionUID = 1L;
  
	@Override
	public void beforeStepsConversion(WorkflowDefinitionConversion conversion) {
		String processId = generateUniqueProcessId(conversion);
		addContentModel(conversion, processId);
		addModule(conversion, processId);
	}

	@Override
	public void afterStepsConversion(WorkflowDefinitionConversion conversion) {
		for(FlowElement flowElement : conversion.getProcess().getFlowElements()) {
			if(flowElement instanceof StartEvent) {
				StartEvent startEvent = (StartEvent) flowElement;
				if(startEvent.getFormKey() == null) {
					startEvent.setFormKey(AlfrescoConversionConstants.DEFAULT_START_FORM_TYPE);
				}
			}
		}
	}
	
	protected String generateUniqueProcessId(WorkflowDefinitionConversion conversion) {
		String processId = AlfrescoConversionUtil.getValidIdString(
				AlfrescoConversionConstants.PROCESS_ID_PREFIX + UUID.randomUUID().toString());
		conversion.getProcess().setId(processId);
		return processId;
  }
	
	protected void addContentModel(WorkflowDefinitionConversion conversion, String processId) {
		// The process ID is used as namespace prefix, to guarantee uniqueness
		
		// Set general model properties
		M2Model model = new M2Model();
		model.setName(AlfrescoConversionUtil.getQualifiedName(processId, 
				AlfrescoConversionConstants.CONTENT_MODEL_UNQUALIFIED_NAME));
		
		M2Namespace namespace = AlfrescoConversionUtil.createNamespace(processId);
		model.getNamespaces().add(namespace);
		
		
		// Import required alfresco models
		model.getImports().add(AlfrescoConversionConstants.DICTIONARY_NAMESPACE);
		model.getImports().add(AlfrescoConversionConstants.CONTENT_NAMESPACE);
		model.getImports().add(AlfrescoConversionConstants.BPM_NAMESPACE);
		
		// Store model in the conversion artifacts to be accessed later
		AlfrescoConversionUtil.storeContentModel(model, conversion);
		AlfrescoConversionUtil.storeModelNamespacePrefix(namespace.getPrefix(), conversion);
  }
	
	protected void addModule(WorkflowDefinitionConversion conversion, String processId) {
		// Create form-configuration
		Module module = new Module();
		module.setId(MessageFormat.format(AlfrescoConversionConstants.MODULE_ID, processId));
		AlfrescoConversionUtil.storeModule(module, conversion);
  }
}
