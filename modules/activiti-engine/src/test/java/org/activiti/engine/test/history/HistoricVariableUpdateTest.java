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

package org.activiti.engine.test.history;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.history.HistoricVariableUpdateQueryProperty;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.test.Deployment;


/**
 * @author Tom Baeyens
 */
public class HistoricVariableUpdateTest extends ActivitiInternalTestCase {

  @Deployment
  public void testHistoricVariableUpdates() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("startFormParamA", "one");
    variables.put("startFormParamB", new Long(234));
    variables.put("startFormParamC", new SerializableVariable("contents"));
    runtimeService.startProcessInstanceByKey("HistoricVariableUpdateProcess", variables);
    
    List<HistoricVariableUpdate> historicVariableUpdates = historyService
      .createHistoricVariableUpdateQuery()
      .orderBy(HistoricVariableUpdateQueryProperty.INDEX).asc()
      .list();
    
//    HistoricVariableUpdate historicVariableUpdate = historicVariableUpdates.get(0);
//    assertEquals(expected, historicVariableUpdate.getProcessInstanceId());
//    assertEquals(expected, historicVariableUpdate.getExecutionId());
//    assertEquals(expected, historicVariableUpdate.getIndex());
//    assertEquals(expected, historicVariableUpdate.getTime());
//    assertEquals(expected, historicVariableUpdate.getVariableName());
//    assertEquals(expected, historicVariableUpdate.getVariableType());
//    assertEquals(expected, historicVariableUpdate.getValue());
    
    System.out.println(historicVariableUpdates);
  }
}
