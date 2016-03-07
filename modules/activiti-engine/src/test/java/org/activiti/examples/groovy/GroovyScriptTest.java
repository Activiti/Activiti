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

package org.activiti.examples.groovy;

import java.util.Date;
import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * @author Tom Baeyens
 */
public class GroovyScriptTest extends PluggableActivitiTestCase {

  @Deployment
  public void testScriptExecution() {
    int[] inputArray = new int[] { 1, 2, 3, 4, 5 };
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("scriptExecution", CollectionUtil.singletonMap("inputArray", inputArray));

    Integer result = (Integer) runtimeService.getVariable(pi.getId(), "sum");
    assertEquals(15, result.intValue());
  }

  @Deployment
  public void testSetVariableThroughExecutionInScript() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setScriptVariableThroughExecution");

    // Since 'def' is used, the 'scriptVar' will be script local
    // and not automatically stored as a process variable.
    assertNull(runtimeService.getVariable(pi.getId(), "scriptVar"));
    assertEquals("test123", runtimeService.getVariable(pi.getId(), "myVar"));
  }
  
  @Deployment
  public void testAsyncScript() {
    // Set the clock fixed
    Date startTime = new Date();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testAsyncScript");
    
    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(processInstance.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());
    
    // After setting the clock to time '1 hour and 5 seconds', the second timer should fire
    waitForJobExecutorToProcessAllJobs(5000L, 100L);
    assertEquals(0L, jobQuery.count());
    
    assertProcessEnded(processInstance.getId());
  } 
}
