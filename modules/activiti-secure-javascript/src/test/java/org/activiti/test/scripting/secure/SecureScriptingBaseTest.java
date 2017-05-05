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
package org.activiti.test.scripting.secure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.activiti.engine.DynamicBpmnService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.scripting.secure.SecureJavascriptConfigurator;
import org.activiti.scripting.secure.impl.SecureScriptClassShutter;
import org.activiti.tasks.secure.impl.DefaultClassWhitelister;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import junit.framework.AssertionFailedError;

/**
 * @author Joram Barrez
 */
public abstract class SecureScriptingBaseTest {

  protected ProcessEngine processEngine;
  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected DynamicBpmnService dynamicBpmnService;

  @Before
  public void initProcessEngine() {

    SecureJavascriptConfigurator configurator = new SecureJavascriptConfigurator()
        .setWhiteListedClasses(new HashSet<String>(Arrays.asList("java.util.ArrayList", "org.activiti.test.scripting.secure.MyBean")))
        .setMaxStackDepth(10).setMaxScriptExecutionTime(3000L)
        .setMaxMemoryUsed(3145728L);

    Map<Object, Object> beans = new HashMap<Object, Object>();
    beans.put("myBean", new MyBean());
    this.processEngine = new StandaloneInMemProcessEngineConfiguration()
        .addConfigurator(configurator)
        .setBeans(beans)
        .setDatabaseSchemaUpdate("create-drop")
        .setEnableProcessDefinitionInfoCache(true)
        .buildProcessEngine();

    this.runtimeService = processEngine.getRuntimeService();
    this.repositoryService = processEngine.getRepositoryService();
    this.taskService = processEngine.getTaskService();
    this.historyService = processEngine.getHistoryService();
    this.dynamicBpmnService = processEngine.getDynamicBpmnService();
  }

  @After
  public void shutdownProcessEngine() {

    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

    this.taskService = null;
    this.repositoryService = null;
    this.runtimeService = null;

    this.processEngine.close();
    this.processEngine = null;
  }

  public void assertProcessEnded(final String processInstanceId) {
    ProcessInstance processInstance = processEngine
            .getRuntimeService()
            .createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();

    if (processInstance!=null) {
      throw new AssertionFailedError("Expected finished process instance '"+processInstanceId+"' but it was still in the db");
    }

    // Verify historical data if end times are correctly set
    if (processEngine.getProcessEngineConfiguration().getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {

      // process instance
      HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
              .processInstanceId(processInstanceId).singleResult();
      Assert.assertEquals(processInstanceId, historicProcessInstance.getId());
      Assert.assertNotNull("Historic process instance has no start time", historicProcessInstance.getStartTime());
      Assert.assertNotNull("Historic process instance has no end time", historicProcessInstance.getEndTime());

      // tasks
      List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
              .processInstanceId(processInstanceId).list();
      if (historicTaskInstances != null && historicTaskInstances.size() > 0) {
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
          Assert.assertEquals(processInstanceId, historicTaskInstance.getProcessInstanceId());
          Assert.assertNotNull("Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no start time", historicTaskInstance.getStartTime());
          Assert.assertNotNull("Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no end time", historicTaskInstance.getEndTime());
        }
      }

      // activities
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
              .processInstanceId(processInstanceId).list();
      if (historicActivityInstances != null && historicActivityInstances.size() > 0) {
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
          Assert.assertEquals(processInstanceId, historicActivityInstance.getProcessInstanceId());
          Assert.assertNotNull("Historic activity instance " + historicActivityInstance.getActivityId() + " has no start time", historicActivityInstance.getStartTime());
          Assert.assertNotNull("Historic activity instance " + historicActivityInstance.getActivityId() + " has no end time", historicActivityInstance.getEndTime());
        }
      }
    }
  }

  protected void deployProcessDefinition(String classpathResource) {
    repositoryService.createDeployment().addClasspathResource(classpathResource).deploy();
  }

  protected void enableSysoutsInScript() {
    addWhiteListedClass("java.lang.System");
    addWhiteListedClass("java.io.PrintStream");
  }

  protected void addWhiteListedClass(String whiteListedClass) {
    SecureScriptClassShutter secureScriptClassShutter = SecureJavascriptConfigurator.getSecureScriptClassShutter();
    ((DefaultClassWhitelister)secureScriptClassShutter.getClassWhitelister()).addWhiteListedClass(whiteListedClass);
  }
  
  protected void removeWhiteListedClass(String whiteListedClass) {
    SecureScriptClassShutter secureScriptClassShutter = SecureJavascriptConfigurator.getSecureScriptClassShutter();
    ((DefaultClassWhitelister)secureScriptClassShutter.getClassWhitelister()).removeWhiteListedClass(whiteListedClass);
  }

}
