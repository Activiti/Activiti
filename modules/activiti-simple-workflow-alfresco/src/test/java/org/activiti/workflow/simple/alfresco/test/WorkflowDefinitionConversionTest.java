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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionConstants;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionUtil;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoWorkflowDefinitionConversionFactory;
import org.activiti.workflow.simple.alfresco.conversion.script.PropertyReference;
import org.activiti.workflow.simple.alfresco.form.AlfrescoTransitionsPropertyDefinition;
import org.activiti.workflow.simple.alfresco.model.M2ClassAssociation;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.M2Property;
import org.activiti.workflow.simple.alfresco.model.M2Type;
import org.activiti.workflow.simple.alfresco.model.config.Configuration;
import org.activiti.workflow.simple.alfresco.model.config.Module;
import org.activiti.workflow.simple.alfresco.step.AlfrescoEmailStepDefinition;
import org.activiti.workflow.simple.alfresco.step.AlfrescoReviewStepDefinition;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.HumanStepAssignment.HumanStepAssignmentType;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.definition.form.FormPropertyGroup;
import org.activiti.workflow.simple.definition.form.ListPropertyEntry;
import org.activiti.workflow.simple.definition.form.ReferencePropertyDefinition;
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
				assertEquals("bpm:startTask", ((StartEvent) element).getFormKey());
				startTaskFound = true;
			}
		}
		assertTrue(startTaskFound);
		
		// Check presence of content-model
		M2Model contentModel = AlfrescoConversionUtil.getContentModel(conversion);
		assertNotNull(contentModel);
		
		// Check presence of form-config and default workflow-details
		Module module = AlfrescoConversionUtil.getExtension(conversion).getModules().get(0);
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
	
	@Test
	public void testTaskListenerForIncomingProperties() throws Exception {
		WorkflowDefinition definition = new WorkflowDefinition();
		definition.setId("process");
		
		HumanStepDefinition humanStep = new HumanStepDefinition();
		humanStep.setId("step1");
		FormDefinition form = new FormDefinition();
		form.setFormKey("myform");
		humanStep.setForm(form);
		
		definition.addStep(humanStep);
		
		WorkflowDefinitionConversion conversion = conversionFactory.createWorkflowDefinitionConversion(definition);
		conversion.convert();
		
		Process process = conversion.getProcess();
		assertNotNull(process);
		
		boolean listenerFound = false;
		for(FlowElement flowElement : process.getFlowElements()) {
			if(flowElement instanceof UserTask) {
					UserTask task = (UserTask) flowElement;
					assertNotNull(task.getTaskListeners());
					assertEquals(1L, task.getTaskListeners().size());
					assertEquals("create", task.getTaskListeners().get(0).getEvent());
					listenerFound = true;
			}
		}
		assertTrue(listenerFound);
	}
	
	@Test
	public void testTaskListenerForOutgoingProperties() throws Exception {
		WorkflowDefinition definition = new WorkflowDefinition();
		definition.setId("process");
		
		HumanStepDefinition humanStep = new HumanStepDefinition();
		humanStep.setId("step1");
		FormDefinition form = new FormDefinition();
		
		humanStep.setForm(form);
		
		TextPropertyDefinition text = new TextPropertyDefinition();
		text.setName("my text");
		
		text.getParameters().put(AlfrescoConversionConstants.PARAMETER_ADD_PROPERTY_TO_OUTPUT, true);
		FormPropertyGroup group = new FormPropertyGroup();
		group.setId("group");
		form.getFormGroups().add(group);
		group.getFormPropertyDefinitions().add(text);
		
		definition.addStep(humanStep);
		
		WorkflowDefinitionConversion conversion = conversionFactory.createWorkflowDefinitionConversion(definition);
		conversion.convert();
		
		Process process = conversion.getProcess();
		assertNotNull(process);
		
		boolean listenerFound = false;
		for(FlowElement flowElement : process.getFlowElements()) {
			if(flowElement instanceof UserTask) {
					UserTask task = (UserTask) flowElement;
					assertNotNull(task.getTaskListeners());
					assertEquals(2L, task.getTaskListeners().size());
					assertEquals("create", task.getTaskListeners().get(0).getEvent());
					assertEquals("complete", task.getTaskListeners().get(1).getEvent());
					listenerFound = true;
			}
		}
		assertTrue(listenerFound);
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
	
	/**
	 * Test if a custom reference (eg. type=cm:person) is turned into an association on the
	 * model.
	 */
	@Test
	public void testCustomReference() throws Exception {
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
		ReferencePropertyDefinition textProperty = new ReferencePropertyDefinition();
		textProperty.setName("person");
		textProperty.setMandatory(true);
		textProperty.setType("cm:person");
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
		M2ClassAssociation association = getAssociationFromType("person", type);
		assertEquals("cm:person", association.getTarget().getClassName());
		assertTrue(association.getTarget().isMandatory());
	}
	
	@Test
	public void testConvertEmailStep() throws Exception {
		WorkflowDefinition definition = new WorkflowDefinition();
		AlfrescoEmailStepDefinition emailStep = new AlfrescoEmailStepDefinition();
		emailStep.setTo("fred");
		emailStep.setSubject("jos");
		
		definition.addStep(emailStep);
		
		WorkflowDefinitionConversion conversion = conversionFactory.createWorkflowDefinitionConversion(definition);
		conversion.convert();
		
		// Process should contain a single service-task
		ServiceTask task = null;
		
		for(FlowElement element : conversion.getProcess().getFlowElements()) {
			if(element instanceof ServiceTask) {
				if(task != null) {
					Assert.fail("More than one service-task found");
				}
				task = (ServiceTask) element;
			}
		}
		
		assertNotNull(task);
		assertEquals(AlfrescoConversionConstants.CLASSNAME_SCRIPT_DELEGATE, task.getImplementation());
	}
	
	@Test
	public void testPropertyReferenceParsing() throws Exception {
		String absoluteReference = "{{Property Name}}";
		
		assertTrue(PropertyReference.isPropertyReference(absoluteReference));
		assertFalse(PropertyReference.isPropertyReference("{{incomplete}"));
		assertTrue(PropertyReference.containsPropertyReference(absoluteReference));
		assertEquals("test_propertyname", PropertyReference.createReference(absoluteReference).getVariableReference("test"));
		
		String referenceWithProperties = "{{Property Name.test}}";
		
		assertTrue(PropertyReference.isPropertyReference(referenceWithProperties));
		assertTrue(PropertyReference.containsPropertyReference(referenceWithProperties));
		assertEquals("test_propertyname.test", PropertyReference.createReference(referenceWithProperties).getVariableReference("test"));

		absoluteReference = "{{Property Name.}}";
		assertTrue(PropertyReference.isPropertyReference(absoluteReference));
		assertFalse(PropertyReference.isPropertyReference("{{incomplete}"));
		assertTrue(PropertyReference.containsPropertyReference(absoluteReference));
		assertEquals("test_propertyname", PropertyReference.createReference(absoluteReference).getVariableReference("test"));
		
		
		String referenceInText = "This is a {{reference}}";
		assertEquals("This is a ${test_reference}", PropertyReference.replaceAllPropertyReferencesInString(referenceInText, "test", new ArrayList<PropertyReference>(), true));
		assertEquals("This is a test_reference", PropertyReference.replaceAllPropertyReferencesInString(referenceInText, "test", new ArrayList<PropertyReference>(), false));
		
	}
	
	@Test
	public void testTransitionProperty() throws Exception {
		WorkflowDefinition definition = new WorkflowDefinition();
		definition.setId("process");
		
		HumanStepDefinition humanStep = new HumanStepDefinition();
		humanStep.setId("step1");
		FormDefinition form = new FormDefinition();
		humanStep.setForm(form);
		
		AlfrescoTransitionsPropertyDefinition transition = new AlfrescoTransitionsPropertyDefinition();
		transition.addEntry(new ListPropertyEntry("One", "One"));
		transition.addEntry(new ListPropertyEntry("Two", "Two"));
		humanStep.getForm().addFormProperty(transition);
		
		definition.addStep(humanStep);
		
		WorkflowDefinitionConversion conversion = conversionFactory.createWorkflowDefinitionConversion(definition);
		conversion.convert();

		M2Model model = AlfrescoConversionUtil.getContentModel(conversion);
		assertEquals(1L, model.getTypes().size());
		
		M2Type taskType = model.getTypes().get(0);
		assertEquals(1L, taskType.getPropertyOverrides().size());
		assertEquals("bpm:outcomePropertyName", taskType.getPropertyOverrides().get(0).getName());
		assertTrue(taskType.getPropertyOverrides().get(0).getDefaultValue().contains("step1transitions"));
		
		assertEquals(1L, taskType.getProperties().size());
		assertEquals(1L, model.getConstraints().size());
	}
	
	@Test
	public void testReviewStep() throws Exception {
		WorkflowDefinition definition = new WorkflowDefinition();
		definition.setId("process");
		
		AlfrescoReviewStepDefinition review = new AlfrescoReviewStepDefinition();
		review.setName("Review");
		review.setAssignmentPropertyName("bpm:people");
		review.setAssignmentType(HumanStepAssignmentType.USERS);
		review.setRequiredApprovalCount("1");
		AlfrescoEmailStepDefinition emailStepDefinition = new AlfrescoEmailStepDefinition();
		emailStepDefinition.setName("Send rejection email");
		review.getRejectionSteps().add(emailStepDefinition);
		definition.addStep(review);
		
		WorkflowDefinitionConversion conversion = conversionFactory.createWorkflowDefinitionConversion(definition);
		conversion.convert();
		new File("target/repo").mkdir();
		new File("target/share").mkdir();
		conversionFactory.getArtifactExporter().exportArtifacts(conversion, new File("target/repo"), new File("target/share"), false);
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
	
	protected M2ClassAssociation getAssociationFromType(String shortName, M2Type type) {
		for(M2ClassAssociation assoc : type.getAssociations()) {
			if(assoc.getName().endsWith(shortName)) {
				return assoc;
			}
		}
		Assert.fail("No association found for the given name: " + shortName);
		return null;
  }
}
