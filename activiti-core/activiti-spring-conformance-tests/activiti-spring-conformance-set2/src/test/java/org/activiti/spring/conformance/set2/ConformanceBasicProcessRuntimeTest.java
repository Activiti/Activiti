package org.activiti.spring.conformance.set2;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessDefinitionMeta;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.activiti.spring.conformance.util.security.SecurityUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ConformanceBasicProcessRuntimeTest {


    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Test
    public void shouldGetConfiguration() {
        securityUtil.logInAs("user1");
        //when
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        //then
        assertThat(configuration).isNotNull();
        //when
        List<ProcessRuntimeEventListener<?>> processRuntimeEventListeners = configuration.processEventListeners();
        List<VariableEventListener<?>> variableEventListeners = configuration.variableEventListeners();
        //then
        assertThat(processRuntimeEventListeners).hasSize(11);
        assertThat(variableEventListeners).hasSize(3);

    }

    @Test
    public void shouldProcessDefinitions() {
        securityUtil.logInAs("user1");

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0, 50));

        List<ProcessDefinition> processDefinitions = processDefinitionPage.getContent();
        assertThat(processDefinitions).extracting(ProcessDefinition::getName).containsOnly(
                "UserTask with Assignee",
                "UserTask with CandidateGroup",
                "UserTask with no User or Group Assignment",
                "UserTask with CandidateUser"
        );

    }

    @Test
    public void shouldProcessDefinitionsMetaData() {
        securityUtil.logInAs("user1");

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0, 50));

        List<ProcessDefinition> processDefinitions = processDefinitionPage.getContent();
        assertThat(processDefinitions).extracting(ProcessDefinition::getName).containsOnly(
                "UserTask with Assignee",
                "UserTask with CandidateGroup",
                "UserTask with no User or Group Assignment",
                "UserTask with CandidateUser"
        );


    }




}
