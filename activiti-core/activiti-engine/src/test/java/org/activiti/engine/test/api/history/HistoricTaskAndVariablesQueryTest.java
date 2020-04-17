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
package org.activiti.engine.test.api.history;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.mockito.Mockito;

/**

 */
public class HistoricTaskAndVariablesQueryTest extends PluggableActivitiTestCase {

    private List<String> taskIds;

    private static final String KERMIT = "kermit";
    private static final List<String> KERMITSGROUPS = asList("management","accountancy");

    private static final String GONZO = "gonzo";
    private static final List<String> GONZOSGROUPS = asList();

    private static final String FOZZIE = "fozzie";
    private static final List<String> FOZZIESGROUPS = asList("management");

    private UserGroupManager userGroupManager = Mockito.mock(UserGroupManager.class);

    public void setUp() throws Exception {
        ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) cachedProcessEngine.getProcessEngineConfiguration();
        engineConfiguration.setUserGroupManager(userGroupManager);
        taskIds = generateTestTasks();
    }

    public void tearDown() throws Exception {

        taskService.deleteTasks(taskIds,
                                true);
    }

    @Deployment
    public void testQuery() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().taskAssignee(GONZO).singleResult();
            Map<String, Object> variableMap = task.getTaskLocalVariables();
            assertThat(variableMap).hasSize(2);
            assertThat(task.getProcessVariables()).hasSize(0);
            assertThat(variableMap.get("testVar")).isNotNull();
            assertThat(variableMap.get("testVar")).isEqualTo("someVariable");
            assertThat(variableMap.get("testVar2")).isNotNull();
            assertThat(variableMap.get("testVar2")).isEqualTo(123);

            List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().list();
            assertThat(tasks).hasSize(3);

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().taskAssignee(GONZO).singleResult();
            assertThat(task.getProcessVariables()).hasSize(0);
            assertThat(task.getTaskLocalVariables()).hasSize(0);

            runtimeService.startProcessInstanceByKey("oneTaskProcess", singletonMap("processVar", true));

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().taskAssignee(KERMIT).singleResult();
            assertThat(task.getProcessVariables()).hasSize(1);
            assertThat(task.getTaskLocalVariables()).hasSize(0);
            assertThat((Boolean) task.getProcessVariables().get("processVar")).isTrue();

            taskService.setVariable(task.getId(), "anotherProcessVar", 123);
            taskService.setVariableLocal(task.getId(), "localVar", "test");

            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().taskAssignee(KERMIT).singleResult();
            assertThat(task.getProcessVariables()).hasSize(0);
            assertThat(task.getTaskLocalVariables()).hasSize(1);
            assertThat(task.getTaskLocalVariables().get("localVar")).isEqualTo("test");

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().taskAssignee(KERMIT).singleResult();
            assertThat(task.getProcessVariables()).hasSize(2);
            assertThat(task.getTaskLocalVariables()).hasSize(0);
            assertThat(task.getProcessVariables().get("processVar")).isEqualTo(true);
            assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);

            task = historyService.createHistoricTaskInstanceQuery().taskVariableValueLike("testVar",
                                                                                          "someVaria%").singleResult();
            assertThat(task).isNotNull();
            assertThat(task.getName()).isEqualTo("gonzoTask");

            task = historyService.createHistoricTaskInstanceQuery().taskVariableValueLikeIgnoreCase("testVar",
                                                                                                    "somevaria%").singleResult();
            assertThat(task).isNotNull();
            assertThat(task.getName()).isEqualTo("gonzoTask");

            task = historyService.createHistoricTaskInstanceQuery().taskVariableValueLikeIgnoreCase("testVar",
                                                                                                    "somevaria2%").singleResult();
            assertThat(task).isNull();

            tasks = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().taskInvolvedUser(KERMIT).orderByTaskCreateTime().asc().list();
            assertThat(tasks).hasSize(3);
            assertThat(tasks.get(0).getTaskLocalVariables()).hasSize(1);
            assertThat(tasks.get(0).getTaskLocalVariables().get("test")).isEqualTo("test");
            assertThat(tasks.get(0).getProcessVariables()).hasSize(0);

            tasks = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().taskInvolvedUser(KERMIT).orderByTaskCreateTime().asc().list();
            assertThat(tasks).hasSize(3);
            assertThat(tasks.get(0).getProcessVariables()).hasSize(0);
            assertThat(tasks.get(0).getTaskLocalVariables()).hasSize(0);

            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().taskAssignee(KERMIT).taskVariableValueEquals("localVar",
                                                                                                                                             "test").singleResult();
            assertThat(task.getProcessVariables()).hasSize(0);
            assertThat(task.getTaskLocalVariables()).hasSize(1);
            assertThat(task.getTaskLocalVariables().get("localVar")).isEqualTo("test");

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().taskAssignee(KERMIT).taskVariableValueEquals("localVar",
                                                                                                                                           "test").singleResult();
            assertThat(task.getProcessVariables()).hasSize(2);
            assertThat(task.getTaskLocalVariables()).hasSize(0);
            assertThat(task.getProcessVariables().get("processVar")).isEqualTo(true);
            assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);

            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().includeProcessVariables().taskAssignee(KERMIT).singleResult();
            assertThat(task.getProcessVariables()).hasSize(2);
            assertThat(task.getTaskLocalVariables()).hasSize(1);
            assertThat(task.getTaskLocalVariables().get("localVar")).isEqualTo("test");
            assertThat(task.getProcessVariables().get("processVar")).isEqualTo(true);
            assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);

            task = historyService.createHistoricTaskInstanceQuery().taskAssignee(GONZO).singleResult();
            taskService.complete(task.getId());
            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().finished().singleResult();
            variableMap = task.getTaskLocalVariables();
            assertThat(variableMap).hasSize(2);
            assertThat(task.getProcessVariables()).hasSize(0);
            assertThat(variableMap.get("testVar")).isNotNull();
            assertThat(variableMap.get("testVar")).isEqualTo("someVariable");
            assertThat(variableMap.get("testVar2")).isNotNull();
            assertThat(variableMap.get("testVar2")).isEqualTo(123);
        }
    }

    @Deployment
    public void testOrQuery() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery()
                    .includeTaskLocalVariables()
                    .or()
                    .taskAssignee(GONZO)
                    .endOr()
                    .singleResult();

            Map<String, Object> variableMap = task.getTaskLocalVariables();
            assertThat(variableMap).hasSize(2);
            assertThat(task.getProcessVariables()).hasSize(0);
            assertThat(variableMap.get("testVar")).isNotNull();
            assertThat(variableMap.get("testVar")).isEqualTo("someVariable");
            assertThat(variableMap.get("testVar2")).isNotNull();
            assertThat(variableMap.get("testVar2")).isEqualTo(123);

            List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().list();
            assertThat(tasks).hasSize(3);

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().or().taskAssignee(GONZO).taskVariableValueEquals("localVar",
                                                                                                                                               "nonExisting").endOr().singleResult();
            assertThat(task.getProcessVariables()).hasSize(0);
            assertThat(task.getTaskLocalVariables()).hasSize(0);

            runtimeService.startProcessInstanceByKey("oneTaskProcess", singletonMap("processVar", true));

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().or().taskAssignee(KERMIT).taskVariableValueEquals("localVar",
                                                                                                                                                "nonExisting").endOr().singleResult();
            assertThat(task.getProcessVariables()).hasSize(1);
            assertThat(task.getTaskLocalVariables()).hasSize(0);
            assertThat((Boolean) task.getProcessVariables().get("processVar")).isTrue();

            task = historyService.createHistoricTaskInstanceQuery()
                    .includeProcessVariables()
                    .or()
                    .taskAssignee(KERMIT)
                    .taskVariableValueEquals("localVar",
                                             "nonExisting")
                    .endOr()
                    .or()
                    .processDefinitionKey("oneTaskProcess")
                    .taskVariableValueEquals("localVar",
                                             "nonExisting")
                    .endOr()
                    .singleResult();

            assertThat(task).isNotNull();
            assertThat(task.getProcessVariables()).hasSize(1);
            assertThat(task.getTaskLocalVariables()).hasSize(0);
            assertThat((Boolean) task.getProcessVariables().get("processVar")).isTrue();

            taskService.setVariable(task.getId(), "anotherProcessVar", 123);
            taskService.setVariableLocal(task.getId(), "localVar", "test");

            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().or().taskAssignee(KERMIT).taskVariableValueEquals("localVar",
                                                                                                                                                  "nonExisting").endOr().singleResult();
            assertThat(task.getProcessVariables()).hasSize(0);
            assertThat(task.getTaskLocalVariables()).hasSize(1);
            assertThat(task.getTaskLocalVariables().get("localVar")).isEqualTo("test");

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().or().taskAssignee(KERMIT).taskVariableValueEquals("localVar",
                                                                                                                                                "nonExisting").endOr().singleResult();
            assertThat(task.getProcessVariables()).hasSize(2);
            assertThat(task.getTaskLocalVariables()).hasSize(0);
            assertThat(task.getProcessVariables().get("processVar")).isEqualTo(true);
            assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);

            task = historyService.createHistoricTaskInstanceQuery()
                    .includeTaskLocalVariables()
                    .or()
                    .taskAssignee("nonexisting")
                    .taskVariableValueLike("testVar",
                                           "someVar%")
                    .endOr()
                    .singleResult();
            assertThat(task.getTaskLocalVariables()).hasSize(2);
            assertThat(task.getProcessVariables()).hasSize(0);
            assertThat(task.getTaskLocalVariables().get("testVar")).isEqualTo("someVariable");
            assertThat(task.getTaskLocalVariables().get("testVar2")).isEqualTo(123);

            task = historyService.createHistoricTaskInstanceQuery()
                    .includeTaskLocalVariables()
                    .or()
                    .taskAssignee("nonexisting")
                    .taskVariableValueLikeIgnoreCase("testVar",
                                                     "somevar%")
                    .endOr()
                    .singleResult();
            assertThat(task.getTaskLocalVariables()).hasSize(2);
            assertThat(task.getProcessVariables()).hasSize(0);
            assertThat(task.getTaskLocalVariables().get("testVar")).isEqualTo("someVariable");
            assertThat(task.getTaskLocalVariables().get("testVar2")).isEqualTo(123);

            task = historyService.createHistoricTaskInstanceQuery()
                    .includeTaskLocalVariables()
                    .or()
                    .taskAssignee("nonexisting")
                    .taskVariableValueLike("testVar",
                                           "someVar2%")
                    .endOr()
                    .singleResult();
            assertThat(task).isNull();

            tasks = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables()
                    .or()
                    .taskInvolvedUser(KERMIT)
                    .taskVariableValueEquals("localVar",
                                             "nonExisting")
                    .endOr()
                    .orderByTaskCreateTime().asc().list();
            assertThat(tasks).hasSize(3);
            assertThat(tasks.get(0).getTaskLocalVariables()).hasSize(1);
            assertThat(tasks.get(0).getTaskLocalVariables().get("test")).isEqualTo("test");
            assertThat(tasks.get(0).getProcessVariables()).hasSize(0);

            tasks = historyService.createHistoricTaskInstanceQuery().includeProcessVariables()
                    .or()
                    .taskInvolvedUser(KERMIT)
                    .taskVariableValueEquals("localVar",
                                             "nonExisting")
                    .endOr()
                    .orderByTaskCreateTime().asc().list();
            assertThat(tasks).hasSize(3);
            assertThat(tasks.get(0).getProcessVariables()).hasSize(0);
            assertThat(tasks.get(0).getTaskLocalVariables()).hasSize(0);

            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().taskAssignee(KERMIT).or().taskVariableValueEquals("localVar",
                                                                                                                                                  "test")
                    .taskVariableValueEquals("localVar",
                                             "nonExisting").endOr().singleResult();
            assertThat(task.getProcessVariables()).hasSize(0);
            assertThat(task.getTaskLocalVariables()).hasSize(1);
            assertThat(task.getTaskLocalVariables().get("localVar")).isEqualTo("test");

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().taskAssignee(KERMIT).or().taskVariableValueEquals("localVar",
                                                                                                                                                "test")
                    .taskVariableValueEquals("localVar",
                                             "nonExisting").endOr().singleResult();
            assertThat(task.getProcessVariables()).hasSize(2);
            assertThat(task.getTaskLocalVariables()).hasSize(0);
            assertThat(task.getProcessVariables().get("processVar")).isEqualTo(true);
            assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);

            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().includeProcessVariables().or().taskAssignee(KERMIT).taskVariableValueEquals("localVar",
                                                                                                                                                                            "nonExisting")
                    .endOr().singleResult();
            assertThat(task.getProcessVariables()).hasSize(2);
            assertThat(task.getTaskLocalVariables()).hasSize(1);
            assertThat(task.getTaskLocalVariables().get("localVar")).isEqualTo("test");
            assertThat(task.getProcessVariables().get("processVar")).isEqualTo(true);
            assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);

            task = historyService.createHistoricTaskInstanceQuery().taskAssignee(GONZO).singleResult();
            taskService.complete(task.getId());
            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().or().finished().taskVariableValueEquals("localVar",
                                                                                                                                        "nonExisting").endOr().singleResult();
            variableMap = task.getTaskLocalVariables();
            assertThat(variableMap).hasSize(2);
            assertThat(task.getProcessVariables()).hasSize(0);
            assertThat(variableMap.get("testVar")).isNotNull();
            assertThat(variableMap.get("testVar")).isEqualTo("someVariable");
            assertThat(variableMap.get("testVar2")).isNotNull();
            assertThat(variableMap.get("testVar2")).isEqualTo(123);
        }
    }

    @Deployment
    public void testOrQueryMultipleVariableValues() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            Map<String, Object> startMap = new HashMap<String, Object>();
            startMap.put("processVar", true);
            startMap.put("anotherProcessVar", 123);
            runtimeService.startProcessInstanceByKey("oneTaskProcess", startMap);

            startMap.put("anotherProcessVar", 999);
            runtimeService.startProcessInstanceByKey("oneTaskProcess", startMap);

            HistoricTaskInstanceQuery query0 = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().or();
            for (int i = 0; i < 20; i++) {
                query0 = query0.processVariableValueEquals("anotherProcessVar", i);
            }
            query0 = query0.endOr();
            assertThat(query0.singleResult()).isNull();

            HistoricTaskInstanceQuery query1 = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().or().processVariableValueEquals("anotherProcessVar",
                                                                                                                                                          123);
            for (int i = 0; i < 20; i++) {
                query1 = query1.processVariableValueEquals("anotherProcessVar", i);
            }
            query1 = query1.endOr();
            HistoricTaskInstance task = query1.singleResult();
            assertThat(task.getProcessVariables()).hasSize(2);
            assertThat(task.getProcessVariables().get("processVar")).isEqualTo(true);
            assertThat(task.getProcessVariables().get("anotherProcessVar")).isEqualTo(123);
        }
    }

    @Deployment
    public void testCandidate() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
            List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT,
                                                                                                                  KERMITSGROUPS).list();
            assertThat(tasks).hasSize(3);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO,
                                                                                       GONZOSGROUPS).list();
            assertThat(tasks).hasSize(0);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(FOZZIE,
                                                                                       FOZZIESGROUPS).list();
            assertThat(tasks).hasSize(1);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateGroup("management").list();
            assertThat(tasks).hasSize(1);
            List<String> groups = new ArrayList<String>();
            groups.add("management");
            groups.add("accountancy");
            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateGroupIn(groups).list();
            assertThat(tasks).hasSize(1);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT,
                                                                                       KERMITSGROUPS).taskCandidateGroupIn(groups).list();
            assertThat(tasks).hasSize(3);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO,
                                                                                       GONZOSGROUPS).taskCandidateGroupIn(groups).list();
            assertThat(tasks).hasSize(1);

            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
            taskService.complete(task.getId());

            assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT,
                                                                                       KERMITSGROUPS).list();
            assertThat(tasks).hasSize(3);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO,
                                                                                       GONZOSGROUPS).list();
            assertThat(tasks).hasSize(0);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(FOZZIE,
                                                                                       FOZZIESGROUPS).list();
            assertThat(tasks).hasSize(1);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateGroup("management").list();
            assertThat(tasks).hasSize(1);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT,
                                                                                       KERMITSGROUPS).taskCandidateGroupIn(asList("management")).list();
            assertThat(tasks).hasSize(3);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO,
                                                                                       GONZOSGROUPS).taskCandidateGroupIn(asList("management")).list();
            assertThat(tasks).hasSize(1);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO,
                                                                                       GONZOSGROUPS).taskCandidateGroupIn(asList("invalid")).list();
            assertThat(tasks).hasSize(0);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateGroupIn(groups).list();
            assertThat(tasks).hasSize(1);
        }
    }

    @Deployment
    public void testCandidateWithUserGroupProxy() {
        //don't specify groups in query calls, instead get them through UserGroupLookupProxy (which could be remote service)

        Mockito.when(userGroupManager.getUserGroups(KERMIT)).thenReturn(KERMITSGROUPS);
        Mockito.when(userGroupManager.getUserGroups(GONZO)).thenReturn(GONZOSGROUPS);
        Mockito.when(userGroupManager.getUserGroups(FOZZIE)).thenReturn(FOZZIESGROUPS);

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            runtimeService.startProcessInstanceByKey("oneTaskProcess");
            List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT,
                                                                                                                  KERMITSGROUPS).list();
            assertThat(tasks).hasSize(3);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO).list();
            assertThat(tasks).hasSize(0);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(FOZZIE).list();
            assertThat(tasks).hasSize(1);

            List<String> groups = new ArrayList<String>();
            groups.add("management");
            groups.add("accountancy");

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT).taskCandidateGroupIn(groups).list();
            assertThat(tasks).hasSize(3);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO).taskCandidateGroupIn(groups).list();
            assertThat(tasks).hasSize(1);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT).list();
            assertThat(tasks).hasSize(3);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO).list();
            assertThat(tasks).hasSize(0);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(FOZZIE).list();
            assertThat(tasks).hasSize(1);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT).taskCandidateGroupIn(asList("management")).list();
            assertThat(tasks).hasSize(3);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO).taskCandidateGroupIn(asList("management")).list();
            assertThat(tasks).hasSize(1);

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO).taskCandidateGroupIn(asList("invalid")).list();
            assertThat(tasks).hasSize(0);
        }
    }

    public void testQueryWithPagingAndVariables() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().includeTaskLocalVariables().orderByTaskPriority().desc().listPage(0,
                                                                                                                                                                                            1);
            assertThat(tasks).hasSize(1);
            HistoricTaskInstance task = tasks.get(0);
            Map<String, Object> variableMap = task.getTaskLocalVariables();
            assertThat(variableMap).hasSize(2);
            assertThat(variableMap.get("testVar")).isEqualTo("someVariable");
            assertThat(variableMap.get("testVar2")).isEqualTo(123);

            tasks = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().includeTaskLocalVariables().orderByTaskPriority().asc().listPage(1,
                                                                                                                                                                2);
            assertThat(tasks).hasSize(2);
            task = tasks.get(1);
            variableMap = task.getTaskLocalVariables();
            assertThat(variableMap).hasSize(2);
            assertThat(variableMap.get("testVar")).isEqualTo("someVariable");
            assertThat(variableMap.get("testVar2")).isEqualTo(123);

            tasks = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().includeTaskLocalVariables().orderByTaskPriority().asc().listPage(2,
                                                                                                                                                                4);
            assertThat(tasks).hasSize(1);
            task = tasks.get(0);
            variableMap = task.getTaskLocalVariables();
            assertThat(variableMap).hasSize(2);
            assertThat(variableMap.get("testVar")).isEqualTo("someVariable");
            assertThat(variableMap.get("testVar2")).isEqualTo(123);

            tasks = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().includeTaskLocalVariables().orderByTaskPriority().asc().listPage(4,
                                                                                                                                                                2);
            assertThat(tasks).hasSize(0);
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
    public void testWithoutDueDateQuery() throws Exception {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
            HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).withoutTaskDueDate().singleResult();
            assertThat(historicTask).isNotNull();
            assertThat(historicTask.getDueDate()).isNull();

            // Set due-date on task
            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
            Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 01:12:13");
            task.setDueDate(dueDate);
            taskService.saveTask(task);

            assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).withoutTaskDueDate().count()).isEqualTo(0);

            task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

            // Clear due-date on task
            task.setDueDate(null);
            taskService.saveTask(task);

            assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).withoutTaskDueDate().count()).isEqualTo(1);
        }
    }

    // Unit test for https://activiti.atlassian.net/browse/ACT-4152
    public void testQueryWithIncludeTaskVariableAndTaskCategory() {
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().taskAssignee(GONZO).list();
        for (HistoricTaskInstance task : tasks) {
            assertThat(task.getCategory()).isNotNull();
            assertThat(task.getCategory()).isEqualTo("testCategory");
        }

        tasks = historyService.createHistoricTaskInstanceQuery().taskAssignee(GONZO).includeTaskLocalVariables().list();
        for (HistoricTaskInstance task : tasks) {
            assertThat(task.getCategory()).isNotNull();
            assertThat(task.getCategory()).isEqualTo("testCategory");
        }

        tasks = historyService.createHistoricTaskInstanceQuery().taskAssignee(GONZO).includeProcessVariables().list();
        for (HistoricTaskInstance task : tasks) {
            assertThat(task.getCategory()).isNotNull();
            assertThat(task.getCategory()).isEqualTo("testCategory");
        }
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
                                     "testVar2",
                                     123);
        ids.add(task.getId());

        return ids;
    }
}
