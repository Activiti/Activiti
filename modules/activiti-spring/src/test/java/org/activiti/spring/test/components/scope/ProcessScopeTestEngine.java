package org.activiti.spring.test.components.scope;


import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

class ProcessScopeTestEngine {
    private int customerId = 43;

    private String keyForObjectType(Map<String, Object> runtimeVars, Class<?> clazz) {
        for (Map.Entry<String, Object> e : runtimeVars.entrySet()) {
            Object value = e.getValue();
            if (value.getClass().isAssignableFrom(clazz)) {
                return e.getKey();
            }
        }
        return null;
    }

    private StatefulObject run() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("customerId", customerId);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("component-waiter", vars);

        Map<String, Object> runtimeVars = runtimeService.getVariables(processInstance.getId());

        String statefulObjectVariableKey = keyForObjectType(runtimeVars, StatefulObject.class);

        assertTrue(!runtimeVars.isEmpty());
        assertTrue(StringUtils.hasText(statefulObjectVariableKey));

        StatefulObject scopedObject = (StatefulObject) runtimeService.getVariable(processInstance.getId(), statefulObjectVariableKey);
        assertNotNull(scopedObject);
        assertTrue(StringUtils.hasText(scopedObject.getName()));
        assertEquals(2, scopedObject.getVisitedCount());

        // the process has paused
        String procId = processInstance.getProcessInstanceId();

        List<Task> tasks = taskService.createTaskQuery().executionId(procId).list();
        assertEquals(1, tasks.size());

        Task t = tasks.iterator().next();
        this.taskService.claim(t.getId(), "me");
        this.taskService.complete(t.getId());

        scopedObject = (StatefulObject) runtimeService.getVariable(processInstance.getId(), statefulObjectVariableKey);
        assertEquals(3, scopedObject.getVisitedCount());

        assertEquals(customerId, scopedObject.getCustomerId());
        return scopedObject;
    }


    private ProcessEngine processEngine;
    private RuntimeService runtimeService;
    private TaskService taskService;

    public void testScopedProxyCreation() {

        StatefulObject one = run();
        StatefulObject two = run();
        assertNotSame(one.getName(), two.getName());
        assertEquals(one.getVisitedCount(), two.getVisitedCount());
    }

    public ProcessScopeTestEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
        this.runtimeService = this.processEngine.getRuntimeService();
        this.taskService = this.processEngine.getTaskService();
    }
}
