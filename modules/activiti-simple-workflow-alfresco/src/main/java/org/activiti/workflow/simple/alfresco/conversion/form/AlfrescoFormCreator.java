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

import java.util.HashMap;
import java.util.Map;

import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionConstants;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionUtil;
import org.activiti.workflow.simple.alfresco.conversion.exception.AlfrescoSimpleWorkflowException;
import org.activiti.workflow.simple.alfresco.form.AlfrescoTransitionsPropertyDefinition;
import org.activiti.workflow.simple.alfresco.model.M2Type;
import org.activiti.workflow.simple.alfresco.model.config.Form;
import org.activiti.workflow.simple.alfresco.model.config.FormSet;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;
import org.activiti.workflow.simple.definition.form.FormPropertyGroup;

/**
 * Populates content-model and form-config based on a {@link FormDefinition}.
 * 
 * @author Frederik Heremans
 */
public class AlfrescoFormCreator {

	private Map<Class<? extends FormPropertyDefinition>, AlfrescoFormPropertyConverter> propertyConverters;
	
	public AlfrescoFormCreator() {
		propertyConverters = new HashMap<Class<? extends FormPropertyDefinition>, AlfrescoFormPropertyConverter>();
		
		registerConverter(new AlfrescoTextPropertyConverter());
		registerConverter(new AlfrescoDatePropertyConverter());
		registerConverter(new AlfrescoNumberPropertyConverter());
		registerConverter(new AlfrescoListPropertyConverter());
		registerConverter(new AlfrescoReferencePropertyConverter());
		registerConverter(new AlfrescoBooleanPropertyConverter());
		registerConverter(new AlfrescoTransitionsPropertyConverter());
  }
	
	public void createForm(M2Type contentType, Form formConfig, FormDefinition formDefinition, WorkflowDefinitionConversion conversion) {
		if(formDefinition != null && formDefinition.getFormGroups() != null) {
			
			for(FormPropertyGroup group : formDefinition.getFormGroups()) {
				// Create a group in the form-config
				String groupId = null; 
				if(group.getId() != null) {
					groupId = AlfrescoConversionUtil.getValidIdString(group.getId());
				} else {
					groupId = AlfrescoConversionUtil.getValidIdString(group.getTitle());
				}
				
				FormSet formSet = formConfig.getFormAppearance().addFormSet(groupId, getAppearanceForGroup(group), group.getTitle(), getTemplateForGroup(group));
				
				// Convert all properties
				AlfrescoFormPropertyConverter converter = null;
				for(FormPropertyDefinition property : group.getFormPropertyDefinitions()) {
			    converter = propertyConverters.get(property.getClass());
			    if(converter == null) {
			    	throw new AlfrescoSimpleWorkflowException("Unsupported property type: " + property.getClass().getName());
			    }
			    converter.convertProperty(contentType, formSet.getId(), formConfig, property, conversion);
				}
			}
			
		}
		
		if(formDefinition != null && formDefinition.getFormPropertyDefinitions() != null && !formDefinition.getFormPropertyDefinitions().isEmpty()) {
			for(FormPropertyDefinition def : formDefinition.getFormPropertyDefinitions()) {
				if(def instanceof AlfrescoTransitionsPropertyDefinition) {
					AlfrescoFormPropertyConverter converter = propertyConverters.get(def.getClass());
			    if(converter != null) {
			    	converter.convertProperty(contentType, null, formConfig, def, conversion);
			    }
				}
			}
		}
		// Finally, add default "transitions" if not already added to the model
		if(formConfig.getFormAppearance().getFormSet(AlfrescoConversionConstants.FORM_SET_RESPONSE) == null) {
			formConfig.getFormAppearance().addFormSet(AlfrescoConversionConstants.FORM_SET_RESPONSE, null, null, null);
			formConfig.getFormFieldVisibility().addShowFieldElement(AlfrescoConversionConstants.FORM_FIELD_TRANSITIONS);
			formConfig.getFormAppearance().addFormField(AlfrescoConversionConstants.FORM_FIELD_TRANSITIONS, null, AlfrescoConversionConstants.FORM_SET_RESPONSE);
		}
	}

	protected String getTemplateForGroup(FormPropertyGroup group) {
		String template = null;
	  if(group.getType() != null) {
	  	if(AlfrescoConversionConstants.FORM_GROUP_LAYOUT_2_COLUMNS.equals(group.getType())) {
	  		template = AlfrescoConversionConstants.FORM_SET_TEMPLATE_2_COLUMN;
			} else if (AlfrescoConversionConstants.FORM_GROUP_LAYOUT_3_COLUMNS.equals(group.getType())) {
				template = AlfrescoConversionConstants.FORM_SET_TEMPLATE_3_COLUMN;
			}
	  }
	  return template;
  }

	protected String getAppearanceForGroup(FormPropertyGroup group) {
		if(group.getTitle() != null && !group.getTitle().isEmpty()) {
			return AlfrescoConversionConstants.FORM_SET_APPEARANCE_TITLE;
		} else {
			return null;
		}
  }
	
	protected void registerConverter(AlfrescoFormPropertyConverter converter) {
		propertyConverters.put(converter.getConvertedClass(), converter);
  }
}
