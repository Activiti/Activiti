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

import org.activiti.workflow.simple.alfresco.conversion.AlfrescoWorkflowDefinitionConversionFactory;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.definition.form.FormPropertyGroup;
import org.activiti.workflow.simple.definition.form.TextPropertyDefinition;

public class ExportMain {

	public static void main(String[] args) {
		AlfrescoWorkflowDefinitionConversionFactory factory = new AlfrescoWorkflowDefinitionConversionFactory();
		
		WorkflowDefinition definition = new WorkflowDefinition();
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
		
		definition.addStep(humanStep);
		
		WorkflowDefinitionConversion conversion = factory.createWorkflowDefinitionConversion(definition);
		conversion.convert();
		
		File repoFolder = new File("target/repo");
		repoFolder.mkdir();
		
		File shareFolder = new File("target/share");
		shareFolder.mkdir();
		
		factory.getArtifactExporter().exportArtifacts(conversion, repoFolder, shareFolder);
	}

}
