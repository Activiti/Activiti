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
package org.activiti.test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.Deployment;
import org.activiti.DeploymentBuilder;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.bpmn.BpmnDeployer;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.jobexecutor.JobExecutor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

/**
 * @author Tom Baeyens
 */
public abstract class ActivitiTestCase extends ProcessEngineTestCase {

  private static Logger log = Logger.getLogger(ActivitiTestCase.class.getName());

  @Rule
  public LogInitializer logSetup = new LogInitializer();

  @Rule
  public ProcessDeployer undeployer = new ProcessDeployer();

  public class ProcessDeployer extends TestWatchman {

    @Override
    public void starting(FrameworkMethod method) {
      ProcessDeclared process = method.getAnnotation(ProcessDeclared.class);
      if (process != null) {
        String[] resources = process.resources();
        resources = resources.length == 0 ? process.value() : resources;
        if (resources.length == 0) {
          String name = method.getName();
          String resource = getBpmnProcessDefinitionResource(method.getMethod().getDeclaringClass(), name);
          log.fine("deploying bpmn process resource: " + resource);
          deployProcessResource(resource);
        } else {
          DeploymentBuilder builder = processService.createDeployment();
          for (String resource : resources) {
            if (resource.startsWith("/")) {
              resource = resource.substring(1);
            } else {
              resource = getProcessDefinitionResource(method.getMethod().getDeclaringClass(), resource);
            }
            builder.addClasspathResource(resource);
          }
          Deployment deployment = builder.deploy();
          registerDeployment(deployment.getId());
        }
      }
    }

  }

  private static String getBpmnProcessDefinitionResource(Class< ? > type, String name) {
    return type.getName().replace('.', '/') + "." + name + "." + BpmnDeployer.BPMN_RESOURCE_SUFFIX;
  }

  public static String getProcessDefinitionResource(Class< ? > type, String name) {
    String path = type.getName();
    path = path.substring(0, path.lastIndexOf(type.getSimpleName())-1);
    System.err.println(path);
    return path.replace('.', '/') + "/" + name;
  }

  protected Set<String> registeredDeploymentIds = new HashSet<String>();

  @After
  public void finished() {
    for (String deploymentId : registeredDeploymentIds) {
      processService.deleteDeploymentCascade(deploymentId);
    }
  }

  private String deployProcessResource(String resource) {
    Deployment deployment = processEngine.getProcessService().createDeployment().name(resource).addClasspathResource(resource).deploy();
    registerDeployment(deployment.getId());
    return deployment.getId();
  }

  public void deployProcessString(String xmlString) {
    deployProcessString("xmlString." + BpmnDeployer.BPMN_RESOURCE_SUFFIX, xmlString);
  }

  private void deployProcessString(String resourceName, String xmlString) {
    Deployment deployment = processEngine.getProcessService().createDeployment().name(resourceName).addString(resourceName, xmlString).deploy();
    registerDeployment(deployment.getId());
  }

  /**
   * Registers the given deployment for post-test clean up. All the related data
   * such as process instances, tasks, etc will be deleted when the test case
   * has run.
   */
  protected void registerDeployment(String deploymentId) {
    if (deploymentId == null) { // common error
      throw new ActivitiException("Trying to add a deploymentid which is null." + "This is not possible and probably due to not using a resource name "
              + "with a recognized extension.");
    }
    registeredDeploymentIds.add(deploymentId);
  }

  /* Deletion helpers */

  /**
   * 
   * @param deploymentIds
   */
  protected void deleteDeploymentsCascade(Collection<String> deploymentIds) {
    for (String id : deploymentIds) {
      processService.deleteDeploymentCascade(id);
    }
  }

  protected void deleteTasks(Collection<String> taskIds) {
    for (String id : taskIds) {
      taskService.deleteTask(id);
    }
  }

  /* Assertion helpers */

  public void assertProcessInstanceEnded(String processInstanceId) {
    Assert.assertNull("An active execution with id " + processInstanceId + " was found.", processEngine.getProcessService().findProcessInstanceById(
            processInstanceId));
  }

  protected static class InteruptTask extends TimerTask {

    ActivitiTestCase jobExecutorTestCase;
    Thread thread;

    public InteruptTask(ActivitiTestCase jobExecutorTestCase, Thread thread) {
      this.jobExecutorTestCase = jobExecutorTestCase;
      this.thread = thread;
    }
    public void run() {
      jobExecutorTestCase.timeLimitExceeded = true;
      thread.interrupt();
    }
  }

  boolean timeLimitExceeded = false;

  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
    JobExecutor jobExecutor = ((ProcessEngineImpl) processEngine).getJobExecutor();
    jobExecutor.start();

    try {
      Timer timer = new Timer();
      timer.schedule(new InteruptTask(this, Thread.currentThread()), maxMillisToWait);
      boolean areJobsAvailable = true;
      try {
        while (areJobsAvailable && !timeLimitExceeded) {
          Thread.sleep(intervalMillis);
          areJobsAvailable = areJobsAvailable();
        }
      } catch (InterruptedException e) {
      } finally {
        timer.cancel();
      }
      if (areJobsAvailable) {
        throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  public boolean areJobsAvailable() {
    ProcessEngineImpl processEngineImpl = (ProcessEngineImpl) processEngine;
    CommandExecutor commandExecutor = processEngineImpl.getProcessEngineConfiguration().getCommandExecutor();
    Boolean areJobsAvailable = commandExecutor.execute(new Command<Boolean>() {

      public Boolean execute(CommandContext commandContext) {
        return !commandContext.getPersistenceSession().findNextJobsToExecute(1).isEmpty();
      }
    });
    return areJobsAvailable;
  }
}
