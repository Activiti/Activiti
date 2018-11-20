package org.activiti.spring.boot.process;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.spring.boot.RuntimeTestConfiguration;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeExpressionsTest {

    private static final String EVAL_EXPRESSIONS = "evalexpres-4b5e9a94-6111-4d34-9252-522bf3f49e6e";


    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityUtil securityUtil;


    @Test
    public void createProcessInstanceAndValidateExpressionsMatch() {

        securityUtil.logInAs("salaboy");

        LinkedHashMap content = new LinkedHashMap();
        content.put("bool",true);
        //note a list is different and requires different expressions
        content.put("array", Collections.singletonList(1).toArray());

                //when
        ProcessInstance evalExprProc = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(EVAL_EXPRESSIONS)
                //note the object structure for expressions is different if we first convert to JsonNode
                //and different again if we use a custom pojo that is serialized as json (then we can use the bean's structure)
                .withVariable("content", content)
                .build());

        assertThat(RuntimeTestConfiguration.completedProcesses).contains(evalExprProc.getId());
        //then
        assertThat(evalExprProc).isNotNull();

        assertThat(evalExprProc.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.COMPLETED);
        assertThat(RuntimeTestConfiguration.recordEvalExprSuccessConnectorExecuted).isEqualTo(true);

    }



}
