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
package org.activiti.engine.test.api.variables;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 */
public class TransientVariablesTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testSetTransientVariableInServiceTask() {
    
    // Process has two service task: first sets transient vars,
    // second then processes transient var and puts data in real vars.
    // (mimicing a service + processing call)
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transientVarsTest");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    String message = (String) taskService.getVariable(task.getId(), "message");
    assertEquals("Hello World!", message);
  }
  
  /* Service task class for previous test */
  
  /**
   * Mimics a service task that fetches data from a server and stored the whole thing
   * in a transient variable.  
   */
  public static class FetchDataServiceTask implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      execution.setTransientVariable("response", "author=kermit;version=3;message=Hello World");
    }
  }
  
  /**
   * Processes the transient variable and puts the relevant bits in real variables
   */
  public static class ServiceTask02 implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      String response = (String) execution.getTransientVariable("response");
      for (String s : response.split(";")) {
        String[] data = s.split("=");
        if (data[0].equals("message")) {
          execution.setVariable("message", data[1] + "!");
        }
      }
    }
  }

}
