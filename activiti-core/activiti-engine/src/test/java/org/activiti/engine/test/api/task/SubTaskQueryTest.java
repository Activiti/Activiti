/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.api.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;

/**
 * Tests for cub-tasks querying
 * @see TaskQueryTest
 */
public class SubTaskQueryTest extends PluggableActivitiTestCase {

    private List<String> taskIds;

    private static final String KERMIT = "kermit";

    private static final String GONZO = "gonzo";

    public void setUp() throws Exception {

        taskIds = generateTestSubTasks();
    }

    public void tearDown() throws Exception {

        taskService.deleteTasks(taskIds,
                                true);
    }

    /**
     * test for task inclusion/exclusion (no other filters, no sort)
     */
    public void testQueryExcludeSubtasks() throws Exception {
        // query all tasks, including subtasks
        TaskQuery query = taskService.createTaskQuery();
        assertThat(query.count()).isEqualTo(10);
        assertThat(query.list()).hasSize(10);
        // query only parent tasks (exclude subtasks)
        query = taskService.createTaskQuery().excludeSubtasks();
        assertThat(query.count()).isEqualTo(3);
        assertThat(query.list()).hasSize(3);
    }

    /**
     * test for task inclusion/exclusion (no other filters, no sort)
     */
    public void testQueryWithPagination() throws Exception {
        // query all tasks, including subtasks
        TaskQuery query = taskService.createTaskQuery();
        assertThat(query.count()).isEqualTo(10);
        assertThat(query.listPage(0,2)).hasSize(2);
        // query only parent tasks (exclude subtasks)
        query = taskService.createTaskQuery().excludeSubtasks();
        assertThat(query.count()).isEqualTo(3);
        assertThat(query.listPage(0,1)).hasSize(1);
    }

    /**
     * test for task inclusion/exclusion (no other filters, order by task assignee )
     */
    public void testQueryExcludeSubtasksSorted() throws Exception {
        // query all tasks, including subtasks
        TaskQuery query = taskService.createTaskQuery().orderByTaskAssignee().asc();
        assertThat(query.count()).isEqualTo(10);
        assertThat(query.list()).hasSize(10);
        // query only parent tasks (exclude subtasks)
        query = taskService.createTaskQuery().excludeSubtasks().orderByTaskAssignee().desc();
        assertThat(query.count()).isEqualTo(3);
        assertThat(query.list()).hasSize(3);
    }

    /**
     * test for task inclusion/exclusion when additional filter is specified (like assignee), no order.
     */
    public void testQueryByAssigneeExcludeSubtasks() throws Exception {
        // gonzo has 2 root tasks and 3+2 subtasks assigned
        // include subtasks
        TaskQuery query = taskService.createTaskQuery().taskAssignee("gonzo");
        assertThat(query.count()).isEqualTo(7);
        assertThat(query.list()).hasSize(7);
        // exclude subtasks
        query = taskService.createTaskQuery().taskAssignee("gonzo").excludeSubtasks();
        assertThat(query.count()).isEqualTo(2);
        assertThat(query.list()).hasSize(2);

        // kermit has no root tasks and no subtasks assigned
        // include subtasks
        query = taskService.createTaskQuery().taskAssignee(KERMIT);
        assertThat(query.count()).isEqualTo(0);
        assertThat(query.list()).hasSize(0);
        assertThat(query.singleResult()).isNull();
        // exclude subtasks
        query = taskService.createTaskQuery().taskAssignee(KERMIT).excludeSubtasks();
        assertThat(query.count()).isEqualTo(0);
        assertThat(query.list()).hasSize(0);
        assertThat(query.singleResult()).isNull();
    }

    /**
     * test for task inclusion/exclusion when additional filter is specified (like assignee), no order.
     */
    public void testQueryByAssigneeExcludeSubtasksPaginated() throws Exception {
        // gonzo has 2 root tasks and 3+2 subtasks assigned
        // include subtasks
        TaskQuery query = taskService.createTaskQuery().taskAssignee("gonzo");
        assertThat(query.count()).isEqualTo(7);
        assertThat(query.listPage(0,2)).hasSize(2);
        // exclude subtasks
        query = taskService.createTaskQuery().taskAssignee("gonzo").excludeSubtasks();
        assertThat(query.count()).isEqualTo(2);
        assertThat(query.listPage(0,1)).hasSize(1);

        // kermit has no root tasks and no subtasks assigned
        // include subtasks
        query = taskService.createTaskQuery().taskAssignee(KERMIT);
        assertThat(query.count()).isEqualTo(0);
        assertThat(query.listPage(0,2)).hasSize(0);
        assertThat(query.singleResult()).isNull();
        // exclude subtasks
        query = taskService.createTaskQuery().taskAssignee(KERMIT).excludeSubtasks();
        assertThat(query.count()).isEqualTo(0);
        assertThat(query.listPage(0,2)).hasSize(0);
        assertThat(query.singleResult()).isNull();
    }

