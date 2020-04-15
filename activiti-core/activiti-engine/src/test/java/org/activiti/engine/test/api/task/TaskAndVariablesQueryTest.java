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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**

 */
public class TaskAndVariablesQueryTest extends PluggableActivitiTestCase {

    private List<String> taskIds;
    private List<String> multipleTaskIds;

    private static final String KERMIT = "kermit";
    private static final List<String> KERMITSGROUPS = asList("management", "accountancy");

    private static final String GONZO = "gonzo";

    public void setUp() throws Exception {
        taskIds = generateTestTasks();
    }

    public void tearDown() throws Exception {
        taskService.deleteTasks(taskIds, true);
    }

    @Deployment
    public void testQuery() {
        Task task = taskService.createTaskQuery().includeTaskLocalVariables().taskAssignee(GONZO).singleResult();
        Map<String, Object> variableMap = task.getTaskLocalVariables();
        assertThat(variableMap).hasSize(3);
        assertThat(task.getProcessVariables()).hasSize(0);
        assertThat(variableMap.get("testVar")).isNotNull();
        assertThat(variableMap.get("testVar")).isEqualTo("someVariable");
        assertThat(variableMap.get("testVar2")).isNotNull();
        assertThat(variableMap.get("testVar2")).isEqualTo(123);
        assertThat(variableMap.get("testVarBinary")).isNotNull();
        assertThat(new String((byte[]) variableMap.get("testVarBinary"))).isEqualTo("This is a binary variable");

        List<Task> tasks = taskService.createTaskQuery().list();
        assertThat(tasks).hasSize(3);

        task = taskService.createTaskQuery().includeProcessVariables().taskAssignee(GONZO).singleResult();
        assertThat(task.getProcessVariables()).hasSize(0);
        assertThat(task.getTaskLocalVariables()).hasSize(0);

        Map<String, Object> startMap = new HashMap<String, Object>();
        startMap.put("processVar", true);
        startMap.put("binaryVariable", "This is a binary process variable".getBytes());
        runtimeService.startProcessInstanceByKey("oneTaskProcess", startMap);

        task = taskService.createTaskQuery().includeProcessVariables().taskAssignee(KERMIT).singleResult();
        assertThat(task.getProcessVariables()).hasSize(2);
        assertThat(task.getTaskLocalVariables()).hasSize(0);
        assertThat((Boolean) task.getProcessVariables().get("processVar")).isTrue();
        assertThat(new String((byte[]) task.getProcessVariables().get("binaryVariable"))).isEqualTo("This is a binary process variable");

        taskService.setVariable(task.getId(), "anotherProcessVar", 123);
        taskService.setVariableLocal(task.getId(), "localVar", "test");

        task = taskService.createTaskQuery().includeTaskLocalVariables().taskAssignee(KERMIT).singleResult();
        assertThat(task.getProcessVariables()).hasSize(0);
        assertThat(task.getTaskLocalVariables()).hasSize(1);
        assertThat(task.getTaskLocalVariables().get("localVar")).isEqualTo("test");

        task = taskService.createTaskQuery().includeProcessVariables().taskAssignee(KERMIT).singleResult();
        assertThat(task.getProcessVariables()).hasSize(3);
        assertThat(task.getTaskLocalVariables()).hasSize(0);
        assertThat(task.getProcessVariables().get("processVar")).isEqualTo(true);
        assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);
        assertThat(new String((byte[]) task.getProcessVariables().get("binaryVariable"))).isEqualTo("This is a binary process variable");

        tasks = taskService.createTaskQuery().includeTaskLocalVariables().taskCandidateUser(KERMIT, KERMITSGROUPS).list();
        assertThat(tasks).hasSize(2);
        assertThat(tasks.get(0).getTaskLocalVariables()).hasSize(2);
        assertThat(tasks.get(0).getTaskLocalVariables().get("test")).isEqualTo("test");
        assertThat(tasks.get(0).getProcessVariables()).hasSize(0);

        tasks = taskService.createTaskQuery().includeProcessVariables().taskCandidateUser(KERMIT, KERMITSGROUPS).list();
        assertThat(tasks).hasSize(2);
        assertThat(tasks.get(0).getProcessVariables()).hasSize(0);
        assertThat(tasks.get(0).getTaskLocalVariables()).hasSize(0);

        task = taskService.createTaskQuery().includeTaskLocalVariables().taskAssignee(KERMIT).taskVariableValueEquals("localVar",
                                                                                                                      "test").singleResult();
        assertThat(task.getProcessVariables()).hasSize(0);
        assertThat(task.getTaskLocalVariables()).hasSize(1);
        assertThat(task.getTaskLocalVariables().get("localVar")).isEqualTo("test");

