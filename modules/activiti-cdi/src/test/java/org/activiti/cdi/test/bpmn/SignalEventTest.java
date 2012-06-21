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
package org.activiti.cdi.test.bpmn;

import java.util.HashMap;

import junit.framework.Assert;

import org.activiti.cdi.test.CdiActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.junit.Test;

public class SignalEventTest extends CdiActivitiTestCase {

  @Test
  @Deployment(resources = {"org/activiti/cdi/test/bpmn/SignalEventTests.catchAlertSignalBoundaryWithReceiveTask.bpmn20.xml", 
                          "org/activiti/cdi/test/bpmn/SignalEventTests.throwAlertSignalWithDelegate.bpmn20.xml"})
  public void testSignalCatchBoundaryWithVariables() {
    HashMap<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("processName", "catchSignal");
    ProcessInstance pi1 = runtimeService.startProcessInstanceByKey("catchSignal", variables1);
        
    HashMap<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("processName", "throwSignal");
    ProcessInstance pi2 = runtimeService.startProcessInstanceByKey("throwSignal", variables2);
    
    Assert.assertEquals("catchSignal", runtimeService.getVariable(pi1.getId(), "processName"));
    Assert.assertEquals("throwSignal", runtimeService.getVariable(pi2.getId(), "processName"));
  }

}
