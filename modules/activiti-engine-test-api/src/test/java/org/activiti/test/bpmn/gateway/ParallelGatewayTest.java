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

package org.activiti.test.bpmn.gateway;

import static org.junit.Assert.assertTrue;

import org.activiti.ProcessInstance;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeclared;
import org.activiti.test.ProcessDeployer;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class ParallelGatewayTest {

  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  /**
   * Case where there is a parallel gateway that splits into 3 paths of
   * execution, that are immediately joined, without any wait states in between.
   * In the end, no executions should be in the database.
   */
  @Test
  @ProcessDeclared
  public void testSplitMergeNoWaitstates() {
    ProcessInstance processInstance = 
      deployer.getProcessService().startProcessInstanceByKey("forkJoinNoWaitStates");
    assertTrue(processInstance.isEnded());
  }
  
  @Test
  @ProcessDeclared
  public void testUnstructuredConcurrencyTwoForks() {
    ProcessInstance processInstance = 
      deployer.getProcessService().startProcessInstanceByKey("unstructuredConcurrencyTwoForks");
    assertTrue(processInstance.isEnded());
  }
  
  @Test
  @ProcessDeclared
  public void testUnstructuredConcurrencyTwoJoins() {
    ProcessInstance processInstance = 
      deployer.getProcessService().startProcessInstanceByKey("unstructuredConcurrencyTwoJoins");
    assertTrue(processInstance.isEnded());
  }

}
