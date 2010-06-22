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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.Deployment;
import org.activiti.DeploymentBuilder;
import org.activiti.impl.bpmn.BpmnDeployer;
import org.junit.Rule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

/**
 * @author Tom Baeyens
 */
public abstract class ActivitiTestCase {

  private static Logger log = Logger.getLogger(ActivitiTestCase.class.getName());

  @Rule
  public LogInitializer logSetup = new LogInitializer();

  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Rule
  public ProcessEngineBuilder processEngineBuilder = new ProcessEngineBuilder();

  public class ProcessDeployer extends TestWatchman {

    private Set<String> registeredDeploymentIds = new HashSet<String>();

    @Override
    public void starting(FrameworkMethod method) {

       ProcessDeclared process = method.getAnnotation(ProcessDeclared.class);
      if (process != null) {
        String[] resources = process.resources();
        resources = resources.length == 0 ? process.value() : resources;
        if (resources.length == 0) {
          String name = method.getName();
          String resource = ResourceUtils.getBpmnProcessDefinitionResource(method.getMethod().getDeclaringClass(), name);
          log.fine("deploying bpmn process resource: " + resource);
          deployProcessResource(resource);
        } else {
          DeploymentBuilder builder = processEngineBuilder.getProcessService().createDeployment();
          for (String resource : resources) {
            if (resource.startsWith("/")) {
              resource = resource.substring(1);
            } else {
              resource = ResourceUtils.getProcessDefinitionResource(method.getMethod().getDeclaringClass(), resource);
            }
            builder.addClasspathResource(resource);
          }
          Deployment deployment = builder.deploy();
          registerDeployment(deployment.getId());
        }
      }

    }

    @Override
    public void finished(FrameworkMethod method) {
      for (String deploymentId : registeredDeploymentIds) {
        processEngineBuilder.getProcessService().deleteDeploymentCascade(deploymentId);
      }
    }

    public void deployProcessString(String xmlString) {
      deployProcessString("xmlString." + BpmnDeployer.BPMN_RESOURCE_SUFFIX, xmlString);
    }

    private String deployProcessResource(String resource) {
      Deployment deployment = processEngineBuilder.getProcessService().createDeployment().name(resource).addClasspathResource(resource)
              .deploy();
      registerDeployment(deployment.getId());
      return deployment.getId();
    }

    private void deployProcessString(String resourceName, String xmlString) {
      Deployment deployment = processEngineBuilder.getProcessEngine().getProcessService().createDeployment().name(resourceName)
              .addString(resourceName, xmlString).deploy();
      registerDeployment(deployment.getId());
    }

    /**
     * Registers the given deployment for post-test clean up. All the related data
     * such as process instances, tasks, etc will be deleted when the test case
     * has run.
     */
    private void registerDeployment(String deploymentId) {
      if (deploymentId == null) { // common error
        throw new ActivitiException("Trying to add a deploymentid which is null." + "This is not possible and probably due to not using a resource name "
                + "with a recognized extension.");
      }
      registeredDeploymentIds.add(deploymentId);
    }

  }

}
