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
package org.activiti.workflow.simple.alfresco.export;

import java.io.File;

import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionConstants;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoWorkflowDefinitionConversionFactory;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.activiti.workflow.simple.definition.form.DatePropertyDefinition;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.definition.form.FormPropertyGroup;
import org.activiti.workflow.simple.definition.form.ListPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ListPropertyEntry;
import org.activiti.workflow.simple.definition.form.NumberPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ReferencePropertyDefinition;
import org.activiti.workflow.simple.definition.form.TextPropertyDefinition;

public class ExportMain {

	public static void main(String[] args) {
		AlfrescoWorkflowDefinitionConversionFactory factory = new AlfrescoWorkflowDefinitionConversionFactory();
		
		WorkflowDefinition definition = new WorkflowDefinition();
		definition.setName("Custom workflow");
		definition.setId("process");
		
		HumanStepDefinition humanStep = new HumanStepDefinition();
		humanStep.setId("step1");
		humanStep.setAssignee("admin");
		FormDefinition form = new FormDefinition();
		humanStep.setForm(form);
		
		FormPropertyGroup group = new FormPropertyGroup();
		group.setId("group");
		group.setTitle("My group");
		humanStep.getForm().addFormPropertyGroup(group);
		
		// Add simple text
		TextPropertyDefinition textProperty = new TextPropertyDefinition();
		textProperty.setName("text");
		textProperty.setMandatory(true);
		group.addFormProperty(textProperty);
		
		ReferencePropertyDefinition taskPackageItem = new ReferencePropertyDefinition();
		taskPackageItem.getParameters().put(AlfrescoConversionConstants.PARAMETER_PACKAGEITEMS_ALLOW_ADD, false);
		taskPackageItem.getParameters().put(AlfrescoConversionConstants.PARAMETER_PACKAGEITEMS_ALLOW_REMOVE, false);
		
		taskPackageItem.setType(AlfrescoConversionConstants.FORM_REFERENCE_PACKAGE_ITEMS);
		group.addFormProperty(taskPackageItem);
		
		definition.addStep(humanStep);
		
		FormDefinition startForm = new FormDefinition();
		definition.setStartFormDefinition(startForm);
		
		FormPropertyGroup startGroup = new FormPropertyGroup();
		startGroup.setId("group");
		startGroup.setTitle("My group");
		startForm.addFormPropertyGroup(startGroup);
		
		// Add simple text
		TextPropertyDefinition startTextProp = new TextPropertyDefinition();
		startTextProp.setName("text");
		startTextProp.setMandatory(true);
		startGroup.addFormProperty(startTextProp);
		
		// Add list box
		ListPropertyDefinition listProp = new ListPropertyDefinition();
		listProp.setName("Gender");
		listProp.getEntries().add(new ListPropertyEntry("Man", "m"));
		listProp.getEntries().add(new ListPropertyEntry("Vrouw", "v"));
		startGroup.addFormProperty(listProp);
		
		
		FormPropertyGroup anotherGroup = new FormPropertyGroup();
		anotherGroup.setType(AlfrescoConversionConstants.FORM_GROUP_LAYOUT_2_COLUMNS);
		anotherGroup.setId("other");
		anotherGroup.setTitle("Another group");
		startForm.addFormPropertyGroup(anotherGroup);
		
		// Add date
		DatePropertyDefinition dateProp = new DatePropertyDefinition();
		dateProp.setName("Start date");
		anotherGroup.addFormProperty(dateProp);
		
		// Add number
		NumberPropertyDefinition numberProp = new NumberPropertyDefinition();
		numberProp.setName("Amount");
		anotherGroup.addFormProperty(numberProp);
		
		FormPropertyGroup defaultGroup = new FormPropertyGroup("default",AlfrescoConversionConstants.FORM_GROUP_LAYOUT_3_COLUMNS,"Default fields");
		startForm.addFormPropertyGroup(defaultGroup);
		
		ReferencePropertyDefinition dueDateField = new ReferencePropertyDefinition();
		dueDateField.setType(AlfrescoConversionConstants.FORM_REFERENCE_DUEDATE);
		defaultGroup.addFormProperty(dueDateField);
		ReferencePropertyDefinition priorityGroup = new ReferencePropertyDefinition();
		priorityGroup.setType(AlfrescoConversionConstants.FORM_REFERENCE_PRIORITY);
		defaultGroup.addFormProperty(priorityGroup);
		ReferencePropertyDefinition messageField = new ReferencePropertyDefinition();
		messageField.setType(AlfrescoConversionConstants.FORM_REFERENCE_WORKFLOW_DESCRIPTION);
		defaultGroup.addFormProperty(messageField);
		
	// Add package items
			ReferencePropertyDefinition packageItem = new ReferencePropertyDefinition();
			packageItem.setType(AlfrescoConversionConstants.FORM_REFERENCE_PACKAGE_ITEMS);
			defaultGroup.addFormProperty(packageItem);
		
		WorkflowDefinitionConversion conversion = factory.createWorkflowDefinitionConversion(definition);
		conversion.convert();
		
		File repoFolder = new File("/development/alfresco/projects/head/software/tomcat/shared/classes/alfresco/extension");
		
		File shareFolder = new File("/development/alfresco/projects/head/software/tomcat-app/shared/classes/alfresco/web-extension");
		
		factory.getArtifactExporter().exportArtifacts(conversion, repoFolder, shareFolder);
	}

}
