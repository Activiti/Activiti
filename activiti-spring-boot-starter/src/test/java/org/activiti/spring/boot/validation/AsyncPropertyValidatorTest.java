package org.activiti.spring.boot.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
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
                .getSystemResourceAsStream("processes-validation/async-process.bpmn")), true,
                false);
        List<ValidationError> validationErrors = repositoryService.validateProcess(bpmnModel);
    }
}
