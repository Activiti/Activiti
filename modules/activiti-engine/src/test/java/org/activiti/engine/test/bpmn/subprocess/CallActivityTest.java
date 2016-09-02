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

package org.activiti.engine.test.bpmn.subprocess;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.impl.util.io.StreamSource;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

public class CallActivityTest extends ResourceActivitiTestCase {

  private static String MAIN_PROCESS_RESOURCE = "org/activiti/engine/test/bpmn/subprocess/SubProcessTest.testSuspendedProcessCallActivity_mainProcess.bpmn.xml";
  private static String CHILD_PROCESS_RESOURCE = "org/activiti/engine/test/bpmn/subprocess/SubProcessTest.testSuspendedProcessCallActivity_childProcess.bpmn.xml";
  private static String MESSAGE_TRIGGERED_PROCESS_RESOURCE = "org/activiti/engine/test/bpmn/subprocess/SubProcessTest.testSuspendedProcessCallActivity_messageTriggeredProcess.bpmn.xml";
  private static String INHERIT_VARIABLES_MAIN_PROCESS_RESOURCE = "org/activiti/engine/test/bpmn/subprocess/SubProcessTest.testInheritVariablesCallActivity_mainProcess.bpmn20.xml";
  private static String INHERIT_VARIABLES_CHILD_PROCESS_RESOURCE = "org/activiti/engine/test/bpmn/subprocess/SubProcessTest.testInheritVariablesCallActivity_childProcess.bpmn20.xml";
  private static String NOT_INHERIT_VARIABLES_MAIN_PROCESS_RESOURCE = "org/activiti/engine/test/bpmn/subprocess/SubProcessTest.testNotInheritVariablesCallActivity_mainProcess.bpmn20.xml";

  public CallActivityTest() {
    super("org/activiti/standalone/parsing/encoding.activiti.cfg.xml");
  }

  public void testInstantiateProcessByMessage() throws Exception {
    BpmnModel messageTriggeredBpmnModel = loadBPMNModel(MESSAGE_TRIGGERED_PROCESS_RESOURCE);

    Deployment messageTriggeredBpmnDeployment = processEngine.getRepositoryService()
        .createDeployment()
        .name("messageTriggeredProcessDeployment")
        .addBpmnModel("messageTriggered.bpmn20.xml", messageTriggeredBpmnModel).deploy();

    ProcessInstance childProcessInstance = runtimeService.startProcessInstanceByMessage("TRIGGER_PROCESS_MESSAGE");
    assertNotNull(childProcessInstance);
  }

  public void testInstantiateSuspendedProcessByMessage() throws Exception {
    BpmnModel messageTriggeredBpmnModel = loadBPMNModel(MESSAGE_TRIGGERED_PROCESS_RESOURCE);

    Deployment messageTriggeredBpmnDeployment = processEngine.getRepositoryService()
        .createDeployment()
        .name("messageTriggeredProcessDeployment")
        .addBpmnModel("messageTriggered.bpmn20.xml", messageTriggeredBpmnModel).deploy();

    suspendProcessDefinitions(messageTriggeredBpmnDeployment);

    try {
      ProcessInstance childProcessInstance = runtimeService.startProcessInstanceByMessage("TRIGGER_PROCESS_MESSAGE");
      fail("Exception expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot start process instance. Process definition Message Triggered Process", ae.getMessage());
    }

  }

  public void testInstantiateChildProcess() throws Exception {
    BpmnModel childBpmnModel = loadBPMNModel(CHILD_PROCESS_RESOURCE);

    Deployment childDeployment = processEngine.getRepositoryService()
        .createDeployment()
        .name("childProcessDeployment")
        .addBpmnModel("childProcess.bpmn20.xml", childBpmnModel).deploy();

    ProcessInstance childProcessInstance = runtimeService.startProcessInstanceByKey("childProcess");
    assertNotNull(childProcessInstance);
  }

  public void testInstantiateSuspendedChildProcess() throws Exception {
    BpmnModel childBpmnModel = loadBPMNModel(CHILD_PROCESS_RESOURCE);

    Deployment childDeployment = processEngine.getRepositoryService()
        .createDeployment()
        .name("childProcessDeployment")
        .addBpmnModel("childProcess.bpmn20.xml", childBpmnModel).deploy();

    suspendProcessDefinitions(childDeployment);

    try {
      ProcessInstance childProcessInstance = runtimeService.startProcessInstanceByKey("childProcess");
      fail("Exception expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot start process instance. Process definition Child Process", ae.getMessage());
    }

  }


