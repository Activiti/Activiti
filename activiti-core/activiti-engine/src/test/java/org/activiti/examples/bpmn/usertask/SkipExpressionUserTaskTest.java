package org.activiti.examples.bpmn.usertask;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

public class SkipExpressionUserTaskTest extends PluggableActivitiTestCase {

    @Deployment
    public void test() {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("skipExpressionUserTask");
        List<Task> tasks = taskService.createTaskQuery().list();
        assertThat(tasks).hasSize(1);
        taskService.complete(tasks.get(0).getId());
        assertThat(taskService.createTaskQuery().list()).hasSize(0);

        Map<String, Object> variables2 = new HashMap<String, Object>();
        variables2.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
        variables2.put("skip", false);
        runtimeService.startProcessInstanceByKey("skipExpressionUserTask", variables2);
        List<Task> tasks2 = taskService.createTaskQuery().list();
        assertThat(tasks2).hasSize(1);
        taskService.complete(tasks2.get(0).getId());
        assertThat(taskService.createTaskQuery().list()).hasSize(0);

        Map<String, Object> variables3 = new HashMap<String, Object>();
        variables3.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
        variables3.put("skip", true);
        runtimeService.startProcessInstanceByKey("skipExpressionUserTask", variables3);
        List<Task> tasks3 = taskService.createTaskQuery().list();
        assertThat(tasks3).hasSize(0);
    }

    @Deployment
    public void testWithCandidateGroups() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
        vars.put("skip", true);
        runtimeService.startProcessInstanceByKey("skipExpressionUserTask", vars);
        assertThat(taskService.createTaskQuery().list()).hasSize(0);
    }

    @Deployment
    public void testSkipMultipleTasks() {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
        variables.put("skip1", true);
        variables.put("skip2", true);
        variables.put("skip3", false);

        runtimeService.startProcessInstanceByKey("skipExpressionUserTask-testSkipMultipleTasks",
                                                 variables);
        List<Task> tasks = taskService.createTaskQuery().list();
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getName()).isEqualTo("Task3");
    }
}
