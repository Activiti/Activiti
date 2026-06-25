/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.validation.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.junit.jupiter.api.Test;

class ExclusiveGatewayValidatorTest {

    private ExclusiveGatewayValidator validatorWith(Map<String, Object> config) {
        return new ExclusiveGatewayValidator(config);
    }

    private List<ValidationError> validate(ExclusiveGatewayValidator validator, ExclusiveGateway gateway) {
        var process = new Process();
        process.addFlowElement(gateway);
        var bpmnModel = new BpmnModel();
        bpmnModel.addProcess(process);
        var errors = new ArrayList<ValidationError>();
        validator.validate(bpmnModel, errors);
        return errors;
    }

    private SequenceFlow sequenceFlowWithCondition(String id, String condition) {
        var flow = new SequenceFlow();
        flow.setId(id);
        flow.setConditionExpression(condition);
        return flow;
    }

    private SequenceFlow sequenceFlowWithoutCondition(String id) {
        var flow = new SequenceFlow();
        flow.setId(id);
        return flow;
    }

    @Test
    void should_addError_when_gatewayHasNoOutgoingFlows() {
        var validator = validatorWith(Map.of());
        var gateway = new ExclusiveGateway();

        var errors = validate(validator, gateway);

        assertThat(errors)
            .hasSize(1)
            .first()
            .satisfies(e -> {
                assertThat(e.getProblem()).isEqualTo(Problems.EXCLUSIVE_GATEWAY_NO_OUTGOING_SEQ_FLOW);
                assertThat(e.isWarning()).isFalse();
            });
    }

    @Test
    void should_addError_when_singleOutgoingFlowHasCondition() {
        var validator = validatorWith(Map.of());
        var gateway = new ExclusiveGateway();
        gateway.setOutgoingFlows(List.of(sequenceFlowWithCondition("flow1", "${condition}")));

        var errors = validate(validator, gateway);

        assertThat(errors)
            .hasSize(1)
            .first()
            .satisfies(e -> {
                assertThat(e.getProblem()).isEqualTo(
                    Problems.EXCLUSIVE_GATEWAY_CONDITION_NOT_ALLOWED_ON_SINGLE_SEQ_FLOW
                );
                assertThat(e.isWarning()).isFalse();
            });
    }

    @Test
    void should_notAddError_when_singleOutgoingFlowHasNoCondition() {
        var validator = validatorWith(Map.of());
        var gateway = new ExclusiveGateway();
        gateway.setOutgoingFlows(List.of(sequenceFlowWithoutCondition("flow1")));

        var errors = validate(validator, gateway);

        assertThat(errors).isEmpty();
    }

    @Test
    void should_addError_when_defaultFlowHasCondition() {
        var validator = validatorWith(Map.of());
        var gateway = new ExclusiveGateway();
        gateway.setDefaultFlow("default");
        gateway.setOutgoingFlows(
            List.of(
                sequenceFlowWithCondition("default", "${condition}"),
                sequenceFlowWithCondition("flow2", "${other}")
            )
        );

        var errors = validate(validator, gateway);

        assertThat(errors)
            .extracting(ValidationError::getProblem)
            .contains(Problems.EXCLUSIVE_GATEWAY_CONDITION_ON_DEFAULT_SEQ_FLOW);
    }

    @Test
    void should_addWarning_when_flowWithoutConditionExistsAndErrorOnMissingConditionIsNotSet() {
        var validator = validatorWith(Map.of());
        var gateway = new ExclusiveGateway();
        gateway.setOutgoingFlows(
            List.of(sequenceFlowWithCondition("flow1", "${condition}"), sequenceFlowWithoutCondition("flow2"))
        );

        var errors = validate(validator, gateway);

        assertThat(errors)
            .hasSize(1)
            .first()
            .satisfies(e -> {
                assertThat(e.getProblem()).isEqualTo(Problems.EXCLUSIVE_GATEWAY_SEQ_FLOW_WITHOUT_CONDITIONS);
                assertThat(e.isWarning()).isTrue();
            });
    }

    @Test
    void should_addWarning_when_flowWithoutConditionExistsAndErrorOnMissingConditionIsFalse() {
        var validator = validatorWith(
            Map.of(ExclusiveGatewayValidator.ERROR_ON_MISSING_CONDITION_VALIDATION_CONFIG, false)
        );
        var gateway = new ExclusiveGateway();
        gateway.setOutgoingFlows(
            List.of(sequenceFlowWithCondition("flow1", "${condition}"), sequenceFlowWithoutCondition("flow2"))
        );

        var errors = validate(validator, gateway);

        assertThat(errors)
            .hasSize(1)
            .first()
            .satisfies(e -> {
                assertThat(e.getProblem()).isEqualTo(Problems.EXCLUSIVE_GATEWAY_SEQ_FLOW_WITHOUT_CONDITIONS);
                assertThat(e.isWarning()).isTrue();
            });
    }