  public void testInstantiateSubprocess() throws Exception {
    BpmnModel mainBpmnModel = loadBPMNModel(MAIN_PROCESS_RESOURCE);
    BpmnModel childBpmnModel = loadBPMNModel(CHILD_PROCESS_RESOURCE);

    Deployment childDeployment = processEngine.getRepositoryService()
        .createDeployment()
        .name("childProcessDeployment")
        .addBpmnModel("childProcess.bpmn20.xml", childBpmnModel).deploy();

    Deployment masterDeployment = processEngine.getRepositoryService()
        .createDeployment()
        .name("masterProcessDeployment")
        .addBpmnModel("masterProcess.bpmn20.xml", mainBpmnModel).deploy();

    suspendProcessDefinitions(childDeployment);

    try {
      ProcessInstance masterProcessInstance = runtimeService.startProcessInstanceByKey("masterProcess");
      fail("Exception expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot start process instance. Process definition Child Process", ae.getMessage());
    }

  }

  public void testInheritVariablesSubprocess() throws Exception {
    BpmnModel mainBpmnModel = loadBPMNModel(INHERIT_VARIABLES_MAIN_PROCESS_RESOURCE);
    BpmnModel childBpmnModel = loadBPMNModel(INHERIT_VARIABLES_CHILD_PROCESS_RESOURCE);

    processEngine.getRepositoryService()
        .createDeployment()
        .name("mainProcessDeployment")
        .addBpmnModel("mainProcess.bpmn20.xml", mainBpmnModel).deploy();

    processEngine.getRepositoryService()
        .createDeployment()
        .name("childProcessDeployment")
        .addBpmnModel("childProcess.bpmn20.xml", childBpmnModel).deploy();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("var1", "String test value");
    variables.put("var2", true);
    variables.put("var3", 12345);
    variables.put("var4", 67890);

    ProcessInstance mainProcessInstance = runtimeService.startProcessInstanceByKey("mainProcess", variables);

    HistoricActivityInstanceQuery activityInstanceQuery = historyService.createHistoricActivityInstanceQuery();
    activityInstanceQuery.processInstanceId(mainProcessInstance.getId());
    activityInstanceQuery.activityId("childProcessCall");
    HistoricActivityInstance activityInstance = activityInstanceQuery.singleResult();
    String calledInstanceId = activityInstance.getCalledProcessInstanceId();

    HistoricVariableInstanceQuery variableInstanceQuery = historyService.createHistoricVariableInstanceQuery();
    List<HistoricVariableInstance> variableInstances = variableInstanceQuery.processInstanceId(calledInstanceId).list();

    assertEquals(4, variableInstances.size());
    for (HistoricVariableInstance variable : variableInstances) {
      assertEquals(variables.get(variable.getVariableName()), variable.getValue());
    }
  }

  public void testNotInheritVariablesSubprocess() throws Exception {
    BpmnModel mainBpmnModel = loadBPMNModel(NOT_INHERIT_VARIABLES_MAIN_PROCESS_RESOURCE);
    BpmnModel childBpmnModel = loadBPMNModel(INHERIT_VARIABLES_CHILD_PROCESS_RESOURCE);

    processEngine.getRepositoryService()
        .createDeployment()
        .name("childProcessDeployment")
        .addBpmnModel("childProcess.bpmn20.xml", childBpmnModel).deploy();
    
    processEngine.getRepositoryService()
        .createDeployment()
        .name("mainProcessDeployment")
        .addBpmnModel("mainProcess.bpmn20.xml", mainBpmnModel).deploy();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("var1", "String test value");
    variables.put("var2", true);
    variables.put("var3", 12345);
    variables.put("var4", 67890);

    ProcessInstance mainProcessInstance = runtimeService.startProcessInstanceByKey("mainProcess", variables);

    HistoricActivityInstanceQuery activityInstanceQuery = historyService.createHistoricActivityInstanceQuery();
    activityInstanceQuery.processInstanceId(mainProcessInstance.getId());
    activityInstanceQuery.activityId("childProcessCall");
    HistoricActivityInstance activityInstance = activityInstanceQuery.singleResult();
    String calledInstanceId = activityInstance.getCalledProcessInstanceId();

    HistoricVariableInstanceQuery variableInstanceQuery = historyService.createHistoricVariableInstanceQuery();
    variableInstanceQuery.processInstanceId(calledInstanceId);
    List<HistoricVariableInstance> variableInstances = variableInstanceQuery.list();

    assertEquals(0, variableInstances.size());
  }

  private void suspendProcessDefinitions(Deployment childDeployment) {
    List<ProcessDefinition> childProcessDefinitionList = repositoryService.createProcessDefinitionQuery().deploymentId(childDeployment.getId()).list();

    for (ProcessDefinition processDefinition : childProcessDefinitionList) {
      repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    }
  }

  protected void tearDown() throws Exception {
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    super.tearDown();
  }


  protected BpmnModel loadBPMNModel(String bpmnModelFilePath) throws Exception {
    InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream(bpmnModelFilePath);
    StreamSource xmlSource = new InputStreamSource(xmlStream);
    BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xmlSource, false, false, processEngineConfiguration.getXmlEncoding());
    return bpmnModel;
  }


}
