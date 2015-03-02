package org.activiti.engine.test.bpmn.event.timer;

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

import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.*;

public class BoundaryTimerEvent_RepeatWithEnd extends PluggableActivitiTestCase {

  @Deployment
  public void testRepeatWithEnd() throws Throwable {
    AsyncExecutor asyncExecutor = processEngineConfiguration.getAsyncExecutor();
    asyncExecutor.start();

    System.out.println("Starting test at " + new Date());

    int minutes = 0;
    int seconds = 20;

    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    c.add(Calendar.MINUTE, minutes);
    c.add(Calendar.SECOND, seconds);
    DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
    DateTime dt = new DateTime(c.getTime());
    String dateStr = fmt.print(dt);
    System.out.println("Setting end date For Boundary : " + dateStr);
    System.out.println("Setting end date For Catch : " + dateStr);


    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("repeatWithEnd");

  
    
    System.out.println("Setting end date process variable: " + dateStr);
    runtimeService.setVariable(processInstance.getId(), "EndDateForBoundary", dateStr);
    runtimeService.setVariable(processInstance.getId(), "EndDateForCatch", dateStr);

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());

    Task task = tasks.get(0);
    assertEquals("Task A", task.getName());

    // complete will cause timer to be created
    taskService.complete(task.getId());

    Thread.sleep(10*1000);

    tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());

    task = tasks.get(0);
    assertEquals("Task B", task.getName());
    taskService.complete(task.getId());


    Thread.sleep(((minutes+1)*60+seconds)*1000);

    DateTime dt1 = new DateTime(Calendar.getInstance());
    String dateStr1 = fmt.print(dt1);
    System.out.println("Thread Execution finished @: " + dateStr1);
  }
}
