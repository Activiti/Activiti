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

package org.activiti.engine.test.bpmn.usertask;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.joda.time.Period;


/**
 * @author Frederik Heremans
 */
public class TaskDueDateExtensionsTest extends ResourceActivitiTestCase {

  public TaskDueDateExtensionsTest() {
    super("org/activiti/engine/test/bpmn/usertask/TaskDueDateExtensionsTest.activiti.cfg.xml");
  }

  @Deployment
  public void testDueDateExtension() throws Exception {
    
    Date date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse("06-07-1986 12:10:00");
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("dateVariable", date);
    
    // Start process-instance, passing date that should be used as dueDate
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dueDateExtension", variables);
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    assertNotNull(task.getDueDate());
    assertEquals(date, task.getDueDate());
  }
  
  @Deployment
  public void testDueDateStringExtension() throws Exception {
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("dateVariable", "1986-07-06T12:10:00");
    
    // Start process-instance, passing date that should be used as dueDate
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dueDateExtension", variables);
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    assertNotNull(task.getDueDate());
    Date date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse("06-07-1986 12:10:00");
    assertEquals(date, task.getDueDate());
  }
  
  @Deployment
  public void testRelativeDueDateStringExtension() throws Exception {
    Clock clock = processEngineConfiguration.getClock();
    clock.setCurrentCalendar(new GregorianCalendar(2015, 0, 1));
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("dateVariable", "P2DT5H40M");
    
    // Start process-instance, passing ISO8601 duration formatted String that should be used to calculate dueDate
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dueDateExtension", variables);
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    assertNotNull(task.getDueDate());
    Period period = new Period(task.getCreateTime().getTime(), task.getDueDate().getTime());
    assertEquals(2, period.getDays());
    assertEquals(5, period.getHours());
    assertEquals(40, period.getMinutes());
    clock.reset();
  }

  @Deployment
  public void testRelativeDueDateStringWithCalendarNameExtension() throws Exception {

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("dateVariable", "P2DT5H40M");

    // Start process-instance, passing ISO8601 duration formatted String that should be used to calculate dueDate
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dueDateExtension", variables);

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull(task.getDueDate());
    assertThat(task.getDueDate(), is(new Date(0)));
  }

  public static class CustomBusinessCalendar implements BusinessCalendar {

    @Override
    public Date resolveDuedate(String duedateDescription) {
      return new Date(0);
    }

    @Override
    public Date resolveDuedate(String duedateDescription, int maxIterations) {
      return new Date(0);
    }

    @Override
    public Boolean validateDuedate(String duedateDescription, int maxIterations, Date endDate, Date newTimer) {
      return true;
    }

    @Override
    public Date resolveEndDate(String endDateString) {
      return new Date(0);
    }

  }

}
