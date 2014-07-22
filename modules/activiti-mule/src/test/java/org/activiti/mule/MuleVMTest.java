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

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Assert;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;

/**
 * @author Esteban Robles Luna
 * @author Tijs Rademakers
 */
public class MuleVMTest extends FunctionalTestCase {

  @Test
  public void testSend() {
    Assert.assertTrue(muleContext.isStarted());
    
    RepositoryService repositoryService = muleContext.getRegistry().get("repositoryService");
    repositoryService.createDeployment().addClasspathResource("org/activiti/mule/testVM.bpmn20.xml").deploy();
    RuntimeService runtimeService = muleContext.getRegistry().get("runtimeService");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("muleProcess");
    Assert.assertFalse(processInstance.isEnded());
    Object result = runtimeService.getVariable(processInstance.getProcessInstanceId(), "theVariable");
    Assert.assertEquals(30, result);
  }

  @Override
  protected String getConfigResources() {
    return "mule-config.xml";
  }
}
