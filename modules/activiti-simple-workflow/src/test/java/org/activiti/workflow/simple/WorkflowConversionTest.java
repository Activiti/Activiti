/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.workflow.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversionFactory;
import org.activiti.workflow.simple.converter.step.FeedbackStepDefinitionConverter;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 */
public class WorkflowConversionTest {
  
  private static Logger log = LoggerFactory.getLogger(WorkflowConversionTest.class);
  
  @Rule
  public ActivitiRule activitiRule = new ActivitiRule();
  
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  
  
  protected WorkflowDefinitionConversionFactory conversionFactory;
  
  @Before
  public void initialiseTest() {
    
    // Alternatively, the following setup could be done using a dependency injection container
    
    conversionFactory = new WorkflowDefinitionConversionFactory();
    runtimeService = activitiRule.getRuntimeService();
    taskService = activitiRule.getTaskService();
  }
  
  @After
  public void cleanup() {
    for (Deployment deployment : activitiRule.getRepositoryService().createDeploymentQuery().list()) {
      activitiRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }
  }
  
  @Test
  public void testSimplestProcess() {
    WorkflowDefinition workflowDefinition = new WorkflowDefinition()
    .name("testWorkflow")
    .description("This is a test workflow");
  
    // Validate
    ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey(convertAndDeploy(workflowDefinition));
    assertTrue(processInstance.isEnded());
  }
  
  @Test
  public void testUserTasksWithOnlyAssignees() {
    String[] assignees = new String[] {"kermit", "gonzo", "mispiggy"};
    
    WorkflowDefinition workflowDefinition = new WorkflowDefinition()
      .name("testWorkflow")
      .description("This is a test workflow")
      .addHumanStep("first task", assignees[0])
      .addHumanStep("second step", assignees[1])
      .addHumanStep("third step", assignees[2]);
    
    // Validate
    activitiRule.getRuntimeService().startProcessInstanceByKey(convertAndDeploy(workflowDefinition));
    for (String assignee : assignees) {
      Task task = activitiRule.getTaskService().createTaskQuery().singleResult();
      assertEquals(assignee, task.getAssignee());
      activitiRule.getTaskService().complete(task.getId());
    }
    
    assertEquals(0, activitiRule.getRuntimeService().createProcessInstanceQuery().count());
  }
  
  @Test
  public void testThreeUserTasksInParallel() throws Exception {
    TaskService taskService = activitiRule.getTaskService();
    
    WorkflowDefinition workflowDefinition = new WorkflowDefinition()
      .name("testWorkflow")
      .description("This is a test workflow")
      .inParallel()
        .inList()
          .addHumanStep("first task", "kermit")
        .endList()
        .inList()
          .addHumanStep("second step", "gonzo")
        .endList()
        .inList()
          .addHumanStep("third task", "mispiggy")
        .endList()
      .endParallel()
      .addHumanStep("Task in between", "kermit")
      .inParallel()
        .inList()
          .addHumanStep("fourth task", "gonzo")
        .endList()
        .inList()
          .addHumanStep("fifth step", "gonzo")
        .endList()
      .endParallel();
    
    // Validate
    activitiRule.getRuntimeService().startProcessInstanceByKey(convertAndDeploy(workflowDefinition));
    assertEquals(1, taskService.createTaskQuery().taskAssignee("kermit").count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("gonzo").count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("mispiggy").count());
    
    // Complete tasks
    for (Task task : taskService.createTaskQuery().list()) {
      activitiRule.getTaskService().complete(task.getId());
    }
    
    // In between task should be active
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Task in between", task.getName());
    taskService.complete(task.getId());
    
    // There should be two task open now for gonzo
    assertEquals(2, taskService.createTaskQuery().taskAssignee("gonzo").count());
  }
  
