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

import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionConstants;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionUtil;
import org.activiti.workflow.simple.alfresco.model.M2Aspect;
import org.activiti.workflow.simple.alfresco.model.M2Mandatory;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.M2Property;
import org.activiti.workflow.simple.alfresco.model.M2Type;
import org.activiti.workflow.simple.alfresco.model.config.Form;
import org.activiti.workflow.simple.alfresco.model.config.FormField;
import org.activiti.workflow.simple.alfresco.model.config.FormFieldControl;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.form.DatePropertyDefinition;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;

public class AlfrescoDatePropertyConverter extends BaseAlfrescoFormPropertyConverter {

	@Override
	public Class<? extends FormPropertyDefinition> getConvertedClass() {
		return DatePropertyDefinition.class;
	}

	@Override
	public void convertProperty(M2Type contentType, String formSet, Form form, FormPropertyDefinition propertyDefinition, WorkflowDefinitionConversion conversion) {
		DatePropertyDefinition dateDefinition = (DatePropertyDefinition) propertyDefinition;
		String propertyName = getPropertyName(propertyDefinition, conversion);
		
		// Add to content model
		M2Property property = new M2Property();
		property.setMandatory(new M2Mandatory(dateDefinition.isMandatory()));
		property.setName(propertyName);
		
		if(dateDefinition.isShowTime()) {
			property.setPropertyType(AlfrescoConversionConstants.PROPERTY_TYPE_DATETIME);
		} else {
			property.setPropertyType(AlfrescoConversionConstants.PROPERTY_TYPE_DATE);
		}
		
		M2Model model = AlfrescoConversionUtil.getContentModel(conversion);
		M2Aspect aspect = model.getAspect(propertyName);
		if(aspect != null) {
			// In case the "shared" aspect doesn't have the actual property set yet, we
			// do this here
			if(aspect.getProperties().isEmpty()) {
				aspect.getProperties().add(property);
			}
			contentType.getMandatoryAspects().add(propertyName);
		} else {
			contentType.getProperties().add(property);
		}
		
		// Add form configuration
		form.getFormFieldVisibility().addShowFieldElement(propertyName);
		FormField formField = form.getFormAppearance().addFormField(propertyName, dateDefinition.getName(), formSet);

		if(dateDefinition.isWritable()) {
			// Use custom date-control
			FormFieldControl control = new FormFieldControl();
			control.setTemplate(AlfrescoConversionConstants.FORM_DATE_TEMPLATE);
			control.addControlParameter(AlfrescoConversionConstants.FORM_DATE_PARAM_SHOW_TIME, 
					Boolean.toString(dateDefinition.isShowTime()));
			control.addControlParameter(AlfrescoConversionConstants.FORM_DATE_PARAM_SUBMIT_TIME, 
					Boolean.toString(dateDefinition.isShowTime()));
			formField.setControl(control);
		} else {
			// Read-only properties should always be rendered using an info-template
			FormFieldControl control = new FormFieldControl();
			control.setTemplate(AlfrescoConversionConstants.FORM_READONLY_TEMPLATE);
			formField.setControl(control);
		}
		
		if(!form.isStartForm()) {
			// Add to output properties, if needed
			addOutputProperty(propertyDefinition, propertyName, contentType.getName(), conversion);
		}

	}
}
