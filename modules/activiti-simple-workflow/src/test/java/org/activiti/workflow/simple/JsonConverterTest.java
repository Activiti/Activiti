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
package org.activiti.workflow.simple;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;

import org.activiti.workflow.simple.converter.json.SimpleWorkflowJsonConverter;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ListPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ListPropertyEntry;
import org.activiti.workflow.simple.definition.form.NumberPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ReferencePropertyDefinition;
import org.activiti.workflow.simple.definition.form.TextPropertyDefinition;
import org.junit.BeforeClass;
import org.junit.Test;

public class JsonConverterTest {
	
	private static SimpleWorkflowJsonConverter converter;
	
	@BeforeClass
	public static void init() {
		converter = new SimpleWorkflowJsonConverter();
	}

	@Test
	public void testWorkflowDefinitionConversion() {
		// Create definition
		WorkflowDefinition workflowDefinition = new WorkflowDefinition().name("testWorkflow")
		    .description("This is a test workflow").id("the id").key("the key");

		// Write result to byte-array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(baos);
		converter.writeWorkflowDefinition(workflowDefinition, writer);

		// Parse definition based on written JSON
		WorkflowDefinition parsedDefinition = converter.readWorkflowDefinition(baos.toByteArray());

		// Check if parsed definition matches the original one
		assertEquals(workflowDefinition.getName(), parsedDefinition.getName());
		assertEquals(workflowDefinition.getKey(), parsedDefinition.getKey());
		assertEquals(workflowDefinition.getId(), parsedDefinition.getId());
		assertEquals(workflowDefinition.getDescription(), parsedDefinition.getDescription());
	}

	@Test
	public void testHumanStepConversion() {
		WorkflowDefinition workflowDefinition = new WorkflowDefinition().name("testWorkflow").addHumanStep("step1", "fred")
		    .addHumanStepForGroup("step2", Collections.singletonList("group")).addHumanStepForWorkflowInitiator("step3");

		// Add form to last step
		HumanStepDefinition stepWithForm = new HumanStepDefinition();
		stepWithForm.setName("step4");
		stepWithForm.setDescription("Step description");
		
		workflowDefinition.getSteps().add(stepWithForm);
		FormDefinition formDefinition = new FormDefinition();
		stepWithForm.setForm(formDefinition);
		formDefinition.setFormKey("123");
		
		TextPropertyDefinition textProp = new TextPropertyDefinition();
		textProp.setMandatory(true);
		textProp.setName("textProp");
		textProp.setWritable(false);
		formDefinition.addFormProperty(textProp);
		textProp.getParameters().put("custom-parameter", "This is a test");
		
		NumberPropertyDefinition numberProp = new NumberPropertyDefinition();
		numberProp.setMandatory(true);
		numberProp.setName("numberProp");
		numberProp.setWritable(false);
		formDefinition.addFormProperty(numberProp);
		
		ReferencePropertyDefinition reference = new ReferencePropertyDefinition();
		reference.setMandatory(true);
		reference.setName("referenceProp");
		reference.setWritable(false);
		reference.setType("referencedType");
		formDefinition.addFormProperty(reference);
		
		ListPropertyDefinition itemType = new ListPropertyDefinition();
		itemType.setMandatory(true);
		itemType.setName("referenceProp");
		itemType.setWritable(false);
		itemType.addEntry(new ListPropertyEntry("1", "Item 1"));
		itemType.addEntry(new ListPropertyEntry("2", "Item 2"));
		formDefinition.addFormProperty(itemType);
		
		// Write result to byte-array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(baos);
		converter.writeWorkflowDefinition(workflowDefinition, writer);
		
		// Parse definition based on written JSON
		WorkflowDefinition parsedDefinition = converter.readWorkflowDefinition(baos.toByteArray());
		assertEquals(workflowDefinition.getSteps().size(), parsedDefinition.getSteps().size());

		int index = 0;
		for(StepDefinition stepDefinition : parsedDefinition.getSteps()) {
			assertTrue(stepDefinition instanceof HumanStepDefinition);
			HumanStepDefinition humanStep = (HumanStepDefinition) stepDefinition;
			HumanStepDefinition originalStep = (HumanStepDefinition) workflowDefinition.getSteps().get(index);
			
			// Check general human-step fields
			assertEquals(originalStep.getAssignee(), humanStep.getAssignee());
			assertEquals(originalStep.getAssignmentType(), humanStep.getAssignmentType());
			assertEquals(originalStep.getCandidateGroups(), humanStep.getCandidateGroups());
			assertEquals(originalStep.getCandidateUsers(), humanStep.getCandidateUsers());
			assertEquals(originalStep.getName(), humanStep.getName());
			assertEquals(originalStep.getDescription(), humanStep.getDescription());
			
			if(originalStep.getForm() != null) {
				// Encountered step with form attached to it, should be last step
				assertEquals(3, index);
				assertEquals("123", humanStep.getForm().getFormKey());
				assertEquals(originalStep.getForm().getFormPropertyDefinitions().size(), 
						humanStep.getForm().getFormPropertyDefinitions().size());
				
				// Check form-fields, generic fields
				for(int i=0; i<originalStep.getForm().getFormPropertyDefinitions().size(); i++) {
					FormPropertyDefinition origDef = originalStep.getForm().getFormPropertyDefinitions().get(i);
					FormPropertyDefinition parsedDef = humanStep.getForm().getFormPropertyDefinitions().get(i);
					
					assertEquals(origDef.getName(), parsedDef.getName());
					assertEquals(origDef.isMandatory(), parsedDef.isMandatory());
					assertEquals(origDef.isWritable(), parsedDef.isWritable());
					assertEquals(origDef.getClass(), parsedDef.getClass());
					
					if(parsedDef instanceof TextPropertyDefinition) {
						assertTrue(parsedDef.getParameters() != null);
						assertEquals(1L, parsedDef.getParameters().size());
						assertEquals("This is a test", parsedDef.getParameters().get("custom-parameter"));
					}
				}
			}
			index++;
		}
	}
}
