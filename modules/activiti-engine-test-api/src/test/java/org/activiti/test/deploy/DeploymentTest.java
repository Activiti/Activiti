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
package org.activiti.test.deploy;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.activiti.Deployment;
import org.activiti.impl.util.IoUtil;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeployer;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DeploymentTest {

  private static final String TO_REPLACE = "to_replace";

  private static final String MINIMAL_PROC_DEF = "<definitions xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL' "
          + "             targetNamespace='http://www.activiti.org/bpmn2.0' >" + "  <process id='" + TO_REPLACE + "' />" + "</definitions>";

  @Rule
  public LogInitializer logSetup = new LogInitializer();

  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Test
  public void testSimpleString() {
    deployer.deployProcessString("<definitions xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL' targetNamespace='http://www.activiti.org/bpmn2.0' />");
  }

  @Test
  public void testMultipleStringResources() throws Exception {

    List<String> deploymentIds = deployTestProcesses();
    List<Deployment> deployments = deployer.getProcessService().findDeployments();
    assertEquals(3, deployments.size());

    // Results should be ordered by deployment time
    assertEquals("idrDeployment", deployments.get(0).getName());
    assertEquals("expenseDeployment", deployments.get(1).getName());
    assertEquals("hiringDeployment", deployments.get(2).getName());

    // Resources should be ordered by name
    String deploymentId = deploymentIds.get(0);
    List<String> resources = deployer.getProcessService().findDeploymentResources(deploymentId);
    assertEquals(2, resources.size());
    assertEquals("idr_process01.bpmn20.xml", resources.get(0));
    assertEquals("idr_process02.bpmn20.xml", resources.get(1));

    // Validate the content of the deployment resources
    InputStream resourceIs = deployer.getProcessService().getDeploymentResourceContent(deploymentId, resources.get(0));
    assertEquals(MINIMAL_PROC_DEF.replace(TO_REPLACE, "IDR1"), new String(IoUtil.readInputStream(resourceIs, null)));
    resourceIs.close();
    resourceIs = deployer.getProcessService().getDeploymentResourceContent(deploymentId, resources.get(1));
    assertEquals(MINIMAL_PROC_DEF.replace(TO_REPLACE, "IDR2"), new String(IoUtil.readInputStream(resourceIs, null)));
    resourceIs.close();

  }

  /*
   * Deploys two test processes: - one deployment called 'idrDeployment' with
   * two bpmn processes - one deployment called 'expenseDeployment' with one
   * bpmn process - one deployment called 'hiringDeployment' with three bpmn
   * processes
   * 
   * The list that is returned contains the ids of the deployments, in the order
   * as defined above.
   */
  private List<String> deployTestProcesses() {

    final String idrDeploymentName = "idrDeployment";
    final String expenseDeploymentName = "expenseDeployment";
    final String hiringDeploymentName = "hiringDeployment";

    Deployment deployment1 = deployer.createDeployment().name(idrDeploymentName).addString("idr_process01.bpmn20.xml",
            MINIMAL_PROC_DEF.replace(TO_REPLACE, "IDR1")).addString("idr_process02.bpmn20.xml", MINIMAL_PROC_DEF.replace(TO_REPLACE, "IDR2")).deploy();

    Deployment deployment2 = deployer.createDeployment().name(expenseDeploymentName).addString("expense_proc.bpmn20.xml",
            MINIMAL_PROC_DEF.replace(TO_REPLACE, "EXP")).deploy();

    Deployment deployment3 = deployer.createDeployment().name(hiringDeploymentName).addString("hiring_process.bpmn20.xml",
            MINIMAL_PROC_DEF.replace(TO_REPLACE, "HIR")).addString("hiring_remote_employee.bpmn20.xml", MINIMAL_PROC_DEF.replace(TO_REPLACE, "HIR_REM"))
            .addString("hiring_process_sales.bpmn20.xml", MINIMAL_PROC_DEF.replace(TO_REPLACE, "HIR_SAL")).deploy();

    assertEquals(idrDeploymentName, deployment1.getName());
    assertEquals(expenseDeploymentName, deployment2.getName());

    return Arrays.asList(deployment1.getId(), deployment2.getId(), deployment3.getId());
  }

}
