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
package org.activiti.examples.processdefinitions;

import java.util.List;

import org.activiti.ProcessDefinition;
import org.activiti.test.ActivitiTestCase;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionsTest extends ActivitiTestCase {

  public void testGetProcessDefinitions() {
    deployProcessString(
      "<definitions xmlns='http://schema.omg.org/spec/BPMN/2.0' " +
      "             targetNamespace='http://www.activiti.org/bpmn2.0' >" +
      "  <process id='IDR' name='Insurance Damage Report' />" +
      "</definitions>"
    );
    deployProcessString(
      "<definitions xmlns='http://schema.omg.org/spec/BPMN/2.0' " +
      "             targetNamespace='http://www.activiti.org/bpmn2.0' >" +
      "  <process id='IDR' name='Insurance Damage Report' />" +
      "</definitions>"
    );
    deployProcessString(
      "<definitions xmlns='http://schema.omg.org/spec/BPMN/2.0' " +
      "             targetNamespace='http://www.activiti.org/bpmn2.0' >" +
      "  <process id='IDR' name='Insurance Damage Report' />" +
      "</definitions>"
    );
    deployProcessString(
      "<definitions xmlns='http://schema.omg.org/spec/BPMN/2.0' " +
      "             targetNamespace='http://www.activiti.org/bpmn2.0' >" +
      "  <process id='EN' name='Expense Note' />" +
      "</definitions>"
    );
    deployProcessString(
      "<definitions xmlns='http://schema.omg.org/spec/BPMN/2.0' " +
      "             targetNamespace='http://www.activiti.org/bpmn2.0' >" +
      "  <process id='EN' name='Expense Note' />" +
      "</definitions>"
    );
    
    List<ProcessDefinition> processDefinitions = processService.findProcessDefinitions();
    assertNotNull(processDefinitions);
    
    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertEquals("EN", processDefinition.getKey());
    assertEquals("Expense Note", processDefinition.getName());
    assertEquals("EN:2", processDefinition.getId());
    assertEquals(2, processDefinition.getVersion());

    processDefinition = processDefinitions.get(1);
    assertEquals("EN", processDefinition.getKey());
    assertEquals("Expense Note", processDefinition.getName());
    assertEquals("EN:1", processDefinition.getId());
    assertEquals(1, processDefinition.getVersion());
    
    processDefinition = processDefinitions.get(2);
    assertEquals("IDR", processDefinition.getKey());
    assertEquals("Insurance Damage Report", processDefinition.getName());
    assertEquals("IDR:3", processDefinition.getId());
    assertEquals(3, processDefinition.getVersion());

    processDefinition = processDefinitions.get(3);
    assertEquals("IDR", processDefinition.getKey());
    assertEquals("Insurance Damage Report", processDefinition.getName());
    assertEquals("IDR:2", processDefinition.getId());
    assertEquals(2, processDefinition.getVersion());

    processDefinition = processDefinitions.get(4);
    assertEquals("IDR", processDefinition.getKey());
    assertEquals("Insurance Damage Report", processDefinition.getName());
    assertEquals("IDR:1", processDefinition.getId());
    assertEquals(1, processDefinition.getVersion());
    
    assertEquals(5, processDefinitions.size());
  }
}
