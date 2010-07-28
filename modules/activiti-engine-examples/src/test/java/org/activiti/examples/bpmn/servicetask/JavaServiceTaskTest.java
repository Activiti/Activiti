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
package org.activiti.examples.bpmn.servicetask;

import static org.junit.Assert.assertEquals;

import org.activiti.engine.Execution;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.ProcessService;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeclared;
import org.activiti.test.ProcessDeployer;
import org.activiti.util.CollectionUtil;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class JavaServiceTaskTest {

  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Test
  @ProcessDeclared
  public void testJavaServiceDelegation() {
    ProcessService processService = deployer.getProcessService();
    ProcessInstance pi = processService.startProcessInstanceByKey("javaServiceDelegation", 
            CollectionUtil.singletonMap("input", "Activiti BPM Engine"));
    Execution execution = processService.findExecutionInActivity(pi.getId(), "waitState");
    assertEquals("ACTIVITI BPM ENGINE", processService.getVariable(execution.getId(), "input"));
  }

}