    /**
     * test for task inclusion/exclusion when additional filter is specified (like assignee), ordered.
     */
    public void testQueryByAssigneeExcludeSubtasksOrdered() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

        // gonzo has 2 root tasks and 3+2 subtasks assigned
        // include subtasks
        TaskQuery query = taskService.createTaskQuery().taskAssignee("gonzo").orderByTaskCreateTime().desc();
        assertThat(query.count()).isEqualTo(7);
        assertThat(query.list()).hasSize(7);
        assertThat(query.list().get(0).getCreateTime()).isEqualTo(sdf.parse("02/01/2009 01:01:01.000"));

        // exclude subtasks
        query = taskService.createTaskQuery().taskAssignee("gonzo").excludeSubtasks().orderByTaskCreateTime().asc();
        assertThat(query.count()).isEqualTo(2);
        assertThat(query.list()).hasSize(2);
        assertThat(query.list().get(0).getCreateTime()).isEqualTo(sdf.parse("01/02/2008 02:02:02.000"));
        assertThat(query.list().get(1).getCreateTime()).isEqualTo(sdf.parse("05/02/2008 02:02:02.000"));

        // kermit has no root tasks and no subtasks assigned include subtasks
        query = taskService.createTaskQuery().taskAssignee(KERMIT).orderByTaskCreateTime().asc();
        assertThat(query.count()).isEqualTo(0);
        assertThat(query.list()).hasSize(0);
        assertThat(query.singleResult()).isNull();
        // exclude subtasks
        query = taskService.createTaskQuery().taskAssignee(KERMIT).excludeSubtasks().orderByTaskCreateTime().desc();
        assertThat(query.count()).isEqualTo(0);
        assertThat(query.list()).hasSize(0);
        assertThat(query.singleResult()).isNull();
    }

    /**
     * test for task inclusion/exclusion when additional filter is specified (like assignee), ordered.
     */
    public void testQueryByAssigneeExcludeSubtasksOrderedAndPaginated() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

        // gonzo has 2 root tasks and 3+2 subtasks assigned
        // include subtasks
        TaskQuery query = taskService.createTaskQuery().taskAssignee("gonzo").orderByTaskCreateTime().asc();
        assertThat(query.count()).isEqualTo(7);
        assertThat(query.listPage(0, 1)).hasSize(1);
        assertThat(query.listPage(0,1).get(0).getCreateTime()).isEqualTo(sdf.parse("01/02/2008 02:02:02.000"));
        assertThat(query.listPage(1, 1)).hasSize(1);
        assertThat(query.listPage(1,1).get(0).getCreateTime()).isEqualTo(sdf.parse("05/02/2008 02:02:02.000"));
        assertThat(query.listPage(0,2)).hasSize(2);
        assertThat(query.listPage(0, 2).get(0).getCreateTime()).isEqualTo(sdf.parse("01/02/2008 02:02:02.000"));
        assertThat(query.listPage(0,2).get(1).getCreateTime()).isEqualTo(sdf.parse("05/02/2008 02:02:02.000"));

        // exclude subtasks
        query = taskService.createTaskQuery().taskAssignee("gonzo").excludeSubtasks().orderByTaskCreateTime().desc();
        assertThat(query.count()).isEqualTo(2);
        assertThat(query.listPage(1,1)).hasSize(1);
        assertThat(query.listPage(1,1).get(0).getCreateTime()).isEqualTo(sdf.parse("01/02/2008 02:02:02.000"));
        assertThat(query.listPage(0, 1)).hasSize(1);
        assertThat(query.listPage(0, 1).get(0).getCreateTime()).isEqualTo(sdf.parse("05/02/2008 02:02:02.000"));

        // kermit has no root tasks and no subtasks assigned include subtasks
        query = taskService.createTaskQuery().taskAssignee(KERMIT).orderByTaskCreateTime().asc();
        assertThat(query.count()).isEqualTo(0);
        assertThat(query.listPage(0,2)).hasSize(0);
        assertThat(query.singleResult()).isNull();
        // exclude subtasks
        query = taskService.createTaskQuery().taskAssignee(KERMIT).excludeSubtasks().orderByTaskCreateTime().desc();
        assertThat(query.count()).isEqualTo(0);
        assertThat(query.listPage(0,2)).hasSize(0);
        assertThat(query.singleResult()).isNull();
    }

    public void testTaskQueryParentTask() throws Exception {
        Task rootTask = taskService.newTask();
        taskService.saveTask(rootTask);

        taskIds.add(rootTask.getId());

        Task subTask = taskService.newTask();
        subTask.setParentTaskId(rootTask.getId());

        taskService.saveTask(subTask);
        taskIds.add(subTask.getId());

        subTask = taskService.newTask();
        subTask.setAssignee("gonzo");
        subTask.setParentTaskId(rootTask.getId());

        taskService.saveTask(subTask);
        taskIds.add(subTask.getId());

        TaskQuery query = taskService.createTaskQuery().taskParentTaskId(rootTask.getId());

        assertThat(query.count()).isEqualTo(2);

        query = taskService.createTaskQuery().taskAssignee("gonzo").taskParentTaskId(rootTask.getId());

        assertThat(query.count()).isEqualTo(1);

        query = taskService.createTaskQuery().taskAssignee("kermit").taskParentTaskId(rootTask.getId());

        assertThat(query.count()).isEqualTo(0);

        query = taskService.createTaskQuery().taskAssignee("gonzo")
                .or()
                    .taskName("A sub task")
                    .taskParentTaskId(rootTask.getId())
                .endOr();

        assertThat(query.count()).isEqualTo(1);
    }

    /**
     * Generates some test sub-tasks to the tasks generated by {@link #generateTestTasks()}.<br/>
     * - 1 root task where kermit is a candidate with 2 subtasks (both with kermit as candidate) <br/>
     * - 2 root task where gonzo is assignee with 3 + 2 subtasks assigned to gonzo
     */
    private List<String> generateTestSubTasks() throws Exception {
        List<String> ids = new ArrayList<String>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
        // 1 parent task for kermit
        processEngineConfiguration.getClock().setCurrentTime(sdf.parse("01/01/2008 01:01:01.000"));
        Task rootTask1 = taskService.newTask();
        rootTask1.setName("rootTestTask");
        rootTask1.setDescription("rootTestTask description");
        taskService.saveTask(rootTask1);
        ids.add(rootTask1.getId());
        taskService.addCandidateUser(rootTask1.getId(), KERMIT);
        // 2 sub-tasks for the task above
        processEngineConfiguration.getClock().setCurrentTime(sdf.parse("01/01/2009 01:01:01.000"));
        for (int i = 1; i <= 2; i++) {
            Task subtask = taskService.newTask();
            subtask.setName("kermitSubTask" + i);
            subtask.setParentTaskId(rootTask1.getId());
            subtask.setDescription("description for kermit sub-task" + i);
            taskService.saveTask(subtask);
            taskService.addCandidateUser(subtask.getId(),
                                         KERMIT);
            ids.add(subtask.getId());
        }

        // 2 parent tasks for gonzo
        // first parent task for gonzo
        processEngineConfiguration.getClock().setCurrentTime(sdf.parse("01/02/2008 02:02:02.000"));
        Task rootTask2 = taskService.newTask();
        rootTask2.setName("gonzoRootTask1");
        rootTask2.setDescription("gonzo Root task1 description");
        taskService.saveTask(rootTask2);
        taskService.setAssignee(rootTask2.getId(),
                                GONZO);
        ids.add(rootTask2.getId());
        // second parent task for gonzo
        processEngineConfiguration.getClock().setCurrentTime(sdf.parse("05/02/2008 02:02:02.000"));
        Task rootTask3 = taskService.newTask();
        rootTask3.setName("gonzoRootTask2");
        rootTask3.setDescription("gonzo Root task2 description");
        taskService.saveTask(rootTask3);
        taskService.setAssignee(rootTask3.getId(),
                                GONZO);
        ids.add(rootTask3.getId());
        // 3 sub-tasks for the first parent task
        processEngineConfiguration.getClock().setCurrentTime(sdf.parse("01/01/2009 01:01:01.000"));
        for (int i = 1; i <= 3; i++) {
            Task subtask = taskService.newTask();
            subtask.setName("gonzoSubTask1_" + i);
            subtask.setParentTaskId(rootTask2.getId());
            subtask.setDescription("description for gonzo sub-task1_" + i);
            taskService.saveTask(subtask);
            taskService.setAssignee(subtask.getId(),
                                    GONZO);
            ids.add(subtask.getId());
        }
        // 2 sub-tasks for the second parent task
        processEngineConfiguration.getClock().setCurrentTime(sdf.parse("02/01/2009 01:01:01.000"));
        for (int i = 1; i <= 2; i++) {
            Task subtask = taskService.newTask();
            subtask.setName("gonzoSubTask2_" + i);
            subtask.setParentTaskId(rootTask3.getId());
            subtask.setDescription("description for gonzo sub-task2_" + i);
            taskService.saveTask(subtask);
            taskService.setAssignee(subtask.getId(),
                                    GONZO);
            ids.add(subtask.getId());
        }
        return ids;
    }
}
