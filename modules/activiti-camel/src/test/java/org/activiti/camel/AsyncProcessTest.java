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

package org.activiti.camel;

/**
 * @author Saeid Mirzaei  
 */

import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:camel-activiti-context.xml")
public class AsyncProcessTest extends SpringActivitiTestCase {

  @Autowired
  RuntimeService runtimeService;

  @Deployment(resources = {"process/async.bpmn20.xml"})
  public void testRunProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncCamelProcess");
    List<Execution> executionList = runtimeService.createExecutionQuery().list();
    assertEquals(3, executionList.size());
    Thread.sleep(4000);
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
  }
}
