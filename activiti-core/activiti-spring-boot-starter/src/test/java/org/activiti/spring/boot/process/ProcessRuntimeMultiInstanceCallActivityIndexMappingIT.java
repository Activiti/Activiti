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
package org.activiti.spring.boot.process;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.task.model.Task;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.tasks.TaskBaseRuntime;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeMultiInstanceCallActivityIndexMappingIT {

    private static final String CALLER_WITH_CATEGORY_BASED_OUTPUTS = "miIndexMappingCaller";

    private static final String CALLER_WITH_EXPLICIT_OUTPUTS = "miIndexMappingCallerExplicitOutputs";

    private static final String INDEX_VARIABLE = "sameNameAsTargetChildProcessVariable";

    private static final List<String> COLORS = List.of("red", "green", "blue");

    @Autowired
    private ProcessBaseRuntime processBaseRuntime;

    @Autowired
    private TaskBaseRuntime taskBaseRuntime;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private SecurityUtil securityUtil;

    @BeforeEach
    void setUp() {
        securityUtil.logInAs("user");
    }

    @AfterEach
    void cleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    void should_visitEachCollectionElementOnce_when_indexVariableIsMappedAsInputAndOutputsComeFromVariableCategories() {
        assertEachCollectionElementIsVisitedOnce(CALLER_WITH_CATEGORY_BASED_OUTPUTS);
    }

    @Test
    void should_visitEachCollectionElementOnce_when_indexVariableIsMappedAsInputAndOutputsAreDeclaredExplicitly() {
        assertEachCollectionElementIsVisitedOnce(CALLER_WITH_EXPLICIT_OUTPUTS);
    }

    private void assertEachCollectionElementIsVisitedOnce(String processDefinitionKey) {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(processDefinitionKey);

        assertThat(completeEachChildTask(processInstance)).containsExactlyElementsOf(expectedIterations());

        assertThat(processBaseRuntime.getProcessInstances())
            .as("the multi instance call activity should have completed the parent process")
            .extracting(ProcessInstance::getId)
            .doesNotContain(processInstance.getId());
    }

    private static List<Map.Entry<Object, Object>> expectedIterations() {
        return IntStream.range(0, COLORS.size())
            .mapToObj(index -> Map.<Object, Object>entry(index, COLORS.get(index)))
            .toList();
    }

    private List<Map.Entry<Object, Object>> completeEachChildTask(ProcessInstance processInstance) {
        List<Map.Entry<Object, Object>> iterationsSeenByChildren = new ArrayList<>();
        for (int completion = 0; completion < COLORS.size(); completion++) {
            findRunningChild(processInstance).ifPresent(child -> {
                    iterationsSeenByChildren.add(
                        entry(getVariableValue(child, INDEX_VARIABLE), getVariableValue(child, "color"))
                    );
                    completeSingleTaskOf(child);
                });
        }
        return iterationsSeenByChildren;
    }

    private Optional<ProcessInstance> findRunningChild(ProcessInstance processInstance) {
        List<ProcessInstance> children = processBaseRuntime
            .getChildrenProcessInstances(processInstance.getId())
            .getContent();
        assertThat(children).as("a sequential multi instance runs one child at a time").hasSizeLessThanOrEqualTo(1);
        return children.stream().findFirst();
    }

    private Object getVariableValue(ProcessInstance processInstance, String name) {
        return processBaseRuntime
            .getProcessVariablesByProcessId(processInstance.getId())
            .stream()
            .filter(variable -> name.equals(variable.getName()))
            .findFirst()
            .map(VariableInstance::getValue)
            .orElse(null);
    }

    private void completeSingleTaskOf(ProcessInstance processInstance) {
        List<Task> tasks = taskBaseRuntime.getTasksByProcessInstanceId(processInstance.getId());
        assertThat(tasks).hasSize(1);
        taskBaseRuntime.completeTask(tasks.get(0));
    }
}
