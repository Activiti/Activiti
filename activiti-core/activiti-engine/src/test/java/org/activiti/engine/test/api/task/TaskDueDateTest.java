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
package org.activiti.engine.test.api.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;

/**

 */
public class TaskDueDateTest extends PluggableActivitiTestCase {

  @Override
  protected void tearDown() throws Exception {

    for (Task task : taskService.createTaskQuery().list()) {
      taskService.deleteTask(task.getId(), true);
    }

    super.tearDown();
  }

  /**
   * See https://activiti.atlassian.net/browse/ACT-2089
   */
  public void testDueDateSortingWithNulls() {
    Date now = processEngineConfiguration.getClock().getCurrentTime();

    // 4 tasks with a due date
    createTask("task0", new Date(now.getTime() + (4L * 7L * 24L * 60L * 60L * 1000L))); // 4 weeks in future
    createTask("task1", new Date(now.getTime() + (2 * 24L * 60L * 60L * 1000L))); // 2 days in future
    createTask("task2", new Date(now.getTime() + (7L * 24L * 60L * 60L * 1000L))); // 1 week in future
    createTask("task3", new Date(now.getTime() + (24L * 60L * 60L * 1000L))); // 1 day in future

    // 2 tasks without a due date
    createTask("task4", null);
    createTask("task5", null);

    assertThat(taskService.createTaskQuery().count()).isEqualTo(6);

    // Sorting on due date asc should put the nulls at the end
    List<Task> tasks = taskService.createTaskQuery().orderByDueDateNullsLast().asc().list();

    for (int i = 0; i < 4; i++) {
      assertThat(tasks.get(i).getDueDate()).isNotNull();
    }

    assertThat(tasks.get(0).getName()).isEqualTo("task3");
    assertThat(tasks.get(1).getName()).isEqualTo("task1");
    assertThat(tasks.get(2).getName()).isEqualTo("task2");
    assertThat(tasks.get(3).getName()).isEqualTo("task0");
    assertThat(tasks.get(4).getDueDate()).isNull();
    assertThat(tasks.get(5).getDueDate()).isNull();

    // The same, but now desc
    tasks = taskService.createTaskQuery().orderByDueDateNullsLast().desc().list();

    for (int i = 0; i < 4; i++) {
      assertThat(tasks.get(i).getDueDate()).isNotNull();
    }

    assertThat(tasks.get(0).getName()).isEqualTo("task0");
    assertThat(tasks.get(1).getName()).isEqualTo("task2");
    assertThat(tasks.get(2).getName()).isEqualTo("task1");
    assertThat(tasks.get(3).getName()).isEqualTo("task3");
    assertThat(tasks.get(4).getDueDate()).isNull();
    assertThat(tasks.get(5).getDueDate()).isNull();

    // The same but now nulls first
    tasks = taskService.createTaskQuery().orderByDueDateNullsFirst().asc().list();

    assertThat(tasks.get(0).getDueDate()).isNull();
    assertThat(tasks.get(1).getDueDate()).isNull();
    assertThat(tasks.get(2).getName()).isEqualTo("task3");
    assertThat(tasks.get(3).getName()).isEqualTo("task1");
    assertThat(tasks.get(4).getName()).isEqualTo("task2");
    assertThat(tasks.get(5).getName()).isEqualTo("task0");

    // And now desc
    tasks = taskService.createTaskQuery().orderByDueDateNullsFirst().desc().list();

    assertThat(tasks.get(0).getDueDate()).isNull();
    assertThat(tasks.get(1).getDueDate()).isNull();
    assertThat(tasks.get(2).getName()).isEqualTo("task0");
    assertThat(tasks.get(3).getName()).isEqualTo("task2");
    assertThat(tasks.get(4).getName()).isEqualTo("task1");
    assertThat(tasks.get(5).getName()).isEqualTo("task3");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      // And now the same, but for history!
      List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery().orderByDueDateNullsLast().asc().list();

      for (int i = 0; i < 4; i++) {
        assertThat(historicTasks.get(i).getDueDate()).isNotNull();
      }

      assertThat(historicTasks.get(0).getName()).isEqualTo("task3");
      assertThat(historicTasks.get(1).getName()).isEqualTo("task1");
      assertThat(historicTasks.get(2).getName()).isEqualTo("task2");
      assertThat(historicTasks.get(3).getName()).isEqualTo("task0");
      assertThat(historicTasks.get(4).getDueDate()).isNull();
      assertThat(historicTasks.get(5).getDueDate()).isNull();

      // The same, but now desc
      historicTasks = historyService.createHistoricTaskInstanceQuery().orderByDueDateNullsLast().desc().list();

      for (int i = 0; i < 4; i++) {
        assertThat(historicTasks.get(i).getDueDate()).isNotNull();
      }

      assertThat(historicTasks.get(0).getName()).isEqualTo("task0");
      assertThat(historicTasks.get(1).getName()).isEqualTo("task2");
      assertThat(historicTasks.get(2).getName()).isEqualTo("task1");
      assertThat(historicTasks.get(3).getName()).isEqualTo("task3");
      assertThat(historicTasks.get(4).getDueDate()).isNull();
      assertThat(historicTasks.get(5).getDueDate()).isNull();

      // The same but now nulls first
      historicTasks = historyService.createHistoricTaskInstanceQuery().orderByDueDateNullsFirst().asc().list();

      assertThat(historicTasks.get(0).getDueDate()).isNull();
      assertThat(historicTasks.get(1).getDueDate()).isNull();
      assertThat(historicTasks.get(2).getName()).isEqualTo("task3");
      assertThat(historicTasks.get(3).getName()).isEqualTo("task1");
      assertThat(historicTasks.get(4).getName()).isEqualTo("task2");
      assertThat(historicTasks.get(5).getName()).isEqualTo("task0");

      // And now desc
      historicTasks = historyService.createHistoricTaskInstanceQuery().orderByDueDateNullsFirst().desc().list();

      assertThat(historicTasks.get(0).getDueDate()).isNull();
      assertThat(historicTasks.get(1).getDueDate()).isNull();
      assertThat(historicTasks.get(2).getName()).isEqualTo("task0");
      assertThat(historicTasks.get(3).getName()).isEqualTo("task2");
      assertThat(historicTasks.get(4).getName()).isEqualTo("task1");
      assertThat(historicTasks.get(5).getName()).isEqualTo("task3");
    }
  }

  private Task createTask(String name, Date dueDate) {
    Task task = taskService.newTask();
    task.setName(name);
    task.setDueDate(dueDate);
    taskService.saveTask(task);
    return task;
  }

}
