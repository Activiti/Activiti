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
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversionFactory;
import org.activiti.workflow.simple.converter.listener.DefaultWorkflowDefinitionConversionListener;
import org.activiti.workflow.simple.converter.listener.WorkflowDefinitionConversionListener;
import org.activiti.workflow.simple.converter.step.HumanStepDefinitionConverter;
import org.activiti.workflow.simple.converter.step.ParallelStepsDefinitionConverter;
import org.activiti.workflow.simple.converter.step.StepDefinitionConverter;
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
  
  protected WorkflowDefinitionConversionFactory conversionFactory;
  
  @Before
  public void initialiseTest() {
    
    // Alternatively, the following setup could be done using a dependency injection container
    
    conversionFactory = new WorkflowDefinitionConversionFactory();
    
    // Steps
    List<StepDefinitionConverter> stepConverters = new ArrayList<StepDefinitionConverter>();
    stepConverters.add(new HumanStepDefinitionConverter());
    stepConverters.add(new ParallelStepsDefinitionConverter());
    conversionFactory.setStepDefinitionConverters(stepConverters);
    
    // Listeners
    List<WorkflowDefinitionConversionListener> conversionListeners = new ArrayList<WorkflowDefinitionConversionListener>();
    conversionListeners.add(new DefaultWorkflowDefinitionConversionListener());
    conversionFactory.setWorkflowDefinitionConversionListeners(conversionListeners);
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
        .addHumanStep("first task", "kermit")
        .addHumanStep("second step", "gonzo")
        .addHumanStep("thrid task", "mispiggy")
      .endParallel()
      .addHumanStep("Task in between", "kermit")
      .inParallel()
        .addHumanStep("fourth task", "gonzo")
        .addHumanStep("fifth step", "gonzo")
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
  
  // Helper methods -----------------------------------------------------------------------------
  
  protected String convertAndDeploy(WorkflowDefinition workflowDefinition) {
   
    // Convert
    WorkflowDefinitionConversion conversion = conversionFactory.createWorkflowDefinitionConversion(workflowDefinition);
    conversion.convert();
    log.info("Converted process : " + conversion.getbpm20Xml());
    
//    InputStream is = conversion.getWorkflowDiagramImage();
//    try {
//      flow(is, new FileOutputStream("temp" + UUID.randomUUID().toString() + ".png"), new byte[1024]);
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
    
    // Deploy
    deployProcessDefinition(conversion);
    return getDeployedProcessKey();
  }
  
  public static void flow( InputStream is, OutputStream os, byte[] buf ) 
          throws IOException {
          int numRead;
          while ( (numRead = is.read(buf) ) >= 0) {
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
      .addString(conversion.getProcess().getId() + ".bpmn20.xml", conversion.getbpm20Xml())
      .deploy();
    
    assertEquals(nrOfDeployments + 1, countNrOfDeployments());
  }
  
  protected long countNrOfDeployments() {
    return activitiRule.getRepositoryService().createDeploymentQuery().count();
  }
  
}
