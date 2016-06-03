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
package org.activiti.standalone.validation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.test.util.TestProcessUtil;
import org.activiti.validation.ProcessValidator;
import org.activiti.validation.ProcessValidatorFactory;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ValidatorSetNames;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jbarrez
 */
public class DefaultProcessValidatorTest {
	
	protected ProcessValidator processValidator;
	
	@Before
	public void setupProcessValidator() {
		ProcessValidatorFactory processValidatorFactory = new ProcessValidatorFactory();
		this.processValidator = processValidatorFactory.createDefaultProcessValidator();
	}
	
	@Test
	public void verifyValidation() throws Exception {
		
		InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream("org/activiti/engine/test/validation/invalidProcess.bpmn20.xml");
	  XMLInputFactory xif = XMLInputFactory.newInstance();
	  InputStreamReader in = new InputStreamReader(xmlStream, "UTF-8");
	  XMLStreamReader xtr = xif.createXMLStreamReader(in);
	  BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
	  Assert.assertNotNull(bpmnModel);
		
		List<ValidationError> allErrors = processValidator.validate(bpmnModel);
		Assert.assertEquals(65, allErrors.size());
		
		String setName = ValidatorSetNames.ACTIVITI_EXECUTABLE_PROCESS; // shortening it a bit
		
		// isExecutable should be true
		List<ValidationError> problems = findErrors(allErrors, setName, Problems.ALL_PROCESS_DEFINITIONS_NOT_EXECUTABLE, 1);
		Assert.assertNotNull(problems.get(0).getValidatorSetName());
		Assert.assertNotNull(problems.get(0).getProblem());
		Assert.assertNotNull(problems.get(0).getDefaultDescription());
		
		// Event listeners
		problems = findErrors(allErrors, setName, Problems.EVENT_LISTENER_IMPLEMENTATION_MISSING, 1);
		assertProcessElementError(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.EVENT_LISTENER_INVALID_THROW_EVENT_TYPE, 1);
		assertProcessElementError(problems.get(0));
		
		// Execution listeners
		problems = findErrors(allErrors, setName, Problems.EXECUTION_LISTENER_IMPLEMENTATION_MISSING, 2);
		assertProcessElementError(problems.get(0));
		assertCommonProblemFieldForActivity(problems.get(1));
		
		// Association
		problems = findErrors(allErrors, setName, Problems.ASSOCIATION_INVALID_SOURCE_REFERENCE, 1);
		assertProcessElementError(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.ASSOCIATION_INVALID_TARGET_REFERENCE, 1);
		assertProcessElementError(problems.get(0));
		
		// Signals
		problems = findErrors(allErrors, setName, Problems.SIGNAL_MISSING_ID, 1);
		assertCommonErrorFields(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.SIGNAL_MISSING_NAME, 2);
		assertCommonErrorFields(problems.get(0));
		assertCommonErrorFields(problems.get(1));
		problems = findErrors(allErrors, setName, Problems.SIGNAL_DUPLICATE_NAME, 2);
		assertCommonErrorFields(problems.get(0));
		assertCommonErrorFields(problems.get(1));
		problems = findErrors(allErrors, setName, Problems.SIGNAL_INVALID_SCOPE, 1);
		assertCommonErrorFields(problems.get(0));
		
		// Start event
		problems = findErrors(allErrors, setName, Problems.START_EVENT_MULTIPLE_FOUND, 2);
		assertCommonProblemFieldForActivity(problems.get(0));
		assertCommonProblemFieldForActivity(problems.get(1));
		problems = findErrors(allErrors, setName, Problems.START_EVENT_INVALID_EVENT_DEFINITION, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Sequence flow
		problems = findErrors(allErrors, setName, Problems.SEQ_FLOW_INVALID_SRC, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.SEQ_FLOW_INVALID_TARGET, 2);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// User task
		problems = findErrors(allErrors, setName, Problems.USER_TASK_LISTENER_IMPLEMENTATION_MISSING, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Service task 
		problems = findErrors(allErrors, setName, Problems.SERVICE_TASK_RESULT_VAR_NAME_WITH_DELEGATE, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.SERVICE_TASK_INVALID_TYPE, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.SERVICE_TASK_MISSING_IMPLEMENTATION, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.SERVICE_TASK_WEBSERVICE_INVALID_OPERATION_REF, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Send task
		problems = findErrors(allErrors, setName, Problems.SEND_TASK_INVALID_IMPLEMENTATION, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.SEND_TASK_INVALID_TYPE, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.SEND_TASK_WEBSERVICE_INVALID_OPERATION_REF, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Mail task
		problems = findErrors(allErrors, setName, Problems.MAIL_TASK_NO_RECIPIENT, 2);
		assertCommonProblemFieldForActivity(problems.get(0));
		assertCommonProblemFieldForActivity(problems.get(1));
		problems = findErrors(allErrors, setName, Problems.MAIL_TASK_NO_CONTENT, 4);
		assertCommonProblemFieldForActivity(problems.get(0));
		assertCommonProblemFieldForActivity(problems.get(1));
		
		// Shell task
		problems = findErrors(allErrors, setName, Problems.SHELL_TASK_NO_COMMAND, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Script task
		problems = findErrors(allErrors, setName, Problems.SCRIPT_TASK_MISSING_SCRIPT, 2);
		assertCommonProblemFieldForActivity(problems.get(0));
		assertCommonProblemFieldForActivity(problems.get(1));
		
		// Exclusive gateway
		problems = findErrors(allErrors, setName, Problems.EXCLUSIVE_GATEWAY_CONDITION_NOT_ALLOWED_ON_SINGLE_SEQ_FLOW, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.EXCLUSIVE_GATEWAY_CONDITION_ON_DEFAULT_SEQ_FLOW, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.EXCLUSIVE_GATEWAY_NO_OUTGOING_SEQ_FLOW, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.EXCLUSIVE_GATEWAY_SEQ_FLOW_WITHOUT_CONDITIONS, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Event gateway
		problems = findErrors(allErrors, setName, Problems.EVENT_GATEWAY_ONLY_CONNECTED_TO_INTERMEDIATE_EVENTS, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Subprocesses
		problems = findErrors(allErrors, setName, Problems.SUBPROCESS_MULTIPLE_START_EVENTS, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.SUBPROCESS_START_EVENT_EVENT_DEFINITION_NOT_ALLOWED, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Event subprocesses
		problems = findErrors(allErrors, setName, Problems.EVENT_SUBPROCESS_INVALID_START_EVENT_DEFINITION, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Boundary events
		problems = findErrors(allErrors, setName, Problems.BOUNDARY_EVENT_NO_EVENT_DEFINITION, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.BOUNDARY_EVENT_CANCEL_ONLY_ON_TRANSACTION, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.BOUNDARY_EVENT_MULTIPLE_CANCEL_ON_TRANSACTION, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Intermediate catch event
		problems = findErrors(allErrors, setName, Problems.INTERMEDIATE_CATCH_EVENT_NO_EVENTDEFINITION, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.INTERMEDIATE_CATCH_EVENT_INVALID_EVENTDEFINITION, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Intermediate throw event
		problems = findErrors(allErrors, setName, Problems.THROW_EVENT_INVALID_EVENTDEFINITION, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Multi instance
		problems = findErrors(allErrors, setName, Problems.MULTI_INSTANCE_MISSING_COLLECTION, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		
		// Message events
		problems = findErrors(allErrors, setName, Problems.MESSAGE_EVENT_MISSING_MESSAGE_REF, 2);
		assertCommonProblemFieldForActivity(problems.get(0));
		assertCommonProblemFieldForActivity(problems.get(1));
		
		// Signal events
		problems = findErrors(allErrors, setName, Problems.SIGNAL_EVENT_MISSING_SIGNAL_REF, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		problems = findErrors(allErrors, setName, Problems.SIGNAL_EVENT_INVALID_SIGNAL_REF, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Compensate event
		problems = findErrors(allErrors, setName, Problems.COMPENSATE_EVENT_INVALID_ACTIVITY_REF, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Timer event
		problems = findErrors(allErrors, setName, Problems.EVENT_TIMER_MISSING_CONFIGURATION, 2);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Data association
		problems = findErrors(allErrors, setName, Problems.DATA_ASSOCIATION_MISSING_TARGETREF, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Data object
		problems = findErrors(allErrors, setName, Problems.DATA_OBJECT_MISSING_NAME, 2);
		assertCommonErrorFields(problems.get(0));
		assertCommonErrorFields(problems.get(1));
		
		// End event
		problems = findErrors(allErrors, setName, Problems.END_EVENT_CANCEL_ONLY_INSIDE_TRANSACTION, 1);
		assertCommonProblemFieldForActivity(problems.get(0));
		
		// Messages
		problems = findErrors(allErrors, setName, Problems.MESSAGE_INVALID_ITEM_REF, 1);
		assertCommonErrorFields(problems.get(0));

	}
	
	@Test
	public void testWarningError() throws UnsupportedEncodingException, XMLStreamException {
		String flowWithoutConditionNoDefaultFlow = "<?xml version='1.0' encoding='UTF-8'?>" +
        "<definitions id='definitions' xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:activiti='http://activiti.org/bpmn' targetNamespace='Examples'>" +
        "  <process id='exclusiveGwDefaultSequenceFlow'> " + 
        "    <startEvent id='theStart' /> " + 
        "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='exclusiveGw' /> " + 
        
        "    <exclusiveGateway id='exclusiveGw' name='Exclusive Gateway' /> " + // no default = "flow3" !!
        "    <sequenceFlow id='flow2' sourceRef='exclusiveGw' targetRef='theTask1'> " + 
        "      <conditionExpression xsi:type='tFormalExpression'>${input == 1}</conditionExpression> " + 
        "    </sequenceFlow> " + 
        "    <sequenceFlow id='flow3' sourceRef='exclusiveGw' targetRef='theTask2'/> " +  // one would be OK
        "    <sequenceFlow id='flow4' sourceRef='exclusiveGw' targetRef='theTask2'/> " +  // but two unconditional not!

        "    <userTask id='theTask1' name='Input is one' /> " + 
        "    <userTask id='theTask2' name='Default input' /> " + 
        "  </process>" + 
        "</definitions>";    

	    XMLInputFactory xif = XMLInputFactory.newInstance();
	    InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(flowWithoutConditionNoDefaultFlow.getBytes()), "UTF-8");
	    XMLStreamReader xtr = xif.createXMLStreamReader(in);
	    BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
	    Assert.assertNotNull(bpmnModel);
	    List<ValidationError> allErrors = processValidator.validate(bpmnModel);
	    Assert.assertEquals(1, allErrors.size());
	    Assert.assertTrue(allErrors.get(0).isWarning());
	}
	
	/*
	 * Test for https://activiti.atlassian.net/browse/ACT-2071:
	 * 
	 * If all processes in a deployment are not executable, throw an exception
	 * as this doesn't make sense to do.
	 */
	@Test
	public void testAllNonExecutableProcesses() {
		BpmnModel bpmnModel = new BpmnModel();
		for (int i=0; i<5; i++) {
			org.activiti.bpmn.model.Process process = TestProcessUtil.createOneTaskProcess();
			process.setExecutable(false);
			bpmnModel.addProcess(process);
		}
		
		List<ValidationError> errors = processValidator.validate(bpmnModel);
		Assert.assertEquals(1, errors.size());
	}
	
	/*
	 * Test for https://activiti.atlassian.net/browse/ACT-2071:
	 * 
	 * If there is at least one process definition which is executable, 
	 * and the deployment contains other process definitions which are not executable,
	 * then add a warning for those non executable process definitions
	 */
	@Test
	public void testNonExecutableProcessDefinitionWarning() {
		BpmnModel bpmnModel = new BpmnModel();
		
		// 3 non-executables
		for (int i=0; i<3; i++) {
			org.activiti.bpmn.model.Process process = TestProcessUtil.createOneTaskProcess();
			process.setExecutable(false);
			bpmnModel.addProcess(process);
		}
		
		// 1 executables
		org.activiti.bpmn.model.Process process = TestProcessUtil.createOneTaskProcess();
		process.setExecutable(true);
		bpmnModel.addProcess(process);
		
		List<ValidationError> errors = processValidator.validate(bpmnModel);
		Assert.assertEquals(3, errors.size());
		for (ValidationError error : errors) {
			Assert.assertTrue(error.isWarning());
			Assert.assertNotNull(error.getValidatorSetName());
			Assert.assertNotNull(error.getProblem());
			Assert.assertNotNull(error.getDefaultDescription());
		}
	}
	
	
	protected void assertCommonProblemFieldForActivity(ValidationError error) {
		assertProcessElementError(error);
		
		Assert.assertNotNull(error.getActivityId());
		Assert.assertNotNull(error.getActivityName());
		
		Assert.assertTrue(error.getActivityId().length() > 0);
		Assert.assertTrue(error.getActivityName().length() > 0);
	}
	
	protected void assertCommonErrorFields(ValidationError error) {
	  Assert.assertNotNull(error.getValidatorSetName());
		Assert.assertNotNull(error.getProblem());
		Assert.assertNotNull(error.getDefaultDescription());
		Assert.assertTrue(error.getXmlLineNumber() > 0);
		Assert.assertTrue(error.getXmlColumnNumber() > 0);
  }

	protected void assertProcessElementError(ValidationError error) {
		assertCommonErrorFields(error);
		Assert.assertEquals("invalidProcess", error.getProcessDefinitionId());
		Assert.assertEquals("The invalid process", error.getProcessDefinitionName());
  }
	
	protected List<ValidationError> findErrors(List<ValidationError> errors, String validatorSetName, String problemName, int expectedNrOfProblems) {
		List<ValidationError> results = findErrors(errors, validatorSetName, problemName);
		Assert.assertEquals(expectedNrOfProblems, results.size());
		for (ValidationError result : results) {
			Assert.assertEquals(validatorSetName, result.getValidatorSetName());
			Assert.assertEquals(problemName, result.getProblem());
		}
		return results;
	}
	
	protected List<ValidationError> findErrors(List<ValidationError> errors, String validatorSetName, String problemName) {
		List<ValidationError> results = new ArrayList<ValidationError>();
		for (ValidationError error : errors) {
			if (error.getValidatorSetName().equals(validatorSetName) && error.getProblem().equals(problemName)) {
				results.add(error);
			}
		}
		return results;
	}
	
}
