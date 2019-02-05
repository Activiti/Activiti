package org.activiti.spring.conformance.variables;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.model.shared.event.VariableEvent;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.GetVariablesPayloadBuilder;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.builders.SetVariablesPayloadBuilder;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.GetTaskVariablesPayloadBuilder;
import org.activiti.api.task.model.builders.SetTaskVariablesPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.conformance.util.security.SecurityUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.activiti.spring.conformance.variables.VariablesRuntimeTestConfiguration.collectedEvents;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessVariablesTest {

    private final String processKey = "usertaskas-b5300a4b-8950-4486-ba20-a8d775a3d75d";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Before
    public void cleanUp() {
        collectedEvents.clear();
    }

    @Test
    public void shouldGetSameNamesAndValues() {

        securityUtil.logInAs("user1");

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processKey)
                .withBusinessKey("my-business-key")
                .withName("my-process-instance-name")
                .build());

        collectedEvents.clear();

        Map<String, Object> variablesMap = new HashMap<>();
        variablesMap.put("one", "variableOne");
        variablesMap.put("two", 2);

        processRuntime.setVariables(new SetVariablesPayloadBuilder().withVariables(variablesMap).withProcessInstanceId(processInstance.getId()).build());

        List<VariableInstance> variableInstanceList = processRuntime.variables(new GetVariablesPayloadBuilder().withProcessInstanceId(processInstance.getId()).build());
        VariableInstance variableOne = variableInstanceList.get(0);
        String valueOne = variableOne.getValue();
        assertThat(valueOne).isEqualTo("variableOne");
        String nameOne = variableOne.getName();
        assertThat(nameOne).isEqualTo("one");

        assertThat(collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(
                        VariableEvent.VariableEvents.VARIABLE_CREATED,
                        VariableEvent.VariableEvents.VARIABLE_CREATED
                );
    }

    @Test
    public void shouldGetProcessIdAndProcessInstanceId() {

        securityUtil.logInAs("user1");

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processKey)
                .withBusinessKey("my-business-key")
                .withName("my-process-instance-name")
                .build());

        collectedEvents.clear();

        Map<String, Object> variablesMap = new HashMap<>();
        variablesMap.put("one", "variableOne");
        variablesMap.put("two", 2);

        processRuntime.setVariables(new SetVariablesPayloadBuilder().withVariables(variablesMap).withProcessInstanceId(processInstance.getId()).build());

        List<VariableInstance> variableInstanceList = processRuntime.variables(new GetVariablesPayloadBuilder().withProcessInstanceId(processInstance.getId()).build());
        VariableInstance variableOne = variableInstanceList.get(0);

        assertThat(variableOne.getTaskId()).isNull();
        assertThat(variableOne.getProcessInstanceId()).isEqualTo(processInstance.getId());

        assertThat(collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(
                        VariableEvent.VariableEvents.VARIABLE_CREATED,
                        VariableEvent.VariableEvents.VARIABLE_CREATED
                );
    }

    @Test
    public void shouldNotBeTaskVariable() {
        securityUtil.logInAs("user1");

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processKey)
                .withBusinessKey("my-business-key")
                .withName("my-process-instance-name")
                .build());

        collectedEvents.clear();

        Map<String, Object> variablesMap = new HashMap<>();
        variablesMap.put("one", "variableOne");
        variablesMap.put("two", 2);

        processRuntime.setVariables(new SetVariablesPayloadBuilder().withVariables(variablesMap).withProcessInstanceId(processInstance.getId()).build());

        List<VariableInstance> variableInstanceList = processRuntime.variables(new GetVariablesPayloadBuilder().withProcessInstanceId(processInstance.getId()).build());
        VariableInstance variableOne = variableInstanceList.get(0);

        assertThat(variableOne.isTaskVariable()).isFalse();

        assertThat(collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(
                        VariableEvent.VariableEvents.VARIABLE_CREATED,
                        VariableEvent.VariableEvents.VARIABLE_CREATED
                );
    }

    @Test
    public void shouldGetRightVariableType(){
        securityUtil.logInAs("user1");

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processKey)
                .withBusinessKey("my-business-key")
                .withName("my-process-instance-name")
                .build());

        collectedEvents.clear();

        Map<String, Object> variablesMap = new HashMap<>();
        variablesMap.put("one", "variableOne");
        variablesMap.put("two", 2);
        processRuntime.setVariables(new SetVariablesPayloadBuilder().withVariables(variablesMap).withProcessInstanceId(processInstance.getId()).build());

        List<VariableInstance> variableInstanceList = processRuntime.variables(new GetVariablesPayloadBuilder().withProcessInstanceId(processInstance.getId()).build());
        VariableInstance variableOne = variableInstanceList.get(0);
        VariableInstance variableTwo = variableInstanceList.get(1);
        assertThat(variableOne.getType()).isEqualTo("string");
        assertThat(variableTwo.getType()).isEqualTo("integer");

        assertThat(collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(
                        VariableEvent.VariableEvents.VARIABLE_CREATED,
                        VariableEvent.VariableEvents.VARIABLE_CREATED
                );
    }

    @After
    public void cleanup() {
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processInstancePage = processAdminRuntime.processInstances(Pageable.of(0, 50));
        for (ProcessInstance pi : processInstancePage.getContent()) {
            processAdminRuntime.delete(ProcessPayloadBuilder.delete(pi.getId()));
        }
    }

}
