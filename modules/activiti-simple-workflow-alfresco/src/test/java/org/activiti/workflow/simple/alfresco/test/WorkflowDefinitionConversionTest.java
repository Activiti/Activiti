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
package org.activiti.workflow.simple.alfresco.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionConstants;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionUtil;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoWorkflowDefinitionConversionFactory;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.M2Property;
import org.activiti.workflow.simple.alfresco.model.M2Type;
import org.activiti.workflow.simple.alfresco.model.config.Configuration;
import org.activiti.workflow.simple.alfresco.model.config.Module;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.definition.form.FormPropertyGroup;
import org.activiti.workflow.simple.definition.form.TextPropertyDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WorkflowDefinitionConversionTest {

	protected AlfrescoWorkflowDefinitionConversionFactory conversionFactory;
	
	@Before
	public void init() {
		conversionFactory = new AlfrescoWorkflowDefinitionConversionFactory();
	}
	
	/**
	 * Check if all required artifacts are created when converting an empty workflow-definition.
	 */
	@Test
	public void testEmptyWorkflowDefinitionConversion() {
		WorkflowDefinition definition = new WorkflowDefinition();
		definition.setDescription("This is the description");
		definition.setId("workflowdefinition");
		definition.setName("My workflow definition");
		
		WorkflowDefinitionConversion conversion = conversionFactory.createWorkflowDefinitionConversion(definition);
		conversion.convert();
		
		BpmnModel bpmnModel = conversion.getBpmnModel();
		assertNotNull(bpmnModel);
		
		Process process = bpmnModel.getMainProcess();
		assertNotNull(process);
		assertEquals("This is the description", process.getDocumentation());
		assertEquals("My workflow definition", process.getName());
		assertEquals("workflowdefinition", process.getId());
		
		// Default start-task key should be used, as no custom startform-config is present
		boolean startTaskFound = false;
		for(FlowElement element : process.getFlowElements()) {
			if(element instanceof StartEvent) {
				assertEquals("bpm:activitiStartTask", ((StartEvent) element).getFormKey());
				startTaskFound = true;
			}
		}
		assertTrue(startTaskFound);
		
		// Check presence of content-model
		M2Model contentModel = AlfrescoConversionUtil.getContentModel(conversion);
		assertNotNull(contentModel);
		
		// Check presence of form-config and default workflow-details
		Module module = AlfrescoConversionUtil.getModule(conversion);
		assertNotNull(module);
		assertEquals(1L, module.getConfigurations().size());
		
		Configuration config = module.getConfigurations().get(0);
		assertEquals(1L, config.getForms().size());
		assertEquals("activiti$workflowdefinition", config.getCondition());
		assertEquals(AlfrescoConversionConstants.EVALUATOR_STRING_COMPARE, config.getEvaluator());
	}
	
	/**
	 * Check if all required artifacts are created when converting an empty workflow-definition.
	 */
	@Test
	public void testGeneratedWorkflowDefinitionId() {
		WorkflowDefinition definition = new WorkflowDefinition();
		
		WorkflowDefinitionConversion conversion = conversionFactory.createWorkflowDefinitionConversion(definition);
		conversion.convert();
		
		BpmnModel bpmnModel = conversion.getBpmnModel();
		assertNotNull(bpmnModel);
		
		Process process = bpmnModel.getMainProcess();
		assertNotNull(process);
		
		String generatedProcessId = process.getId();
		assertNotNull(generatedProcessId);
	}
	
	/**
	 * Check artifact export.
	 */
	@Test
	public void testExportArtifacts() throws Exception {
		WorkflowDefinition definition = new WorkflowDefinition();
		HumanStepDefinition humanStep = new HumanStepDefinition();
		humanStep.setAssignee("fred");
		humanStep.setId("step 1");
		FormDefinition form = new FormDefinition();
		form.setFormKey("wf:activitiAdhoc");
		humanStep.setForm(form);
		
		definition.addStep(humanStep);
		
		WorkflowDefinitionConversion conversion = conversionFactory.createWorkflowDefinitionConversion(definition);
		conversion.convert();
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		conversionFactory.getArtifactExporter().writeBpmnModel(stream, conversion);
		byte[] byteArray = stream.toByteArray();
		assertTrue(byteArray.length > 0);
		
		stream = new ByteArrayOutputStream();
		conversionFactory.getArtifactExporter().writeContentModel(stream, conversion);
		byteArray = stream.toByteArray();
		assertTrue(byteArray.length > 0);
		
		stream = new ByteArrayOutputStream();
		conversionFactory.getArtifactExporter().writeShareConfig(stream, conversion, true);
		byteArray = stream.toByteArray();
		assertTrue(byteArray.length > 0);
		
		stream = new ByteArrayOutputStream();
		conversionFactory.getArtifactExporter().writeShareConfig(stream, conversion, false);
		byteArray = stream.toByteArray();
		assertTrue(byteArray.length > 0);
	}
	
	/**
	 * Test basic form-fields (text, number, date, list, ...)
	 */
	@Test
	public void testHumanStepBasicFormField() throws Exception {
		// TODO: finish test once all types are present
		WorkflowDefinition definition = new WorkflowDefinition();
		definition.setId("process");
		
		HumanStepDefinition humanStep = new HumanStepDefinition();
		humanStep.setId("step1");
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
		
		WorkflowDefinitionConversion conversion = conversionFactory.createWorkflowDefinitionConversion(definition);
		conversion.convert();
		
		// Check content-model
		M2Model model = AlfrescoConversionUtil.getContentModel(conversion);
		assertNotNull(model);
		M2Type type = model.getTypes().get(0);
		assertNotNull(type);
		
		// Simple text
		M2Property property = getPropertyFromType("text", type);
		assertEquals("d:text", property.getPropertyType());
		assertEquals(Boolean.TRUE, property.getMandatory().isMandatory());
	}

	protected M2Property getPropertyFromType(String shortName, M2Type type) {
		for(M2Property prop : type.getProperties()) {
			if(prop.getName().endsWith(shortName)) {
				return prop;
			}
		}
		Assert.fail("No property found for the given name: " + shortName);
		return null;
  }
}