    @Test
    void should_addError_when_flowWithoutConditionExistsAndErrorOnMissingConditionIsTrue() {
        var validator = validatorWith(
            Map.of(ExclusiveGatewayValidator.ERROR_ON_MISSING_CONDITION_VALIDATION_CONFIG, true)
        );
        var gateway = new ExclusiveGateway();
        gateway.setOutgoingFlows(
            List.of(sequenceFlowWithCondition("flow1", "${condition}"), sequenceFlowWithoutCondition("flow2"))
        );

        var errors = validate(validator, gateway);

        assertThat(errors)
            .hasSize(1)
            .first()
            .satisfies(e -> {
                assertThat(e.getProblem()).isEqualTo(Problems.EXCLUSIVE_GATEWAY_SEQ_FLOW_WITHOUT_CONDITIONS);
                assertThat(e.isWarning()).isFalse();
            });
    }

    @Test
    void should_notAddError_when_allFlowsHaveConditionsOrIsDefault() {
        var validator = validatorWith(
            Map.of(ExclusiveGatewayValidator.ERROR_ON_MISSING_CONDITION_VALIDATION_CONFIG, true)
        );
        var gateway = new ExclusiveGateway();
        gateway.setDefaultFlow("default");
        gateway.setOutgoingFlows(
            List.of(sequenceFlowWithCondition("flow1", "${condition}"), sequenceFlowWithoutCondition("default"))
        );

        var errors = validate(validator, gateway);

        assertThat(errors).isEmpty();
    }

    @Test
    void should_addWarning_when_constructedWithNoArgConstructor() {
        var validator = new ExclusiveGatewayValidator();
        var gateway = new ExclusiveGateway();
        gateway.setOutgoingFlows(
            List.of(sequenceFlowWithCondition("flow1", "${condition}"), sequenceFlowWithoutCondition("flow2"))
        );

        var errors = validate(validator, gateway);

        assertThat(errors)
            .hasSize(1)
            .first()
            .satisfies(e -> {
                assertThat(e.getProblem()).isEqualTo(Problems.EXCLUSIVE_GATEWAY_SEQ_FLOW_WITHOUT_CONDITIONS);
                assertThat(e.isWarning()).isTrue();
            });
    }

    @Test
    void should_addError_when_errorOnMissingConditionConfiguredAsStringTrue() {
        var validator = validatorWith(
            Map.of(ExclusiveGatewayValidator.ERROR_ON_MISSING_CONDITION_VALIDATION_CONFIG, "true")
        );
        var gateway = new ExclusiveGateway();
        gateway.setOutgoingFlows(
            List.of(sequenceFlowWithCondition("flow1", "${condition}"), sequenceFlowWithoutCondition("flow2"))
        );

        var errors = validate(validator, gateway);

        assertThat(errors)
            .hasSize(1)
            .first()
            .satisfies(e -> {
                assertThat(e.getProblem()).isEqualTo(Problems.EXCLUSIVE_GATEWAY_SEQ_FLOW_WITHOUT_CONDITIONS);
                assertThat(e.isWarning()).isFalse();
            });
    }

    @Test
    void should_addWarning_when_errorOnMissingConditionConfiguredAsStringFalse() {
        var validator = validatorWith(
            Map.of(ExclusiveGatewayValidator.ERROR_ON_MISSING_CONDITION_VALIDATION_CONFIG, "false")
        );
        var gateway = new ExclusiveGateway();
        gateway.setOutgoingFlows(
            List.of(sequenceFlowWithCondition("flow1", "${condition}"), sequenceFlowWithoutCondition("flow2"))
        );

        var errors = validate(validator, gateway);

        assertThat(errors)
            .hasSize(1)
            .first()
            .satisfies(e -> {
                assertThat(e.getProblem()).isEqualTo(Problems.EXCLUSIVE_GATEWAY_SEQ_FLOW_WITHOUT_CONDITIONS);
                assertThat(e.isWarning()).isTrue();
            });
    }

    @Test
    void should_addWarning_when_errorOnMissingConditionConfiguredWithUnrecognizedType() {
        var config = new java.util.HashMap<String, Object>();
        config.put(ExclusiveGatewayValidator.ERROR_ON_MISSING_CONDITION_VALIDATION_CONFIG, 42);
        var validator = new ExclusiveGatewayValidator(config);
        var gateway = new ExclusiveGateway();
        gateway.setOutgoingFlows(
            List.of(sequenceFlowWithCondition("flow1", "${condition}"), sequenceFlowWithoutCondition("flow2"))
        );

        var errors = validate(validator, gateway);

        assertThat(errors)
            .hasSize(1)
            .first()
            .satisfies(e -> {
                assertThat(e.getProblem()).isEqualTo(Problems.EXCLUSIVE_GATEWAY_SEQ_FLOW_WITHOUT_CONDITIONS);
                assertThat(e.isWarning()).isTrue();
            });
    }
}
