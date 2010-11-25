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

package org.activiti.standalone.history;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;


/**
 * @author Tom Baeyens
 */
public class FullHistoryTest extends ResourceActivitiTestCase {

  public FullHistoryTest() {
    super("org/activiti/standalone/history/fullhistory.activiti.cfg.xml");
  }

  @Deployment
  public void testVariableUpdates() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("number", "one");
    variables.put("character", "a");
    variables.put("bytes", ":-(".getBytes());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("receiveTask", variables);
    runtimeService.setVariable(processInstance.getId(), "number", "two");
    runtimeService.setVariable(processInstance.getId(), "bytes", ":-)".getBytes());
    
    List<HistoricDetail> historicDetails = historyService
      .createHistoricDetailQuery()
      .orderByVariableName().asc()
      .orderByVariableRevision().asc()
      .list();
    
    HistoricVariableUpdate historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(0);
    assertEquals("bytes", historicVariableUpdate.getVariableName());
    assertEquals(":-(", new String((byte[])historicVariableUpdate.getValue()));
    assertEquals(0, historicVariableUpdate.getRevision());
    
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(1);
    assertEquals("bytes", historicVariableUpdate.getVariableName());
    assertEquals(":-)", new String((byte[])historicVariableUpdate.getValue()));
    assertEquals(1, historicVariableUpdate.getRevision());
    
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(2);
    assertEquals("character", historicVariableUpdate.getVariableName());
    assertEquals("a", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(3);
    assertEquals("number", historicVariableUpdate.getVariableName());
    assertEquals("one", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(4);
    assertEquals("number", historicVariableUpdate.getVariableName());
    assertEquals("two", historicVariableUpdate.getValue());
    assertEquals(1, historicVariableUpdate.getRevision());
  }
}
