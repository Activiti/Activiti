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

package org.activiti.pvm.test;

import java.util.ArrayList;
import java.util.List;

import org.activiti.pvm.ProcessDefinitionBuilder;
import org.activiti.pvm.process.PvmProcessDefinition;
import org.activiti.pvm.runtime.PvmProcessInstance;
import org.activiti.test.pvm.activities.Automatic;
import org.activiti.test.pvm.activities.ParallelGateway;
import org.activiti.test.pvm.activities.WaitState;


/**
 * @author Tom Baeyens
 */
public class PvmParallelScopeTest extends PvmTestCase {

  public void testConcurrentPathsComingOutOfScope() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork")
      .endActivity()
      .createActivity("scope")
        .scope()
        .createActivity("fork")
          .behavior(new ParallelGateway())
          .transition("c1")
          .transition("c2")
          .transition("c3")
        .endActivity()
      .endActivity()
      .createActivity("c1")
        .behavior(new WaitState())
      .endActivity()
      .createActivity("c2")
        .behavior(new WaitState())
      .endActivity()
      .createActivity("c3")
        .behavior(new WaitState())
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    List<String> activeActivityIds = processInstance.findActiveActivityIds();
    List<String> expectedActiveActivityIds = new ArrayList<String>();
    expectedActiveActivityIds.add("c1");
    expectedActiveActivityIds.add("c2");
    expectedActiveActivityIds.add("c3");
    
    assertEquals(expectedActiveActivityIds, activeActivityIds);
  }

  public void testConcurrentPathsGoingIntoScope() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("parallel")
      .endActivity()
      .createActivity("parallel")
        .behavior(new ParallelGateway())
        .transition("inside")
        .transition("inside")
        .transition("inside")
      .endActivity()
      .createActivity("scope")
        .scope()
        .createActivity("inside")
          .behavior(new WaitState())
        .endActivity()
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    List<String> activeActivityIds = processInstance.findActiveActivityIds();
    List<String> expectedActiveActivityIds = new ArrayList<String>();
    expectedActiveActivityIds.add("inside");
    expectedActiveActivityIds.add("inside");
    expectedActiveActivityIds.add("inside");
    
    assertEquals(expectedActiveActivityIds, activeActivityIds);
  }
}
