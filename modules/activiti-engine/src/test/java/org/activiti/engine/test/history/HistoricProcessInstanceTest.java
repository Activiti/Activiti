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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.ProcessEngineTestCase;


/**
 * @author Tom Baeyens
 */
public class HistoricProcessInstanceTest extends ProcessEngineTestCase {
  
  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricDataCreatedForProcessExecution() {

    Calendar calendar = new GregorianCalendar();
    calendar.set(Calendar.YEAR, 2010);
    calendar.set(Calendar.MONTH, 8);
    calendar.set(Calendar.DAY_OF_MONTH, 30);
    calendar.set(Calendar.HOUR_OF_DAY, 12);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date noon = calendar.getTime();
    
    ClockUtil.setCurrentTime(noon);
    final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    HistoricProcessInstance historicProcessInstance = historicDataService.findHistoricProcessInstanceById(processInstance.getId());

    assertNotNull(historicProcessInstance);
    assertEquals(processInstance.getId(), historicProcessInstance.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), historicProcessInstance.getProcessDefinitionId());
    assertEquals(noon, historicProcessInstance.getStartTime());
    assertNull(historicProcessInstance.getEndTime());
    assertNull(historicProcessInstance.getDurationInMillis());

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

    assertEquals(1, tasks.size());

    // in this test scenario we assume that 25 seconds after the process start, the 
    // user completes the task (yes! he must be almost as fast as me)
    Date twentyFiveSecsAfterNoon = new Date(noon.getTime() + 25*1000);
    ClockUtil.setCurrentTime(twentyFiveSecsAfterNoon);
    taskService.complete(tasks.get(0).getId());

    historicProcessInstance = historicDataService.findHistoricProcessInstanceById(processInstance.getId());

    assertNotNull(historicProcessInstance);
    assertEquals(processInstance.getId(), historicProcessInstance.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), historicProcessInstance.getProcessDefinitionId());
    assertEquals(noon, historicProcessInstance.getStartTime());
    assertEquals(twentyFiveSecsAfterNoon, historicProcessInstance.getEndTime());
    assertEquals(new Long(25*1000), historicProcessInstance.getDurationInMillis());
  }

  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceSorting() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderAsc(HistoricProcessInstanceQuery.PROPERTY_ID).list().size());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderAsc(HistoricProcessInstanceQuery.PROPERTY_START).list().size());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderAsc(HistoricProcessInstanceQuery.PROPERTY_END).list().size());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderAsc(HistoricProcessInstanceQuery.PROPERTY_DURATION).list().size());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderAsc(HistoricProcessInstanceQuery.PROPERTY_PROCESS_DEFINITION_ID).list().size());

    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderDesc(HistoricProcessInstanceQuery.PROPERTY_ID).list().size());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderDesc(HistoricProcessInstanceQuery.PROPERTY_START).list().size());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderDesc(HistoricProcessInstanceQuery.PROPERTY_END).list().size());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderDesc(HistoricProcessInstanceQuery.PROPERTY_DURATION).list().size());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderDesc(HistoricProcessInstanceQuery.PROPERTY_PROCESS_DEFINITION_ID).list().size());

    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderAsc(HistoricProcessInstanceQuery.PROPERTY_ID).count());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderAsc(HistoricProcessInstanceQuery.PROPERTY_START).count());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderAsc(HistoricProcessInstanceQuery.PROPERTY_END).count());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderAsc(HistoricProcessInstanceQuery.PROPERTY_DURATION).count());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderAsc(HistoricProcessInstanceQuery.PROPERTY_PROCESS_DEFINITION_ID).count());

    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderDesc(HistoricProcessInstanceQuery.PROPERTY_ID).count());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderDesc(HistoricProcessInstanceQuery.PROPERTY_START).count());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderDesc(HistoricProcessInstanceQuery.PROPERTY_END).count());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderDesc(HistoricProcessInstanceQuery.PROPERTY_DURATION).count());
    assertEquals(1, historicDataService.createHistoricProcessInstanceQuery().orderDesc(HistoricProcessInstanceQuery.PROPERTY_PROCESS_DEFINITION_ID).count());
  }
}