  @Test
  public void testUserTasksInChoice() throws Exception {
    TaskService taskService = activitiRule.getTaskService();
    
    WorkflowDefinition workflowDefinition = new WorkflowDefinition()
      .name("testWorkflow")
      .description("This is a test workflow")
      .inChoice()
        .inList()
          .addCondition("test", "==", "'hello'")
          .addHumanStep("first task", "kermit")
          .addHumanStep("second task", "kermit")
        .endList()
        .inList()
          .addHumanStep("gonzo task", "gonzo")
        .endList()
      .endChoice()
      .addHumanStep("last task", "kermit");
    
    // Validate
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("test", "hello");
    String definitionKey = convertAndDeploy(workflowDefinition);
    ProcessInstance instance = activitiRule.getRuntimeService().startProcessInstanceByKey(definitionKey, varMap);
    assertEquals(1, taskService.createTaskQuery().taskAssignee("kermit").count());
    assertEquals(0, taskService.createTaskQuery().taskAssignee("gonzo").count());
    
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("first task", task.getName());
    taskService.complete(task.getId());
    
    task = taskService.createTaskQuery().singleResult();
    assertEquals("second task", task.getName());
    taskService.complete(task.getId());
    
    task = taskService.createTaskQuery().singleResult();
    assertEquals("last task", task.getName());
    taskService.complete(task.getId());
    
    assertEquals(0, activitiRule.getRuntimeService().createProcessInstanceQuery().processInstanceId(instance.getId()).count());
    
    varMap = new HashMap<String, Object>();
    varMap.put("test", "world");
    instance = activitiRule.getRuntimeService().startProcessInstanceByKey(definitionKey, varMap);
    assertEquals(0, taskService.createTaskQuery().taskAssignee("kermit").count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("gonzo").count());
    
    task = taskService.createTaskQuery().singleResult();
    assertEquals("gonzo task", task.getName());
    taskService.complete(task.getId());
    
    task = taskService.createTaskQuery().singleResult();
    assertEquals("last task", task.getName());
    taskService.complete(task.getId());
    
    assertEquals(0, activitiRule.getRuntimeService().createProcessInstanceQuery().processInstanceId(instance.getId()).count());
  }
  
  @Test
  public void testMultipleConditionsInChoice() throws Exception {
    TaskService taskService = activitiRule.getTaskService();
    
    WorkflowDefinition workflowDefinition = new WorkflowDefinition()
      .name("testWorkflow")
      .description("This is a test workflow")
      .inChoice()
        .inList()
          .addCondition("test", "==", "'hello'")
          .addCondition("test2", "==", "'world'")
          .addHumanStep("first task", "kermit")
        .endList()
        .inList()
          .addHumanStep("gonzo task", "gonzo")
        .endList()
      .endChoice();
    
    // Validate
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("test", "hello");
    varMap.put("test2", "world");
    String definitionKey = convertAndDeploy(workflowDefinition);
    ProcessInstance instance = activitiRule.getRuntimeService().startProcessInstanceByKey(definitionKey, varMap);
    assertEquals(1, taskService.createTaskQuery().taskAssignee("kermit").count());
    assertEquals(0, taskService.createTaskQuery().taskAssignee("gonzo").count());
    
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("first task", task.getName());
    taskService.complete(task.getId());
    
    assertEquals(0, activitiRule.getRuntimeService().createProcessInstanceQuery().processInstanceId(instance.getId()).count());
    
    varMap = new HashMap<String, Object>();
    varMap.put("test", "world");
    varMap.put("test2", "world");
    instance = activitiRule.getRuntimeService().startProcessInstanceByKey(definitionKey, varMap);
    assertEquals(0, taskService.createTaskQuery().taskAssignee("kermit").count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("gonzo").count());
    
    task = taskService.createTaskQuery().singleResult();
    assertEquals("gonzo task", task.getName());
    taskService.complete(task.getId());
    
    assertEquals(0, activitiRule.getRuntimeService().createProcessInstanceQuery().processInstanceId(instance.getId()).count());
  }
  
  @Test
  public void testInitiatorOnHumanStep() {
    WorkflowDefinition workflowDefinition = new WorkflowDefinition()
      .name("testWorkflow")
      .description("This is a test workflow")
      .addHumanStep("step1", "kermit")
      .addHumanStepForWorkflowInitiator("step2");
    
    activitiRule.getIdentityService().setAuthenticatedUserId("MrPink");
    activitiRule.getRuntimeService().startProcessInstanceByKey(convertAndDeploy(workflowDefinition));
    activitiRule.getIdentityService().setAuthenticatedUserId("null");
    
    // Complete first task
    TaskService taskService = activitiRule.getTaskService();
    assertEquals(1, taskService.createTaskQuery().taskAssignee("kermit").count());
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    
    // Second task should be done by initiator of workflow
    assertEquals(1, taskService.createTaskQuery().taskAssignee("MrPink").count());
    assertEquals(0, taskService.createTaskQuery().taskAssignee("kermit").count());
  }
  
