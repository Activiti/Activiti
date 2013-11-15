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
import org.activiti.workflow.simple.alfresco.conversion.exception.AlfrescoSimpleWorkflowException;
import org.activiti.workflow.simple.alfresco.conversion.script.PropertyReference;
import org.activiti.workflow.simple.alfresco.model.M2Aspect;
import org.activiti.workflow.simple.alfresco.model.M2AssociationSource;
import org.activiti.workflow.simple.alfresco.model.M2AssociationTarget;
import org.activiti.workflow.simple.alfresco.model.M2ClassAssociation;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.M2PropertyOverride;
import org.activiti.workflow.simple.alfresco.model.M2Type;
import org.activiti.workflow.simple.alfresco.model.config.Form;
import org.activiti.workflow.simple.alfresco.model.config.FormField;
import org.activiti.workflow.simple.alfresco.model.config.FormFieldControl;
import org.activiti.workflow.simple.alfresco.model.config.FormFieldControlParameter;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ReferencePropertyDefinition;

public class AlfrescoReferencePropertyConverter extends BaseAlfrescoFormPropertyConverter {

	@Override
	public Class<? extends FormPropertyDefinition> getConvertedClass() {
		return ReferencePropertyDefinition.class;
	}

	@Override
	public void convertProperty(M2Type contentType, String formSet, Form form, FormPropertyDefinition propertyDefinition,
	    WorkflowDefinitionConversion conversion) {
		ReferencePropertyDefinition referenceDefinition = (ReferencePropertyDefinition) propertyDefinition;

		if (AlfrescoConversionConstants.FORM_REFERENCE_DUEDATE.equals(referenceDefinition.getType())) {
			addDueDateReference(form, formSet, referenceDefinition.isWritable());
		} else if (AlfrescoConversionConstants.FORM_REFERENCE_PACKAGE_ITEMS.equals(referenceDefinition.getType())) {
			addPackageReference(form, formSet, contentType, referenceDefinition);
		} else if (AlfrescoConversionConstants.FORM_REFERENCE_EMAIL_NOTIFICATION.equals(referenceDefinition.getType())) {
			addEmailNotificationReference(form, formSet, contentType, referenceDefinition);
		} else if (AlfrescoConversionConstants.FORM_REFERENCE_PRIORITY.equals(referenceDefinition.getType())) {
			addPriorityReference(form, formSet, referenceDefinition.isWritable());
		} else if (AlfrescoConversionConstants.FORM_REFERENCE_WORKFLOW_DESCRIPTION.equals(referenceDefinition.getType())) {
			addWorkflowDescriptionReference(form, formSet);
		} else if (AlfrescoConversionConstants.FORM_REFERENCE_COMMENT.equals(referenceDefinition.getType())) {
			addCommentReference(form, formSet);
		} else if (AlfrescoConversionConstants.FORM_REFERENCE_FIELD.equals(referenceDefinition.getType())) {
			addFieldReference(form, formSet, referenceDefinition, contentType, conversion);
		} else {
			addAssociation(form, formSet, referenceDefinition, contentType, conversion);
		}
	}

	protected void addAssociation(Form form, String formSet, ReferencePropertyDefinition referenceDefinition,
	    M2Type contentType, WorkflowDefinitionConversion conversion) {

		M2Model model = AlfrescoConversionUtil.getContentModel(conversion);

		// Check if model contains an aspect for the property
		String propertyName = getPropertyName(referenceDefinition, conversion);

		M2ClassAssociation association = new M2ClassAssociation();
		association.setName(propertyName);
		association.setTitle(referenceDefinition.getName());
		M2AssociationSource source = new M2AssociationSource();
		source.setMany(false);
		source.setMandatory(true);
		M2AssociationTarget target = new M2AssociationTarget();
		target.setClassName(referenceDefinition.getType());
		target.setMandatory(referenceDefinition.isMandatory());

		// Determine whether or not it's allowed to select multiple targets
		boolean isTargetMany = extractBooleanFromParameters(referenceDefinition.getParameters(),
		    AlfrescoConversionConstants.PARAMETER_REFERENCE_MANY, false);
		target.setMany(isTargetMany);

		association.setTarget(target);
		association.setSource(source);

		M2Aspect aspect = model.getAspect(propertyName);
		if (aspect != null) {
			if (aspect.getAssociations().isEmpty()) {
				aspect.getAssociations().add(association);
			}
			contentType.getMandatoryAspects().add(propertyName);
		} else {
			contentType.getAssociations().add(association);
		}

		// Add field to form
		form.getFormFieldVisibility().addShowFieldElement(propertyName);

		FormField field = new FormField();
		form.getFormAppearance().addFormAppearanceElement(field);
		field.setId(propertyName);
		field.setSet(formSet);

		// In case the reference references "cm:content", use a "package-items"
		// template instead
		// of the normal document picker, starting from the default location
		if (AlfrescoConversionConstants.CONTENT_TYPE_CONTENT.equals(referenceDefinition.getType())) {
			FormFieldControl control = new FormFieldControl(AlfrescoConversionConstants.FORM_PACKAGE_ITEMS_TEMPLATE);
			control.addControlParameter(AlfrescoConversionConstants.FORM_PACKAGE_ITEMS_PARAM_ROOTNODE,
			    AlfrescoConversionConstants.FORM_PACKAGE_ITEMS_PARAM_ROOTNODE_DEFAULT);
			field.setControl(control);

			// Since we use the "package" template, we need to set the special action-parameters. This might
			// impact the general package actions, but it's the bes we can do without custom coding/template
			addOrAlterPackageItemActions(contentType, true, true);
		}

		// Wire in variable output to process, if needed
		addOutputProperty(referenceDefinition, propertyName, contentType.getName(), conversion);
	}