        task = taskService.createTaskQuery().includeProcessVariables().taskAssignee(KERMIT).taskVariableValueEquals("localVar",
                                                                                                                    "test").singleResult();
        assertThat(task.getProcessVariables()).hasSize(3);
        assertThat(task.getTaskLocalVariables()).hasSize(0);
        assertThat(task.getProcessVariables().get("processVar")).isEqualTo(true);
        assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);

        task = taskService.createTaskQuery().includeTaskLocalVariables().includeProcessVariables().taskAssignee(KERMIT).singleResult();
        assertThat(task.getProcessVariables()).hasSize(3);
        assertThat(task.getTaskLocalVariables()).hasSize(1);
        assertThat(task.getTaskLocalVariables().get("localVar")).isEqualTo("test");
        assertThat(task.getProcessVariables().get("processVar")).isEqualTo(true);
        assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);
        assertThat(new String((byte[]) task.getProcessVariables().get("binaryVariable"))).isEqualTo("This is a binary process variable");
    }

    public void testQueryWithPagingAndVariables() {
        List<Task> tasks = taskService.createTaskQuery().includeProcessVariables().includeTaskLocalVariables().orderByTaskPriority().desc().listPage(0,
                                                                                                                                                     1);
        assertThat(tasks).hasSize(1);
        Task task = tasks.get(0);
        Map<String, Object> variableMap = task.getTaskLocalVariables();
        assertThat(variableMap).hasSize(3);
        assertThat(variableMap.get("testVar")).isEqualTo("someVariable");
        assertThat(variableMap.get("testVar2")).isEqualTo(123);
        assertThat(new String((byte[]) variableMap.get("testVarBinary"))).isEqualTo("This is a binary variable");

        tasks = taskService.createTaskQuery().includeProcessVariables().includeTaskLocalVariables().orderByTaskPriority().asc().listPage(1,
                                                                                                                                         2);
        assertThat(tasks).hasSize(2);
        task = tasks.get(1);
        variableMap = task.getTaskLocalVariables();
        assertThat(variableMap).hasSize(3);
        assertThat(variableMap.get("testVar")).isEqualTo("someVariable");
        assertThat(variableMap.get("testVar2")).isEqualTo(123);
        assertThat(new String((byte[]) variableMap.get("testVarBinary"))).isEqualTo("This is a binary variable");

        tasks = taskService.createTaskQuery().includeProcessVariables().includeTaskLocalVariables().orderByTaskPriority().asc().listPage(2,
                                                                                                                                         4);
        assertThat(tasks).hasSize(1);
        task = tasks.get(0);
        variableMap = task.getTaskLocalVariables();
        assertThat(variableMap).hasSize(3);
        assertThat(variableMap.get("testVar")).isEqualTo("someVariable");
        assertThat(variableMap.get("testVar2")).isEqualTo(123);
        assertThat(new String((byte[]) variableMap.get("testVarBinary"))).isEqualTo("This is a binary variable");

        tasks = taskService.createTaskQuery().includeProcessVariables().includeTaskLocalVariables().orderByTaskPriority().asc().listPage(4,
                                                                                                                                         2);
        assertThat(tasks).hasSize(0);
    }

    // Unit test for https://activiti.atlassian.net/browse/ACT-4152
    public void testQueryWithIncludeTaskVariableAndTaskCategory() {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(GONZO).list();
        for (Task task : tasks) {
            assertThat(task.getCategory()).isNotNull();
            assertThat(task.getCategory()).isEqualTo("testCategory");
        }

        tasks = taskService.createTaskQuery().taskAssignee(GONZO).includeTaskLocalVariables().list();
        for (Task task : tasks) {
            assertThat(task.getCategory()).isNotNull();
            assertThat(task.getCategory()).isEqualTo("testCategory");
        }

        tasks = taskService.createTaskQuery().taskAssignee(GONZO).includeProcessVariables().list();
        for (Task task : tasks) {
            assertThat(task.getCategory()).isNotNull();
            assertThat(task.getCategory()).isEqualTo("testCategory");
        }
    }

    public void testQueryWithLimitAndVariables() throws Exception {

        int taskVariablesLimit = 2000;
        int expectedNumberOfTasks = 103;

        try {
            //setup - create 100 tasks
            multipleTaskIds = generateMultipleTestTasks();

            // limit results to 2000 and set maxResults for paging to 200
            // please see MNT-16040
            List<Task> tasks = taskService.createTaskQuery()
                    .includeProcessVariables()
                    .includeTaskLocalVariables()
                    .limitTaskVariables(taskVariablesLimit)
                    .orderByTaskPriority()
                    .asc()
                    .listPage(0,
                              200);
            // 100 tasks created by generateMultipleTestTasks and 3 created previously at setUp
            assertThat(tasks).hasSize(expectedNumberOfTasks);

            tasks = taskService.createTaskQuery()
                    .includeProcessVariables()
                    .includeTaskLocalVariables()
                    .orderByTaskPriority()
                    .limitTaskVariables(taskVariablesLimit)
                    .asc()
                    .listPage(50,
                              100);
            assertThat(tasks).hasSize(53);
        } finally {
            taskService.deleteTasks(multipleTaskIds,
                                    true);
        }
    }

    @Deployment
    public void testOrQuery() {
        Map<String, Object> startMap = new HashMap<String, Object>();
        startMap.put("anotherProcessVar",
                     123);
        runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                 startMap);

        Task task = taskService.createTaskQuery().includeProcessVariables().or().processVariableValueEquals("undefined",
                                                                                                            999).processVariableValueEquals("anotherProcessVar",
                                                                                                                                            123).endOr().singleResult();
        assertThat(task.getProcessVariables()).hasSize(1);
        assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);

        task = taskService.createTaskQuery().includeProcessVariables().or().processVariableValueEquals("undefined",
                                                                                                       999).endOr().singleResult();
        assertThat(task).isNull();

        task = taskService.createTaskQuery().includeProcessVariables().or().processVariableValueEquals("anotherProcessVar",
                                                                                                       123).processVariableValueEquals("undefined",
                                                                                                                                       999).endOr().singleResult();
        assertThat(task.getProcessVariables()).hasSize(1);
        assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);

        task = taskService.createTaskQuery().includeProcessVariables().or().processVariableValueEquals("anotherProcessVar",
                                                                                                       123).endOr().singleResult();
        assertThat(task.getProcessVariables()).hasSize(1);
        assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);

        task = taskService.createTaskQuery().includeProcessVariables().or().processVariableValueEquals("anotherProcessVar",
                                                                                                       999).endOr().singleResult();
        assertThat(task).isNull();

        task = taskService.createTaskQuery().includeProcessVariables().or().processVariableValueEquals("anotherProcessVar",
                                                                                                       999).processVariableValueEquals("anotherProcessVar",
                                                                                                                                       123).endOr().singleResult();
        assertThat(task.getProcessVariables()).hasSize(1);
        assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);
    }

    @Deployment
    public void testOrQueryMultipleVariableValues() {
        Map<String, Object> startMap = new HashMap<String, Object>();
        startMap.put("aProcessVar",
                     1);
        startMap.put("anotherProcessVar",
                     123);
        runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                 startMap);

        TaskQuery query0 = taskService.createTaskQuery().includeProcessVariables().or();
        for (int i = 0; i < 20; i++) {
            query0 = query0.processVariableValueEquals("anotherProcessVar",
                                                       i);
        }
        query0 = query0.endOr();
        assertThat(query0.singleResult()).isNull();

        TaskQuery query1 = taskService.createTaskQuery().includeProcessVariables().or().processVariableValueEquals("anotherProcessVar",
                                                                                                                   123);
        for (int i = 0; i < 20; i++) {
            query1 = query1.processVariableValueEquals("anotherProcessVar",
                                                       i);
        }
        query1 = query1.endOr();
        Task task = query1.singleResult();
        assertThat(task.getProcessVariables()).hasSize(2);
        assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);
    }

    /**
     * Generates some test tasks. - 2 tasks where kermit is a candidate and 1 task where gonzo is assignee
     */
    private List<String> generateTestTasks() throws Exception {
        List<String> ids = new ArrayList<String>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
        // 2 tasks for kermit
        processEngineConfiguration.getClock().setCurrentTime(sdf.parse("01/01/2001 01:01:01.000"));
        for (int i = 0; i < 2; i++) {
            Task task = taskService.newTask();
            task.setName("testTask");
            task.setDescription("testTask description");
            task.setPriority(3);
            taskService.saveTask(task);
            ids.add(task.getId());
            taskService.setVariableLocal(task.getId(),
                                         "test",
                                         "test");
            taskService.setVariableLocal(task.getId(),
                                         "testBinary",
                                         "This is a binary variable".getBytes());
            taskService.addCandidateUser(task.getId(),
                                         KERMIT);
        }

        processEngineConfiguration.getClock().setCurrentTime(sdf.parse("02/02/2002 02:02:02.000"));
        // 1 task for gonzo
        Task task = taskService.newTask();
        task.setName("gonzoTask");
        task.setDescription("gonzo description");
        task.setPriority(4);
        task.setCategory("testCategory");
        taskService.saveTask(task);
        taskService.setAssignee(task.getId(),
                                GONZO);
        taskService.setVariableLocal(task.getId(),
                                     "testVar",
                                     "someVariable");
        taskService.setVariableLocal(task.getId(),
                                     "testVarBinary",
                                     "This is a binary variable".getBytes());
        taskService.setVariableLocal(task.getId(),
                                     "testVar2",
                                     123);
        ids.add(task.getId());

        return ids;
    }

    /**
     * Generates 100 test tasks.
     */
    private List<String> generateMultipleTestTasks() throws Exception {
        List<String> ids = new ArrayList<String>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
        processEngineConfiguration.getClock().setCurrentTime(sdf.parse("01/01/2001 01:01:01.000"));
        for (int i = 0; i < 100; i++) {
            Task task = taskService.newTask();
            task.setName("testTask");
            task.setDescription("testTask description");
            task.setPriority(3);
            taskService.saveTask(task);
            ids.add(task.getId());
            taskService.setVariableLocal(task.getId(),
                                         "test",
                                         "test");
            taskService.setVariableLocal(task.getId(),
                                         "testBinary",
                                         "This is a binary variable".getBytes());
            taskService.addCandidateUser(task.getId(),
                                         KERMIT);
        }
        return ids;
    }
}
