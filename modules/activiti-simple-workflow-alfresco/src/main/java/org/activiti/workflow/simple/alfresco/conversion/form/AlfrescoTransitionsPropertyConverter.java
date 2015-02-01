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
import org.activiti.workflow.simple.alfresco.form.AlfrescoTransitionsPropertyDefinition;
import org.activiti.workflow.simple.alfresco.model.M2Constraint;
import org.activiti.workflow.simple.alfresco.model.M2Mandatory;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.M2NamedValue;
import org.activiti.workflow.simple.alfresco.model.M2Property;
import org.activiti.workflow.simple.alfresco.model.M2PropertyOverride;
import org.activiti.workflow.simple.alfresco.model.M2Type;
import org.activiti.workflow.simple.alfresco.model.config.Form;
import org.activiti.workflow.simple.alfresco.model.config.FormField;
import org.activiti.workflow.simple.alfresco.model.config.FormFieldControl;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ListPropertyEntry;

public class AlfrescoTransitionsPropertyConverter extends BaseAlfrescoFormPropertyConverter {

	@Override
	public Class<? extends FormPropertyDefinition> getConvertedClass() {
		return AlfrescoTransitionsPropertyDefinition.class;
	}

	@Override
	public void convertProperty(M2Type contentType, String formSet, Form form, FormPropertyDefinition propertyDefinition, WorkflowDefinitionConversion conversion) {
		AlfrescoTransitionsPropertyDefinition def = (AlfrescoTransitionsPropertyDefinition) propertyDefinition;
		
		String propertyName = contentType.getName() + AlfrescoConversionConstants.PROPERTY_TRANSITIONS_SUFFIX;
		
		// Add to content model
		M2Property property = new M2Property();
		property.setMandatory(new M2Mandatory(def.isMandatory()));
		property.setName(propertyName);
		property.setPropertyType(AlfrescoConversionConstants.PROPERTY_TYPE_TEXT);
		
		M2Model model = AlfrescoConversionUtil.getContentModel(conversion);
	  contentType.getProperties().add(property);
		
		// Create constraint for the values
		if(def.getTransitions() != null && !def.getTransitions().isEmpty()) {
			M2Constraint valueConstraint = new M2Constraint();
			valueConstraint.setType(AlfrescoConversionConstants.CONTENT_MODEL_CONSTRAINT_TYPE_LIST);
			valueConstraint.setName(propertyName + AlfrescoConversionConstants.CONTENT_MODEL_CONSTRAINT_TYPE_LIST.toLowerCase());
			
			List<String> values = new ArrayList<String>(def.getTransitions().size());
			for(ListPropertyEntry entry : def.getTransitions()) {
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
		
		// Add a pointer to the transition-property as well, using an override
		M2PropertyOverride override = new M2PropertyOverride();
		override.setDefaultValue(AlfrescoConversionUtil.getUrlQualifiedPropertyName(propertyName, model.getNamespaces().get(0)));
		override.setName(AlfrescoConversionConstants.PROPERTY_OUTCOME_PROPERTY_NAME);
		contentType.getPropertyOverrides().add(override);
		
		// Add the transition-set
		form.getFormAppearance().addFormSet(AlfrescoConversionConstants.FORM_SET_RESPONSE, null, null, null);
		form.getFormFieldVisibility().addShowFieldElement(propertyName);
		
		FormField transitionsFormField = new FormField();
		transitionsFormField.setId(propertyName);
		transitionsFormField.setSet(AlfrescoConversionConstants.FORM_SET_RESPONSE);
		transitionsFormField.setControl(new FormFieldControl(AlfrescoConversionConstants.FORM_TRANSITIONS_TEMPLATE));
		form.getFormAppearance().addFormAppearanceElement(transitionsFormField);
	}
}
