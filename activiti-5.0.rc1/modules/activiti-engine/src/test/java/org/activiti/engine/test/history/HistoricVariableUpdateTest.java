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

import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.test.Deployment;


/**
 * @author Tom Baeyens
 */
public class HistoricVariableUpdateTest extends ActivitiInternalTestCase {

  @Deployment
  public void testHistoricVariableUpdates() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("startFormParamA", "one");
    properties.put("startFormParamB", "234");
    
    String processInstanceId = formService.submitStartFormData("HistoricVariableUpdateProcess:1", properties).getId();

    List<HistoricDetail> historicDetails = historyService
      .createHistoricDetailQuery()
      .orderByVariableName().asc()
      .list();
    
    System.out.println(historicDetails);

    HistoricFormProperty historicFormProperty = (HistoricFormProperty) historicDetails.get(0);
    assertEquals(processInstanceId, historicFormProperty.getProcessInstanceId());
    assertEquals(processInstanceId, historicFormProperty.getExecutionId());
    assertNotNull(historicFormProperty.getTime());
    assertEquals("startFormParamA", historicFormProperty.getPropertyId());
    assertEquals("one", historicFormProperty.getPropertyValue());

    historicFormProperty = (HistoricFormProperty) historicDetails.get(1);
    assertEquals(processInstanceId, historicFormProperty.getProcessInstanceId());
    assertEquals(processInstanceId, historicFormProperty.getExecutionId());
    assertNotNull(historicFormProperty.getTime());
    assertEquals("startFormParamB", historicFormProperty.getPropertyId());
    assertEquals("234", historicFormProperty.getPropertyValue());
  }
}