	protected void addFieldReference(Form form, String formSet, ReferencePropertyDefinition definition,
	    M2Type contentType, WorkflowDefinitionConversion conversion) {
		if (form.isStartForm()) {
			throw new AlfrescoSimpleWorkflowException("Field references cannot be used on start-forms");
		}

		// Check if model contains an aspect for the property
		String propertyName = getPropertyName(definition, conversion);

		// Add property-reference to the context to be validated
		PropertyReference reference = new PropertyReference(definition.getName());
		AlfrescoConversionUtil.getPropertyReferences(conversion).add(reference);

		// Add aspect to content-type
		contentType.getMandatoryAspects().add(propertyName);

		// Add read-only field to form
		form.getFormFieldVisibility().addShowFieldElement(propertyName);

		FormField field = new FormField();
		form.getFormAppearance().addFormAppearanceElement(field);
		field.setId(propertyName);
		field.setLabelId(definition.getName());
		field.setSet(formSet);
		field.setControl(new FormFieldControl(AlfrescoConversionConstants.FORM_READONLY_TEMPLATE));
	}

	protected void addWorkflowDescriptionReference(Form form, String formSet) {
		String fieldName = null;
		if (form.isStartForm()) {
			fieldName = AlfrescoConversionConstants.PROPERTY_WORKFLOW_DESCRIPTION;
		} else {
			fieldName = AlfrescoConversionConstants.PROPERTY_DESCRIPTION;
		}
		form.getFormFieldVisibility().addShowFieldElement(fieldName);

		FormField descriptionField = new FormField();
		descriptionField.setId(fieldName);
		descriptionField.setLabelId(AlfrescoConversionConstants.FORM_WORKFLOW_DESCRIPTION_LABEL);
		descriptionField.setSet(formSet);
		if (form.isStartForm()) {
			descriptionField.setControl(new FormFieldControl(AlfrescoConversionConstants.FORM_MULTILINE_TEXT_TEMPLATE));
		} else {
			descriptionField.setControl(new FormFieldControl(AlfrescoConversionConstants.FORM_READONLY_TEMPLATE));
		}
		form.getFormAppearance().addFormAppearanceElement(descriptionField);
	}
	
	protected void addCommentReference(Form form, String formSet) {
		form.getFormFieldVisibility().addShowFieldElement(AlfrescoConversionConstants.PROPERTY_COMMENT);

		FormField commentField = new FormField();
		commentField.setId(AlfrescoConversionConstants.PROPERTY_COMMENT);
		commentField.setLabelId(AlfrescoConversionConstants.FORM_COMMENT_LABEL);
		commentField.setSet(formSet);
	  commentField.setControl(new FormFieldControl(AlfrescoConversionConstants.FORM_MULTILINE_TEXT_TEMPLATE));
		form.getFormAppearance().addFormAppearanceElement(commentField);
	}

	protected void addPriorityReference(Form form, String formSet, boolean writable) {

		String fieldName = null;
		if (form.isStartForm()) {
			fieldName = AlfrescoConversionConstants.PROPERTY_WORKFLOW_PRIORITY;
		} else {
			fieldName = AlfrescoConversionConstants.PROPERTY_PRIORITY;
		}
		form.getFormFieldVisibility().addShowFieldElement(fieldName);

		FormField priorityField = new FormField();
		priorityField.setSet(formSet);
		priorityField.setLabelId(AlfrescoConversionConstants.FORM_WORKFLOW_PRIORITY_LABEL);
		priorityField.setId(fieldName);

		if (form.isStartForm() || writable) {
			priorityField.setControl(new FormFieldControl(AlfrescoConversionConstants.FORM_PRIORITY_TEMPLATE));
		} else {
			priorityField.setControl(new FormFieldControl(AlfrescoConversionConstants.FORM_READONLY_TEMPLATE));
		}
		form.getFormAppearance().addFormAppearanceElement(priorityField);
	}

	protected void addPackageReference(Form form, String formSet, M2Type contentType,
	    ReferencePropertyDefinition referenceDefinition) {
		form.getFormFieldVisibility().addShowFieldElement(AlfrescoConversionConstants.PROPERTY_PACKAGEITEMS);
		form.getFormAppearance().addFormField(AlfrescoConversionConstants.PROPERTY_PACKAGEITEMS, null, formSet);

		// Only take into account package-actions when we're not dealing with a
		// start-form
		if (!form.isStartForm()) {
			boolean allowAddingItems = extractBooleanFromParameters(referenceDefinition.getParameters(),
			    AlfrescoConversionConstants.PARAMETER_PACKAGEITEMS_ALLOW_ADD, true);
			boolean allowRemovingItems = extractBooleanFromParameters(referenceDefinition.getParameters(),
			    AlfrescoConversionConstants.PARAMETER_PACKAGEITEMS_ALLOW_REMOVE, true);

			addOrAlterPackageItemActions(contentType, allowAddingItems, allowRemovingItems);
		}
	}

