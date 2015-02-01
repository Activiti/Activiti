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

import java.util.ArrayList;
import java.util.List;

import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionConstants;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionUtil;
import org.activiti.workflow.simple.alfresco.model.M2Aspect;
import org.activiti.workflow.simple.alfresco.model.M2Constraint;
import org.activiti.workflow.simple.alfresco.model.M2Mandatory;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.M2NamedValue;
import org.activiti.workflow.simple.alfresco.model.M2Property;
import org.activiti.workflow.simple.alfresco.model.M2Type;
import org.activiti.workflow.simple.alfresco.model.config.Form;
import org.activiti.workflow.simple.alfresco.model.config.FormField;
import org.activiti.workflow.simple.alfresco.model.config.FormFieldControl;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ListPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ListPropertyEntry;

public class AlfrescoListPropertyConverter extends BaseAlfrescoFormPropertyConverter {

	@Override
	public Class<? extends FormPropertyDefinition> getConvertedClass() {
		return ListPropertyDefinition.class;
	}

	@Override
	public void convertProperty(M2Type contentType, String formSet, Form form, FormPropertyDefinition propertyDefinition, WorkflowDefinitionConversion conversion) {
		ListPropertyDefinition dateDefinition = (ListPropertyDefinition) propertyDefinition;
		
		String propertyName = getPropertyName(propertyDefinition, conversion);
		
		// Add to content model
		M2Property property = new M2Property();
		property.setMandatory(new M2Mandatory(dateDefinition.isMandatory()));
		property.setName(propertyName);
		property.setPropertyType(AlfrescoConversionConstants.PROPERTY_TYPE_TEXT);
		
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
		
		// Create constraint for the values
		if(dateDefinition.getEntries() != null && !dateDefinition.getEntries().isEmpty()) {
			M2Constraint valueConstraint = new M2Constraint();
			valueConstraint.setType(AlfrescoConversionConstants.CONTENT_MODEL_CONSTRAINT_TYPE_LIST);
			valueConstraint.setName(propertyName + AlfrescoConversionConstants.CONTENT_MODEL_CONSTRAINT_TYPE_LIST.toLowerCase());
			
			List<String> values = new ArrayList<String>(dateDefinition.getEntries().size());
			for(ListPropertyEntry entry : dateDefinition.getEntries()) {
				// TODO: i18n file using labels in properties-file, a part of deployment?
				values.add(entry.getValue());
			}
			valueConstraint.getParameters().add(new M2NamedValue(AlfrescoConversionConstants.CONTENT_MODEL_CONSTRAINT_ALLOWED_VALUES, null, values));
			
			// Add constraint to the root model instead of the type itself and reference it from within the property
			// for readability and reuse of the model
			model.getConstraints().add(valueConstraint);
			
			M2Constraint reference = new M2Constraint();
			reference.setRef(valueConstraint.getName());
			property.getConstraints().add(reference);
		}
		
		// Add form configuration
		form.getFormFieldVisibility().addShowFieldElement(propertyName);
		FormField formField = form.getFormAppearance().addFormField(propertyName, propertyDefinition.getName(), formSet);

		// Read-only properties should always be rendered using an info-template
		if(!dateDefinition.isWritable()) {
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
