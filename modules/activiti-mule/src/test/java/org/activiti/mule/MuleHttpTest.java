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
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Assert;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;

/**
 * @author Esteban Robles Luna
 * @author Tijs Rademakers
 */
public class MuleHttpTest extends FunctionalTestCase {
  
  @Test
  public void http() throws Exception {
    Assert.assertTrue(muleContext.isStarted());
    
    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    processEngine.getRepositoryService().createDeployment().addClasspathResource("org/activiti/mule/testHttp.bpmn20.xml").deploy();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("muleProcess");
    Assert.assertFalse(processInstance.isEnded());
    Object result = runtimeService.getVariable(processInstance.getProcessInstanceId(), "theVariable");
    Assert.assertEquals(20, result);
  }
  
  @Override
  protected String getConfigResources() {
    return "mule-http-config.xml";
  }
}
