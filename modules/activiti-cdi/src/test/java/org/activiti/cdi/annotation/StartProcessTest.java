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
package org.activiti.cdi.annotation;

import org.activiti.cdi.BusinessProcess;
import org.activiti.cdi.CdiActivitiTestCase;
import org.activiti.cdi.impl.annotation.StartProcessInterceptor;
import org.activiti.cdi.test.beans.DeclarativeProcessController;
import org.activiti.engine.test.Deployment;

/**
 * Testcase for assuring that the {@link StartProcessInterceptor} behaves as
 * expected.
 * 
 * @author Daniel Meyer
 */
public class StartProcessTest extends CdiActivitiTestCase {

  @Deployment(resources = "org/activiti/cdi/annotation/StartProcessTest.bpmn20.xml")
  public void testStartProcessByKey() {

    assertNull(runtimeService.createProcessInstanceQuery().singleResult());

    getBeanInstance(DeclarativeProcessController.class).startProcessByKey();
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    assertNotNull(runtimeService.createProcessInstanceQuery().singleResult());

    assertEquals("Activiti", businessProcess.getProcessVariable("name"));

    businessProcess.completeTask();
  }

  @Deployment(resources = "org/activiti/cdi/annotation/StartProcessTest.bpmn20.xml")
  public void testStartProcessByName() {

    assertNull(runtimeService.createProcessInstanceQuery().singleResult());

    getBeanInstance(DeclarativeProcessController.class).startProcessByName();

    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    assertNotNull(runtimeService.createProcessInstanceQuery().singleResult());

    assertEquals("Activiti", businessProcess.getProcessVariable("name"));

    businessProcess.completeTask();
  }

}
