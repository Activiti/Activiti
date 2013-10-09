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

import org.activiti.workflow.simple.alfresco.model.M2Type;
import org.activiti.workflow.simple.alfresco.model.config.Form;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;

/**
 *  
 * @author Frederik Heremans
 */
public interface AlfrescoFormPropertyConverter {

	/**
	 * @return class this converter can convert from.
	 */
	Class<? extends FormPropertyDefinition> getConvertedClass();
	
	/**
	 * Convert property and add it to the model/form.
	 */
	void convertProperty(M2Type contentType, String formSetId, Form form, FormPropertyDefinition propertyDefinition, WorkflowDefinitionConversion conversion);
}
