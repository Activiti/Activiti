/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.standalone.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;


public class DefaultProcessValidatorTest {

  private ProcessValidator processValidator;

  @Before
  public void setupProcessValidator() {
    ProcessValidatorFactory processValidatorFactory = new ProcessValidatorFactory();
    this.processValidator = processValidatorFactory.createDefaultProcessValidator();
  }

  @Test
  public void verifyValidation() throws Exception {

      BpmnModel bpmnModel = readModel(
          "org/activiti/engine/test/validation/invalidProcess.bpmn20.xml");
      assertThat(bpmnModel).isNotNull();

    List<ValidationError> allErrors = processValidator.validate(bpmnModel);
    assertThat(allErrors).hasSize(66);

    String setName = ValidatorSetNames.ACTIVITI_EXECUTABLE_PROCESS; // shortening
                                                                    // it a
                                                                    // bit

    // isExecutable should be true
    List<ValidationError> problems = findErrors(allErrors, setName, Problems.ALL_PROCESS_DEFINITIONS_NOT_EXECUTABLE, 1);
    assertThat(problems.get(0).getValidatorSetName()).isNotNull();
    assertThat(problems.get(0).getProblem()).isNotNull();
    assertThat(problems.get(0).getDefaultDescription()).isNotNull();

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

    //Conditional expression
      problems = findErrors(allErrors, setName, Problems.SEQ_FLOW_INVALID_CONDITIONAL_EXPRESSION, 1);
      assertCommonErrorFields(problems.get(0));

  }

    @Test
    public void testWarningError() throws Exception {
        BpmnModel bpmnModel = readModel(
            "org/activiti/engine/test/validation/flowWithoutConditionNoDefaultFlow.bpmn20.xml");
        assertThat(bpmnModel).isNotNull();

        assertThat(bpmnModel).isNotNull();
        List<ValidationError> allErrors = processValidator.validate(bpmnModel);
        assertThat(allErrors).hasSize(1);
        assertThat(allErrors.get(0).isWarning()).isTrue();
    }

    @Test
    public void testValidMessageFlow() throws Exception {
        BpmnModel bpmnModel = readModel(
            "org/activiti/engine/test/validation/validMessageProcess.bpmn20.xml");
        assertThat(bpmnModel).isNotNull();

        assertThat(bpmnModel).isNotNull();
        List<ValidationError> allErrors = processValidator.validate(bpmnModel);
        assertThat(allErrors).isEmpty();
    }

    private BpmnModel readModel(String modelPath)
        throws XMLStreamException, IOException {
        try (InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream(
            modelPath)) {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            InputStreamReader in = new InputStreamReader(xmlStream, StandardCharsets.UTF_8);
            XMLStreamReader xtr = xif.createXMLStreamReader(in);
            return new BpmnXMLConverter().convertToBpmnModel(xtr);
        }
    }

    @Test
    public void testInvalidMessageFlow() throws Exception {
        BpmnModel bpmnModel = readModel(
            "org/activiti/engine/test/validation/invalidMessageProcess.bpmn20.xml");
        assertThat(bpmnModel).isNotNull();

        assertThat(bpmnModel).isNotNull();
        List<ValidationError> allErrors = processValidator.validate(bpmnModel);
        assertThat(allErrors).hasSize(1);
        assertThat(allErrors.get(0).isWarning()).isTrue();
        assertThat(allErrors.get(0).getProblem()).isEqualTo("activiti-di-invalid-reference");
    }

  @Test
  public void should_raiseAValidationError_when_noProcessIsExecutable() {
    BpmnModel bpmnModel = new BpmnModel();
    for (int i = 0; i < 5; i++) {
      bpmnModel.addProcess(createNonExecutableProcess());
    }

    List<ValidationError> errors = processValidator.validate(bpmnModel);
    assertThat(errors).hasSize(1);
  }

    @Test
    public void should_raiseAnError_when_twoProcessesHasSameIdInTheBPMNModel() {
        BpmnModel bpmnModel = new BpmnModel();

        String sameIdTest = UUID.randomUUID().toString();
        bpmnModel.addProcess(TestProcessUtil.createOneTaskProcessWithId(sameIdTest));
        bpmnModel.addProcess(TestProcessUtil.createOneTaskProcessWithId(sameIdTest));

        List<ValidationError> errors = processValidator.validate(bpmnModel);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getProblem()).isEqualTo(Problems.PROCESS_DEFINITION_ID_NOT_UNIQUE);
  }

    @Test
  public void should_addWarningsForAllNonExecutableProcesses_WhenAtLeastOneProcessIsExecutable() {
    BpmnModel bpmnModel = new BpmnModel();

    for (int i = 0; i < 3; i++) {
        bpmnModel.addProcess(createNonExecutableProcess());
    }

    org.activiti.bpmn.model.Process executableProcess = TestProcessUtil.createOneTaskProcess();
    bpmnModel.addProcess(executableProcess);

    List<ValidationError> errors = processValidator.validate(bpmnModel);
    assertThat(errors).hasSize(3);
    for (ValidationError error : errors) {
      assertThat(error.isWarning()).isTrue();
      assertThat(error.getValidatorSetName()).isNotNull();
      assertThat(error.getProblem()).isNotNull();
      assertThat(error.getDefaultDescription()).isNotNull();
    }
  }

    private org.activiti.bpmn.model.Process createNonExecutableProcess() {
        org.activiti.bpmn.model.Process process = TestProcessUtil.createOneTaskProcess();
        process.setExecutable(false);
        return process;
    }

    private void assertCommonProblemFieldForActivity(ValidationError error) {
    assertProcessElementError(error);

    assertThat(error.getActivityId()).isNotNull();
    assertThat(error.getActivityName()).isNotNull();

    assertThat(error.getActivityId().length() > 0).isTrue();
    assertThat(error.getActivityName().length() > 0).isTrue();
  }

  private void assertCommonErrorFields(ValidationError error) {
    assertThat(error.getValidatorSetName()).isNotNull();
    assertThat(error.getProblem()).isNotNull();
    assertThat(error.getDefaultDescription()).isNotNull();
    assertThat(error.getXmlLineNumber() > 0).isTrue();
    assertThat(error.getXmlColumnNumber() > 0).isTrue();
  }

  private void assertProcessElementError(ValidationError error) {
    assertCommonErrorFields(error);
    assertThat(error.getProcessDefinitionId()).isEqualTo("invalidProcess");
    assertThat(error.getProcessDefinitionName()).isEqualTo("The invalid process");
  }

  private List<ValidationError> findErrors(List<ValidationError> errors, String validatorSetName,
      String problemName, int expectedNrOfProblems) {
    List<ValidationError> results = findErrors(errors, validatorSetName, problemName);
    assertThat(results).hasSize(expectedNrOfProblems);
    for (ValidationError result : results) {
      assertThat(result.getValidatorSetName()).isEqualTo(validatorSetName);
      assertThat(result.getProblem()).isEqualTo(problemName);
    }
    return results;
  }

  private List<ValidationError> findErrors(List<ValidationError> errors, String validatorSetName,
      String problemName) {
    List<ValidationError> results = new ArrayList<>();
    for (ValidationError error : errors) {
      if (error.getValidatorSetName().equals(validatorSetName) && error.getProblem().equals(problemName)) {
        results.add(error);
      }
    }
    return results;
  }

}
