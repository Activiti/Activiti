package org.activiti.spring.boot.process.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.validation.ValidationError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class AsyncPropertyValidatorTest {

    @Autowired
    private RepositoryService repositoryService;

    @Test
    public void shouldGetConfiguration() {
        BpmnXMLConverter converter = new BpmnXMLConverter();
        BpmnModel bpmnModel = converter.convertToBpmnModel(new InputStreamSource(ClassLoader
                .getSystemResourceAsStream("processes-validation/async-property-process.bpmn")), true,
                false);
        List<ValidationError> validationErrors = repositoryService.validateProcess(bpmnModel);
        assertThat(validationErrors.get(0))
                 .extracting(ValidationError::getValidatorSetName,
                 ValidationError::getProblem,
                 ValidationError::getProcessDefinitionId,
                 ValidationError::getActivityId)
                 .contains("activiti-spring-boot-starter", 
                 "activiti-flow-element-async-not-available", "async-property-root-process", "usertask1");
        assertThat(validationErrors.get(1))
            .extracting(ValidationError::getValidatorSetName,
            ValidationError::getProblem,
            ValidationError::getProcessDefinitionId,
            ValidationError::getActivityId)
            .contains("activiti-spring-boot-starter", 
            "activiti-flow-element-async-not-available", "async-property-root-process", "usertask2");
        assertThat(validationErrors.get(2))
            .extracting(ValidationError::getValidatorSetName,
            ValidationError::getProblem,
            ValidationError::getProcessDefinitionId,
            ValidationError::getActivityId)
            .contains("activiti-spring-boot-starter", 
            "activiti-flow-element-async-not-available", "async-property-root-process", "usertask3");
        assertThat(validationErrors.get(3))
            .extracting(ValidationError::getValidatorSetName,
            ValidationError::getProblem,
            ValidationError::getProcessDefinitionId,
            ValidationError::getActivityId)
            .contains("activiti-spring-boot-starter", 
            "activiti-signal-async-not-available", "async-property-root-process", "signalintermediatethrowevent1");
        assertThat(validationErrors.get(4))
            .extracting(ValidationError::getValidatorSetName,
            ValidationError::getProblem,
            ValidationError::getProcessDefinitionId,
            ValidationError::getActivityId)
            .contains("activiti-spring-boot-starter", 
            "activiti-event-timer-async-not-available", "async-property-root-process", "boundarytimer1");
        assertThat(validationErrors.get(5))
            .extracting(ValidationError::getValidatorSetName,
            ValidationError::getProblem,
            ValidationError::getProcessDefinitionId,
            ValidationError::getActivityId)
            .contains("activiti-spring-boot-starter", 
            "activiti-event-timer-async-not-available", "async-property-root-process", "timerintermediatecatchevent1");
        assertThat(validationErrors.get(6))
            .extracting(ValidationError::getValidatorSetName,
            ValidationError::getProblem,
            ValidationError::getProcessDefinitionId,
            ValidationError::getActivityId)
            .contains("activiti-spring-boot-starter", 
            "activiti-event-timer-async-not-available", "async-property-pool-process", "timerstartevent1");
    }
}
