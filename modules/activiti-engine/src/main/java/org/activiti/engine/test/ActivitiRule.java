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

package org.activiti.engine.test;

import java.util.Date;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.impl.util.ClockUtil;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;


/** Convenience for ProcessEngine and services initialization in the form of a JUnit rule.
 * 
 * <p>Usage:</p>
 * <pre>public class YourTest {
 * 
 *   &#64;Rule
 *   public ActivitiRule activitiRule = new ActivitiRule();
 *   
 *   ...
 * }
 * </pre>
 * 
 * <p>The ProcessEngine and the services will be made available to the test class 
 * through the getters of the activitiRule.  
 * The processEngine will be initialized by default with the activiti.cfg.xml resource 
 * on the classpath.  To specify a different configuration file, pass the 
 * resource location in {@link #ActivitiRule(String) the appropriate constructor}.
 * Process engines will be cached statically.  Right before the first time the setUp is called for a given 
 * configuration resource, the process engine will be constructed.</p>
 * 
 * <p>You can declare a deployment with the {@link Deployment} annotation.
 * This base class will make sure that this deployment gets deployed before the
 * setUp and {@link RepositoryService#deleteDeploymentCascade(String) cascade deleted}
 * after the tearDown.
 * </p>
 * 
 * <p>The activitiRule also lets you {@link ActivitiRule#setCurrentTime(Date) set the current time used by the 
 * process engine}. This can be handy to control the exact time that is used by the engine
 * in order to verify e.g. e.g. due dates of timers.  Or start, end and duration times
 * in the history service.  In the tearDown, the internal clock will automatically be 
 * reset to use the current system time rather then the time that was set during 
 * a test method.  In other words, you don't have to clean up your own time messing mess ;-)
 * </p>
 *  
 * @author Tom Baeyens
 */
public class ActivitiRule extends TestWatchman {

  protected String configurationResource = "activiti.cfg.xml";
  protected String deploymentId = null;

  protected ProcessEngine processEngine;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected ManagementService managementService;
  
  public ActivitiRule() {
  }

  public ActivitiRule(String configurationResource) {
    this.configurationResource = configurationResource;
  }
  
  public ActivitiRule(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  @Override
  public void starting(FrameworkMethod method) {
    if (processEngine==null) {
      initializeProcessEngine();
      initializeServices();
    }

    deploymentId = TestHelper.annotationDeploymentSetUp(processEngine, method.getMethod().getDeclaringClass(), method.getName());
  }
  
  protected void initializeProcessEngine() {
    processEngine = TestHelper.getProcessEngine(configurationResource);
  }

  protected void initializeServices() {
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    historyService = processEngine.getHistoryService();
    identityService = processEngine.getIdentityService();
    managementService = processEngine.getManagementService();
  }

  @Override
  public void finished(FrameworkMethod method) {
    TestHelper.annotationDeploymentTearDown(processEngine, deploymentId, method.getMethod().getDeclaringClass(), method.getName());

    ClockUtil.reset();
  }
  
  public void setCurrentTime(Date currentTime) {
    ClockUtil.setCurrentTime(currentTime);
  }

  public String getConfigurationResource() {
    return configurationResource;
  }
  
  public void setConfigurationResource(String configurationResource) {
    this.configurationResource = configurationResource;
  }
  
  public ProcessEngine getProcessEngine() {
    return processEngine;
  }
  
  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }
  
  public RepositoryService getRepositoryService() {
    return repositoryService;
  }
  
  public void setRepositoryService(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }
  
  public RuntimeService getRuntimeService() {
    return runtimeService;
  }
  
  public void setRuntimeService(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
  }
  
  public TaskService getTaskService() {
    return taskService;
  }
  
  public void setTaskService(TaskService taskService) {
    this.taskService = taskService;
  }
  
  public HistoryService getHistoryService() {
    return historyService;
  }

  public void setHistoricDataService(HistoryService historicDataService) {
    this.historyService = historicDataService;
  }
  
  public IdentityService getIdentityService() {
    return identityService;
  }
  
  public void setIdentityService(IdentityService identityService) {
    this.identityService = identityService;
  }
  
  public ManagementService getManagementService() {
    return managementService;
  }
  
  public void setManagementService(ManagementService managementService) {
    this.managementService = managementService;
  }
}
