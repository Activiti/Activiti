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

package org.activiti.engine.test.bpmn.servicetask;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;


/**
 *
 * @author Daniel Meyer
 */
public abstract class ServiceTaskVariablesTest extends PluggableActivitiTestCase {
  
  static boolean isNullInDelegate2;
  static boolean isNullInDelegate3;
  
  public static class Variable implements Serializable {
    private static final long serialVersionUID = 1L;
    public String value;    
  }
  
  public static class Delegate1 implements JavaDelegate {

    public void execute(DelegateExecution execution) throws Exception {
      Variable v = new Variable();
      execution.setVariable("variable", v);
      v.value = "test";
    }
    
  }
  
  public static class Delegate2 implements JavaDelegate {

    public void execute(DelegateExecution execution) throws Exception {
      Variable v = (Variable) execution.getVariable("variable");
      synchronized (ServiceTaskVariablesTest.class) {
        // we expect this to be 'true'
        isNullInDelegate2 = (v.value == null);        
      }
      v.value = "test";      
    }
    
  }
  
  public static class Delegate3 implements JavaDelegate {

    public void execute(DelegateExecution execution) throws Exception {
      Variable v = (Variable) execution.getVariable("variable");
      synchronized (ServiceTaskVariablesTest.class) {
        // we expect this to be 'true' as well
        isNullInDelegate3 = (v.value == null);
      }
    }
    
  }
  
  @Deployment
  public void FAILING_testSerializedVariablesBothAsync() {
    
    // in this test, there is an async cont. both before the second and the
    // third service task in the sequence
    
    // this test demonstrates, that the new value set through 
    //    v.value = "test";      
    // in Delegate2 is seraialized even though I do not call 
    //    execution.setVariable("variable", v);
    // in the second Delegate
    
    runtimeService.startProcessInstanceByKey("process");
    waitForJobExecutorToProcessAllJobs(1000, 500);
    
    synchronized (ServiceTaskVariablesTest.class) {
      assertTrue(isNullInDelegate2); // this passes -> v.value is null in second service task (this is expected)
      assertTrue(isNullInDelegate3); // this fails -> updated even though setVariable(...) was not called
    }
  }

  @Deployment
  public void FAILING_testSerializedVariablesThirdAsync() {
    
    // in this test, only the third service task is async
    
    // this test demonstrates that v.value is not null in the second service task
    // (since the value set in the first service task is cached in memory)
    // ( -> this is not expected)
    
    // but it is null in the third -> new command 
    // ( -> this is expected)
        
    runtimeService.startProcessInstanceByKey("process");
    waitForJobExecutorToProcessAllJobs(1000, 500);
    
    synchronized (ServiceTaskVariablesTest.class) {
      assertTrue(isNullInDelegate3); // this passes -> v.value is null in the third service task
      assertTrue(isNullInDelegate2); // this fails 
    }
    
  }

}

