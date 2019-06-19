package org.activiti.engine.test.json;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.examples.variables.SomeSerializable;

public class SerializePOJOJsonTest extends ResourceActivitiTestCase {

    public SerializePOJOJsonTest() {
        super("org/activiti/standalone/cfg/variable/custom-serialize-variables-activiti.cfg.xml");
    }

    @Deployment
    public void testJsonVarInExpression() throws Exception {
        Map<String, Object> vars = new HashMap<String, Object>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("assignee", "salaboy");
        map.put("category", "test");
        Map<String, Object> mapInMap = new HashMap<String, Object>();
        mapInMap.put("user", "salaboy");
        map.put("mapInMap", mapInMap);
        vars.put("userMap", map);
        List<String> list = Arrays.asList("salaboy", "salaboy", "salaboy");
        vars.put("userCollection", list);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testJsonVarInExpression", vars);
        String taskId = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId();
        taskService.complete(taskId);
        taskId = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId();
        taskService.getIdentityLinksForTask(taskId).stream().forEach(new Consumer<IdentityLink>() {
            @Override
            public void accept(IdentityLink i) {
                if ("candidate".equals(i.getType()) ) {
                    assertEquals("salaboy", i.getUserId());
                }
            }
        });
        taskService.complete(taskId);
        HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        assertEquals("salaboy", task.getAssignee());
        assertEquals("test", task.getCategory());
    }

    @Deployment
    public void testCollectionJsonVarInExpression() throws Exception {
        Map<String, Object> vars = new HashMap<String, Object>();
        List<String> list = Arrays.asList("salaboy", "salaboy", "salaboy");
        vars.put("userCollection", list);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testCollectionJsonVarInExpression", vars);
        String taskId = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId();
        taskService.complete(taskId);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertEquals(3, tasks.size());
        tasks.forEach(task -> taskService.complete(task.getId()));

    }

    @Deployment
    public void testCollectionInJsonVarInExpression() throws Exception {
        Map<String, Object> vars = new HashMap<String, Object>();
        List<String> list = Arrays.asList("salaboy", "salaboy", "salaboy");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userCollection", list);
        vars.put("userMap", map);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testCollectionInJsonVarInExpression", vars);
        String taskId = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId();
        taskService.complete(taskId);
        taskService.createTaskQuery().processInstanceId(processInstance.getId()).list().forEach(task -> taskService.complete(task.getId()));

        vars = new HashMap<String, Object>();
        List<SomeSerializable> beanList = Arrays.asList(new SomeSerializable("salaboy"), new SomeSerializable("salaboy"), new SomeSerializable("salaboy"));
        map = new HashMap<String, Object>();
        map.put("userCollection", beanList);
        vars.put("userMap", map);
        processInstance = runtimeService.startProcessInstanceByKey("testCollectionInJsonVarInExpression", vars);
        taskId = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId();
        taskService.complete(taskId);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertEquals(3, tasks.size());
        tasks.forEach(task -> taskService.complete(task.getId()));
    }

    @Deployment
    public void testPOJOCollectionInJsonVarInExpression() throws Exception {
        Map<String, Object> vars = new HashMap<String, Object>();
        Map<String, Object> map = new HashMap<String, Object>();
        vars = new HashMap<String, Object>();
        List<SomeSerializable> beanList = Arrays.asList(new SomeSerializable("salaboy"), new SomeSerializable("salaboy"), new SomeSerializable("salaboy"));
        map = new HashMap<String, Object>();
        map.put("userCollection", beanList);
        vars.put("userMap", map);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testPOJOCollectionInJsonVarInExpression", vars);
        String taskId = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId();
        taskService.complete(taskId);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertEquals(3, tasks.size());
        tasks.forEach(task -> taskService.complete(task.getId()));
    }
}