  @Test
  public void testGroupsForHumanStep() {
    WorkflowDefinition workflowDefinition = new WorkflowDefinition()
      .name("testWorkflow")
      .description("This is a test workflow")
      .addHumanStepForGroup("step1", "management", "sales")
      .addHumanStepForGroup("step1", "sales");
    activitiRule.getRuntimeService().startProcessInstanceByKey(convertAndDeploy(workflowDefinition));
      
    // Complete first task
    TaskService taskService = activitiRule.getTaskService();
    assertEquals(1, taskService.createTaskQuery().taskCandidateGroup("management").count());
    assertEquals(1, taskService.createTaskQuery().taskCandidateGroup("sales").count());
    assertEquals(1, taskService.createTaskQuery().taskCandidateGroupIn(Arrays.asList("management")).count());
    assertEquals(1, taskService.createTaskQuery().taskCandidateGroupIn(Arrays.asList("management", "sales")).count());
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    
    // Second task is only done by sales
    assertEquals(0, taskService.createTaskQuery().taskCandidateGroup("management").count());
    assertEquals(1, taskService.createTaskQuery().taskCandidateGroup("sales").count());
    assertEquals(0, taskService.createTaskQuery().taskCandidateGroupIn(Arrays.asList("management")).count());
    assertEquals(1, taskService.createTaskQuery().taskCandidateGroupIn(Arrays.asList("sales")).count());
    assertEquals(1, taskService.createTaskQuery().taskCandidateGroupIn(Arrays.asList("management", "sales")).count());
  }
  
  @Test
  public void testFeedbackStepWithFixedUsersAllFeedbackProvided() {
    WorkflowDefinition workflowDefinition = new WorkflowDefinition()
      .name("testWorkflow")
      .description("This is a test workflow")
      .addFeedbackStep("Test feedback", "kermit", Arrays.asList("gonzo", "mispiggy", "fozzie"));
    
    activitiRule.getRuntimeService().startProcessInstanceByKey(convertAndDeploy(workflowDefinition));
    
    // First, a task should be assigned to kermit to select the people
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("kermit").count());
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    
    // Four tasks should be available now
    assertEquals(4, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("kermit").count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("gonzo").count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("mispiggy").count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("fozzie").count());
    
