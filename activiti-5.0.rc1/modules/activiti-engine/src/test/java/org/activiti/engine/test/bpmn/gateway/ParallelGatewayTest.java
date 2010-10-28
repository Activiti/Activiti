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

package org.activiti.engine.test.bpmn.gateway;

import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 */
public class ParallelGatewayTest extends ActivitiInternalTestCase {

  /**
   * Case where there is a parallel gateway that splits into 3 paths of
   * execution, that are immediately joined, without any wait states in between.
   * In the end, no executions should be in the database.
   */
  @Deployment
  public void testSplitMergeNoWaitstates() {
    ProcessInstance processInstance = 
      runtimeService.startProcessInstanceByKey("forkJoinNoWaitStates");
    assertTrue(processInstance.isEnded());
  }
  
  @Deployment
  public void testUnstructuredConcurrencyTwoForks() {
    ProcessInstance processInstance = 
      runtimeService.startProcessInstanceByKey("unstructuredConcurrencyTwoForks");
    assertTrue(processInstance.isEnded());
  }
  
  @Deployment
  public void testUnstructuredConcurrencyTwoJoins() {
    ProcessInstance processInstance = 
      runtimeService.startProcessInstanceByKey("unstructuredConcurrencyTwoJoins");
    assertTrue(processInstance.isEnded());
  }
  
  @Deployment
  public void testForkFollowedByOnlyEndEvents() {
    ProcessInstance processInstance = 
      runtimeService.startProcessInstanceByKey("forkFollowedByEndEvents");
    assertTrue(processInstance.isEnded());
  }
  
  @Deployment
  public void testNestedForksFollowedByEndEvents() {
    ProcessInstance processInstance = 
      runtimeService.startProcessInstanceByKey("nestedForksFollowedByEndEvents");
    assertTrue(processInstance.isEnded());
  }
  
}
