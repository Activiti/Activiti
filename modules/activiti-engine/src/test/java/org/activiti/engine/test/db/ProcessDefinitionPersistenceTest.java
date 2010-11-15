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

package org.activiti.engine.test.db;

import java.util.Collections;
import java.util.List;

import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.repository.ProcessDefinition;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionPersistenceTest extends ActivitiInternalTestCase {

  public void testProcessDefinitionPersistence() {
    String deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource("org/activiti/engine/test/db/processOne.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/db/processTwo.bpmn20.xml")
      .deploy()
      .getId();
  
    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .list();
    
    assertEquals(2, processDefinitions.size());
    
    repositoryService.deleteDeployment(deploymentId);
  }

  public void testProcessDefinitionIntrospection() {
    String deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource("org/activiti/engine/test/db/processOne.bpmn20.xml")
      .deploy()
      .getId();
  
    ReadOnlyProcessDefinition processDefinition = ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition("processOne:1");
    
    assertEquals("processOne:1", processDefinition.getId());
    assertEquals("Process One", processDefinition.getName());
    assertEquals("the first process", processDefinition.getProperty("documentation"));
    
    PvmActivity start = processDefinition.findActivity("start");
    assertNotNull(start);
    assertEquals("start", start.getId());
    assertEquals("S t a r t", start.getProperty("name"));
    assertEquals("the start event", start.getProperty("documentation"));
    assertEquals(Collections.EMPTY_LIST, start.getActivities());
    List<PvmTransition> outgoingTransitions = start.getOutgoingTransitions();
    assertEquals(1, outgoingTransitions.size());
    assertEquals("${a == b}", outgoingTransitions.get(0).getProperty(BpmnParse.PROPERTYNAME_CONDITION_TEXT));

    PvmActivity end = processDefinition.findActivity("end");
    assertNotNull(end);
    assertEquals("end", end.getId());
    
    PvmTransition transition = outgoingTransitions.get(0);
    assertEquals("flow1", transition.getId());
    assertEquals("Flow One", transition.getProperty("name"));
    assertEquals("The only transitions in the process", transition.getProperty("documentation"));
    assertSame(start, transition.getSource());
    assertSame(end, transition.getDestination());
    
    repositoryService.deleteDeployment(deploymentId);
  }
}
