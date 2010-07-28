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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.activiti.engine.ProcessDefinition;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeployer;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionsTest {

  private static final String NAMESPACE = "xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL'";

  private static final String TARGET_NAMESPACE = "targetNamespace='http://activiti.org/BPMN20'";

  @Rule
  public LogInitializer logSetup = new LogInitializer();

  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Test
  public void testGetProcessDefinitions() {
    deployer.deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report 1' />" + "</definitions>"));
    deployer.deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report 2' />" + "</definitions>"));
    deployer.deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report 3' />" + "</definitions>"));
    deployer.deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='EN' name='Expense Note 1' />" + "</definitions>"));
    deployer.deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='EN' name='Expense Note 2' />" + "</definitions>"));

    List<ProcessDefinition> processDefinitions = deployer.getProcessService().findProcessDefinitions();
    assertNotNull(processDefinitions);

    assertEquals(5, processDefinitions.size());

    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertEquals("EN", processDefinition.getKey());
    assertEquals("Expense Note 2", processDefinition.getName());
    assertEquals("EN:2", processDefinition.getId());
    assertEquals(2, processDefinition.getVersion());

    processDefinition = processDefinitions.get(1);
    assertEquals("EN", processDefinition.getKey());
    assertEquals("Expense Note 1", processDefinition.getName());
    assertEquals("EN:1", processDefinition.getId());
    assertEquals(1, processDefinition.getVersion());

    processDefinition = processDefinitions.get(2);
    assertEquals("IDR", processDefinition.getKey());
    assertEquals("Insurance Damage Report 3", processDefinition.getName());
    assertEquals("IDR:3", processDefinition.getId());
    assertEquals(3, processDefinition.getVersion());

    processDefinition = processDefinitions.get(3);
    assertEquals("IDR", processDefinition.getKey());
    assertEquals("Insurance Damage Report 2", processDefinition.getName());
    assertEquals("IDR:2", processDefinition.getId());
    assertEquals(2, processDefinition.getVersion());

    processDefinition = processDefinitions.get(4);
    assertEquals("IDR", processDefinition.getKey());
    assertEquals("Insurance Damage Report 1", processDefinition.getName());
    assertEquals("IDR:1", processDefinition.getId());
    assertEquals(1, processDefinition.getVersion());

  }

  @Test
  public void testDeployIdenticalProcessDefinitions() {
    deployer.deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report' />" + "</definitions>"));
    deployer.deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report' />" + "</definitions>"));

    List<ProcessDefinition> processDefinitions = deployer.getProcessService().findProcessDefinitions();
    assertNotNull(processDefinitions);
    assertEquals(1, processDefinitions.size());

    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertEquals("IDR", processDefinition.getKey());
    assertEquals("Insurance Damage Report", processDefinition.getName());
    assertEquals("IDR:1", processDefinition.getId());
    assertEquals(1, processDefinition.getVersion());

  }
}