    // Completing the feedback tasks first should only leave the 'gather feedback' task for kermit open
    for (Task feedbackTask : taskService.createTaskQuery().list()) {
      if (!feedbackTask.getAssignee().equals("kermit")) {
        activitiRule.getTaskService().complete(feedbackTask.getId());
      }
    }
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("kermit").count());
    
    // Completing this last task should finish the process
    activitiRule.getTaskService().complete(activitiRule.getTaskService().createTaskQuery().singleResult().getId());
    assertEquals(0, activitiRule.getRuntimeService().createProcessInstanceQuery().count());
  }
  
  @Test
  public void testFeedbackStepWithFixedUsersFeedbackHaltedByInitiator() {
    WorkflowDefinition workflowDefinition = new WorkflowDefinition()
      .name("testWorkflow")
      .description("This is a test workflow")
      .addFeedbackStep("Test feedback", "kermit", Arrays.asList("gonzo", "mispiggy", "fozzie"));
    
    activitiRule.getRuntimeService().startProcessInstanceByKey(convertAndDeploy(workflowDefinition));
    
    // First, a task should be assigned to kermit to select the people
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("kermit").count());
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    
    // Four tasks should be available now
    assertEquals(4, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("kermit").count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("gonzo").count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("mispiggy").count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("fozzie").count());
    
    // Completing only one feedback task
    for (Task feedbackTask : taskService.createTaskQuery().list()) {
      if (!feedbackTask.getAssignee().equals("kermit")) {
        activitiRule.getTaskService().complete(feedbackTask.getId());
        break;
      }
    }
    assertEquals(3, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("kermit").count());
    
    // Completing the 'gather feedback' task by kermit should cancel the remaining feedback tasks
    activitiRule.getTaskService().complete(activitiRule.getTaskService().createTaskQuery().taskAssignee("kermit").singleResult().getId());
    assertEquals(0, taskService.createTaskQuery().count());
    assertEquals(0, activitiRule.getRuntimeService().createProcessInstanceQuery().count());
  }
  
  @Test
  public void testFeedbackStepWithUserSelectionAtRuntimeAllFeedbackProvided() {
    WorkflowDefinition workflowDefinition = new WorkflowDefinition()
    .name("testWorkflow")
    .description("This is a test workflow")
    .addFeedbackStep("Test feedback", "kermit");
  
    activitiRule.getRuntimeService().startProcessInstanceByKey(convertAndDeploy(workflowDefinition));
    
    // First, a task should be assigned to kermit to select the people
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("kermit").count());
    Task task = taskService.createTaskQuery().singleResult();
    
    // Completing the task using the predefined process variable (normally done through the form)
    TaskService taskService = activitiRule.getTaskService();
    taskService.complete(task.getId(), CollectionUtil.singletonMap(FeedbackStepDefinitionConverter.VARIABLE_FEEDBACK_PROVIDERS, Arrays.asList("gonzo", "fozzie")));
    
    // Three tasks should be available now
    assertEquals(3, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("kermit").count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("gonzo").count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("fozzie").count());
    
    // Completing the feedback tasks first should only leave the 'gather feedback' task for kermit open
    for (Task feedbackTask : taskService.createTaskQuery().list()) {
      if (!feedbackTask.getAssignee().equals("kermit")) {
        activitiRule.getTaskService().complete(feedbackTask.getId());
      }
    }
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskAssignee("kermit").count());
    
    // Completing this last task should finish the process
    activitiRule.getTaskService().complete(activitiRule.getTaskService().createTaskQuery().singleResult().getId());
    assertEquals(0, activitiRule.getRuntimeService().createProcessInstanceQuery().count());
  }
  
  // Helper methods -----------------------------------------------------------------------------
  
  protected String convertAndDeploy(WorkflowDefinition workflowDefinition) {
   
    // Convert
    WorkflowDefinitionConversion conversion = conversionFactory.createWorkflowDefinitionConversion(workflowDefinition);
    conversion.convert();
    
    log.info("Converted process : " + conversion.getBpmn20Xml());
    
//    InputStream is = conversion.getWorkflowDiagramImage();
//    try {
//      write(is, new FileOutputStream("temp" + UUID.randomUUID().toString() + ".png"), new byte[1024]);
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
    
    // Deploy
    long processDefinitionCount = activitiRule.getRepositoryService().createProcessDefinitionQuery().count();
    deployProcessDefinition(conversion);
    assertEquals(processDefinitionCount + 1, activitiRule.getRepositoryService().createProcessDefinitionQuery().count());
    
    return getDeployedProcessKey();
  }
  
  public static void write(InputStream is, OutputStream os, byte[] buf) throws IOException {
    int numRead;
    while ((numRead = is.read(buf)) >= 0) {
      os.write(buf, 0, numRead);
    }
  }
  
  protected String getDeployedProcessKey() {
    ProcessDefinition processDefinition = activitiRule.getRepositoryService().createProcessDefinitionQuery().singleResult();
    assertNotNull(processDefinition);
    return processDefinition.getKey();
  }
  
  protected void deployProcessDefinition(WorkflowDefinitionConversion conversion) {
    long nrOfDeployments = countNrOfDeployments();
    
    activitiRule.getRepositoryService().createDeployment()
      .addString(conversion.getProcess().getId() + ".bpmn20.xml", conversion.getBpmn20Xml())
      .deploy();
    
    assertEquals(nrOfDeployments + 1, countNrOfDeployments());
  }
  
  protected long countNrOfDeployments() {
    return activitiRule.getRepositoryService().createDeploymentQuery().count();
  }
  
}
