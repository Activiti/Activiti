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
package org.activiti.mule;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.mule.tck.FunctionalTestCase;

/**
 * @author Esteban Robles Luna
 */
public class MuleSendActivitiBehaviorTest extends FunctionalTestCase {

  @Deployment
  public void testSend() {
    ProcessEngine processEngine = muleContext.getRegistry().get("processEngine");
    String deploymentId = TestHelper.annotationDeploymentSetUp(processEngine, getClass(), getName());

    RuntimeService runtimeService = muleContext.getRegistry().get("runtimeService");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("muleProcess");
    assertFalse(processInstance.isEnded());
    Object result = runtimeService.getVariable(processInstance.getProcessInstanceId(), "theVariable");
    assertEquals(10, result);
  }

  @Override
  protected String getConfigResources() {
    return "mule-config.xml";
  }
}
