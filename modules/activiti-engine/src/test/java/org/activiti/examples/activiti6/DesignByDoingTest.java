package org.activiti.examples.activiti6;

import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.SetProcessDefinitionVersionCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * This example shows how to implement design by doing with adaptive process definitions
 */
public class DesignByDoingTest {

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    private int counter = 0;

    @Test
    public void testSequentialUserTasksProcessModel() throws Exception {
        designProcessByDoing_processModel();
        assertThatNextProcessInstanceIsTheSame();
    }

    private void assertThatNextProcessInstanceIsTheSame() {
        // execute next process instance according to already created process definition
        String doingByDesignProcessInstanceId = activitiRule.getRuntimeService()
                .startProcessInstanceByKey("designByDoing-process").getId();

        assertAndCompleteTask("Make a record", doingByDesignProcessInstanceId, "Fred");

        assertAndCompleteTask("Create inventory list", doingByDesignProcessInstanceId, "John");

        assertAndCompleteTask("Collect inventory", doingByDesignProcessInstanceId, "Bill");

        assertThat("All process instances must be finished", this.activitiRule.getRuntimeService().createProcessInstanceQuery().count(), is(0L));
    }

    private void designProcessByDoing_processModel() throws IOException {
        // Fred initializes design by doing process - it means he creates the first process model with one task
        // with name 'Make a record' assigned to him
        ProcessInstance processInstance = createAdaptiveProcessInstance("Fred", "Make a record");

        assertThat("User task with the name 'Make a record' assigned to 'Fred' exists",
                this.activitiRule.getTaskService().createTaskQuery().taskAssignee("Fred").taskName("Make a record").count(), is(1L));
        exportProcessDefinition(processInstance.getProcessDefinitionId(), "target/step-1-", "designByDoing-model.bpmn");

        // Fred has to specify who is the next user to continue in the process execution and what he should do
        processInstance = addUserTask(processInstance, "Create inventory list", "John", "target/step-2-");
        // now Fred can complete his task and process instance can continue to the next step
        assertAndCompleteTask("Make a record", processInstance.getId(), "Fred");

        // assert that the next task exists
        assertThat("User task with the name 'Create inventory list' assigned to 'John' exists",
                this.activitiRule.getTaskService().createTaskQuery().taskAssignee("John").taskName("Create inventory list").count(), is(1L));

        // Process instance is still not finished John has to specify who has to take care of the process execution next
        processInstance = addUserTask(processInstance, "Collect inventory", "Bill", "target/step-3-");
        // now John can complete his task and process instance can continue to the next step
        assertAndCompleteTask("Create inventory list", processInstance.getId(), "John");

        // Bill has collected all inventory. Bill can complete the task and end the process instance. Process model was designed by doing.
        assertAndCompleteTask("Collect inventory", processInstance.getId(), "Bill");
    }

    private ProcessInstance addUserTask(ProcessInstance processInstance, String userTaskName, String assigneeId, String namePrefix) throws IOException {
        addUserTaskToProcessInstance(processInstance, userTaskName, assigneeId);
        processInstance = this.activitiRule.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        exportProcessDefinition(processInstance.getProcessDefinitionId(), namePrefix, "designByDoing-model.bpmn");
        return processInstance;
    }

    private void assertAndCompleteTask(String taskName, String processInstanceId, String assigneeId) {
        Task task = this.activitiRule.getTaskService().createTaskQuery().
                processInstanceId(processInstanceId).
                taskName(taskName).
                taskAssignee(assigneeId).singleResult();
        assertThat(task, is(notNullValue()));
        this.activitiRule.getTaskService().complete(task.getId());
    }

    private void addUserTaskToProcessInstance(ProcessInstance processInstance, String userTaskName, String assignee) {
        ProcessEngineConfigurationImpl oldProcessEngineConfiguration = Context.getProcessEngineConfiguration();
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) this.activitiRule.getProcessEngine().getProcessEngineConfiguration());

        Process process = ProcessDefinitionUtil.getProcess(processInstance.getProcessDefinitionId());

        process.addFlowElement(createUserTask("userTask"+this.counter, userTaskName, assignee));
        process.addFlowElement(createSequenceFlow("toUserTask" + this.counter, "userTask" + (this.counter-1), "userTask" + this.counter));
        process.removeFlowElement("toEnd");
        process.addFlowElement(createSequenceFlow("toEnd", "userTask" + this.counter, "end"));
        this.counter++;

        deployModelWithProcess(process);
        upgradeProcessInstanceDefinitionVersion(processInstance);
        Context.setProcessEngineConfiguration(oldProcessEngineConfiguration);
    }

    private void upgradeProcessInstanceDefinitionVersion(ProcessInstance processInstance) {
        ProcessDefinition processDefinition = this.activitiRule.getRepositoryService().getProcessDefinition(processInstance.getProcessDefinitionId());
        this.activitiRule.getManagementService().executeCommand(new SetProcessDefinitionVersionCmd(processInstance.getId(), processDefinition.getVersion() + 1));
    }

    private Deployment deployModelWithProcess(Process process) {
        BpmnModel model = new BpmnModel();
        model.addProcess(process);
        return activitiRule.getRepositoryService().createDeployment()
                .addBpmnModel("designByDoing-model.bpmn", model).name("DesignByDoing process deployment")
                .deploy();
    }

    private ProcessInstance createAdaptiveProcessInstance(String currentUserId, String taskName) {
        // 1. Build up the basic model
        Process process = new Process();
        process.setId("designByDoing-process");

        process.addFlowElement(createStartEvent());
        process.addFlowElement(createSequenceFlow("toUserTask" + this.counter, "start", "userTask" + this.counter));
        process.addFlowElement(createUserTask("userTask" + this.counter, taskName, currentUserId));
        process.addFlowElement(createSequenceFlow("toEnd", "userTask"+this.counter, "end"));
        process.addFlowElement(createEndEvent());
        this.counter++;

        // 2. deploy basic process model
        deployModelWithProcess(process);

        // start process instance according to the process definition
        return activitiRule.getRuntimeService()
                .startProcessInstanceByKey("designByDoing-process");
    }

    private void exportProcessDefinition(String processDefinitionId, String namePrefix, String resourceName) throws IOException {
        ProcessDefinition processDefinition = this.activitiRule.getRepositoryService().getProcessDefinition(processDefinitionId);
        InputStream processBpmn = activitiRule.getRepositoryService()
                .getResourceAsStream(processDefinition.getDeploymentId(), resourceName);
        FileUtils.copyInputStreamToFile(processBpmn,
                new File(namePrefix + "process.bpmn20.xml"));
    }

    private UserTask createUserTask(String id, String name, String assignee) {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee(assignee);
        return userTask;
    }

    private SequenceFlow createSequenceFlow(String id, String from, String to) {
        SequenceFlow flow = new SequenceFlow();
        flow.setId(id);
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        return flow;
    }

    private StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        return startEvent;
    }

    private EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("end");
        return endEvent;
    }

}