	protected void addDueDateReference(Form form, String formSet, boolean writable) {
		String fieldName = null;
		if (form.isStartForm()) {
			fieldName = AlfrescoConversionConstants.PROPERTY_WORKFLOW_DUE_DATE;
		} else {
			fieldName = AlfrescoConversionConstants.PROPERTY_DUE_DATE;
		}

		form.getFormFieldVisibility().addShowFieldElement(fieldName);

		FormField formField = form.getFormAppearance().addFormField(fieldName,
		    AlfrescoConversionConstants.FORM_WORKFLOW_DUE_DATE_LABEL, formSet);
		if (form.isStartForm() || writable) {
			formField.setControl(new FormFieldControl(AlfrescoConversionConstants.FORM_DATE_TEMPLATE));
			formField
			    .getControl()
			    .getControlParameters()
			    .add(
			        new FormFieldControlParameter(AlfrescoConversionConstants.FORM_DATE_PARAM_SHOW_TIME, Boolean.FALSE
			            .toString()));
			formField
			    .getControl()
			    .getControlParameters()
			    .add(
			        new FormFieldControlParameter(AlfrescoConversionConstants.FORM_DATE_PARAM_SUBMIT_TIME, Boolean.FALSE
			            .toString()));
		} else {
			formField.setControl(new FormFieldControl(AlfrescoConversionConstants.FORM_READONLY_TEMPLATE));
		}
		form.getFormAppearance().addFormAppearanceElement(formField);
	}
	
	protected void addEmailNotificationReference(Form form, String formSet, M2Type type, ReferencePropertyDefinition def) {
		// Only relevant on a start-form
		if(form.isStartForm()) {
			boolean forced = extractBooleanFromParameters(def.getParameters(), AlfrescoConversionConstants.PARAMETER_FORCE_NOTOFICATIONS, false);
			if(forced) {
				// Notifications are needed, add an override to the model
				if(type.getPropertyOverride(AlfrescoConversionConstants.PROPERTY_SEND_EMAIL_NOTIFICATIONS) == null) {
					M2PropertyOverride override = new M2PropertyOverride();
					override.setName(AlfrescoConversionConstants.PROPERTY_SEND_EMAIL_NOTIFICATIONS);
					override.setDefaultValue(Boolean.TRUE.toString());
					type.getPropertyOverrides().add(override);
				}
			} else {
				// Render a control to select whether notifications are needed or not
				form.getFormFieldVisibility().addShowFieldElement(AlfrescoConversionConstants.PROPERTY_SEND_EMAIL_NOTIFICATIONS);
				FormField formField = form.getFormAppearance().addFormField(AlfrescoConversionConstants.PROPERTY_SEND_EMAIL_NOTIFICATIONS,
						null, formSet);
				formField.setControl(new FormFieldControl(AlfrescoConversionConstants.FORM_EMAIL_NOTIFICATION_TEMPLATE));
				form.getFormAppearance().addFormAppearanceElement(formField);
			}
		}
	}
	
	protected void addOrAlterPackageItemActions(M2Type contentType, boolean allowAdd, boolean allowRemove) {
		if(allowAdd) {
			M2PropertyOverride addOverride = contentType.getPropertyOverride(AlfrescoConversionConstants.PROPERTY_PACKAGEITEMS_ACTION_GROUP);
			if(addOverride == null) {
				addOverride = new M2PropertyOverride();
				addOverride.setName(AlfrescoConversionConstants.PROPERTY_PACKAGEITEMS_ACTION_GROUP);
				contentType.getPropertyOverrides().add(addOverride);
			}
	  	addOverride.setDefaultValue(AlfrescoConversionConstants.PROPERTY_PACKAGEITEMS_ACTION_GROUP_ADD);
		}
		
		M2PropertyOverride removeOverride = contentType.getPropertyOverride(AlfrescoConversionConstants.PROPERTY_PACKAGEITEMS_ITEM_ACTION_GROUP);
		if(removeOverride == null) {
			removeOverride = new M2PropertyOverride();
			removeOverride.setName(AlfrescoConversionConstants.PROPERTY_PACKAGEITEMS_ITEM_ACTION_GROUP);
			contentType.getPropertyOverrides().add(removeOverride);
		}
		
		if(allowRemove) {
			removeOverride.setDefaultValue(AlfrescoConversionConstants.PROPERTY_PACKAGEITEMS_ITEM_ACTION_GROUP_REMOVE);
		} else {
			removeOverride.setDefaultValue(AlfrescoConversionConstants.PROPERTY_PACKAGEITEMS_ITEM_ACTION_GROUP_EDIT);
		}
	}
}
