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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.runtime.api.identity.UserGroupManager;
import org.mockito.Mockito;

/**

 */
public class HistoricTaskAndVariablesQueryTest extends PluggableActivitiTestCase {

    private List<String> taskIds;

    private static final String KERMIT = "kermit";
    private static final List<String> KERMITSGROUPS = Arrays.asList("management",
                                                                    "accountancy");

    private static final String GONZO = "gonzo";
    private static final List<String> GONZOSGROUPS = Arrays.asList();

    private static final String FOZZIE = "fozzie";
    private static final List<String> FOZZIESGROUPS = Arrays.asList("management");

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
            assertEquals(2,
                         variableMap.size());
            assertEquals(0,
                         task.getProcessVariables().size());
            assertNotNull(variableMap.get("testVar"));
            assertEquals("someVariable",
                         variableMap.get("testVar"));
            assertNotNull(variableMap.get("testVar2"));
            assertEquals(123,
                         variableMap.get("testVar2"));

            List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().list();
            assertEquals(3,
                         tasks.size());

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().taskAssignee(GONZO).singleResult();
            assertEquals(0,
                         task.getProcessVariables().size());
            assertEquals(0,
                         task.getTaskLocalVariables().size());

            Map<String, Object> startMap = new HashMap<String, Object>();
            startMap.put("processVar",
                         true);
            runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                     startMap);

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().taskAssignee(KERMIT).singleResult();
            assertEquals(1,
                         task.getProcessVariables().size());
            assertEquals(0,
                         task.getTaskLocalVariables().size());
            assertTrue((Boolean) task.getProcessVariables().get("processVar"));

            taskService.setVariable(task.getId(),
                                    "anotherProcessVar",
                                    123);
            taskService.setVariableLocal(task.getId(),
                                         "localVar",
                                         "test");

            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().taskAssignee(KERMIT).singleResult();
            assertEquals(0,
                         task.getProcessVariables().size());
            assertEquals(1,
                         task.getTaskLocalVariables().size());
            assertEquals("test",
                         task.getTaskLocalVariables().get("localVar"));

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().taskAssignee(KERMIT).singleResult();
            assertEquals(2,
                         task.getProcessVariables().size());
            assertEquals(0,
                         task.getTaskLocalVariables().size());
            assertEquals(true,
                         task.getProcessVariables().get("processVar"));
            assertEquals(123,
                         task.getProcessVariables().get("anotherProcessVar"));

            task = historyService.createHistoricTaskInstanceQuery().taskVariableValueLike("testVar",
                                                                                          "someVaria%").singleResult();
            assertNotNull(task);
            assertEquals("gonzoTask",
                         task.getName());

            task = historyService.createHistoricTaskInstanceQuery().taskVariableValueLikeIgnoreCase("testVar",
                                                                                                    "somevaria%").singleResult();
            assertNotNull(task);
            assertEquals("gonzoTask",
                         task.getName());

            task = historyService.createHistoricTaskInstanceQuery().taskVariableValueLikeIgnoreCase("testVar",
                                                                                                    "somevaria2%").singleResult();
            assertNull(task);

            tasks = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().taskInvolvedUser(KERMIT).orderByTaskCreateTime().asc().list();
            assertEquals(3,
                         tasks.size());
            assertEquals(1,
                         tasks.get(0).getTaskLocalVariables().size());
            assertEquals("test",
                         tasks.get(0).getTaskLocalVariables().get("test"));
            assertEquals(0,
                         tasks.get(0).getProcessVariables().size());

            tasks = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().taskInvolvedUser(KERMIT).orderByTaskCreateTime().asc().list();
            assertEquals(3,
                         tasks.size());
            assertEquals(0,
                         tasks.get(0).getProcessVariables().size());
            assertEquals(0,
                         tasks.get(0).getTaskLocalVariables().size());

            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().taskAssignee(KERMIT).taskVariableValueEquals("localVar",
                                                                                                                                             "test").singleResult();
            assertEquals(0,
                         task.getProcessVariables().size());
            assertEquals(1,
                         task.getTaskLocalVariables().size());
            assertEquals("test",
                         task.getTaskLocalVariables().get("localVar"));

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().taskAssignee(KERMIT).taskVariableValueEquals("localVar",
                                                                                                                                           "test").singleResult();
            assertEquals(2,
                         task.getProcessVariables().size());
            assertEquals(0,
                         task.getTaskLocalVariables().size());
            assertEquals(true,
                         task.getProcessVariables().get("processVar"));
            assertEquals(123,
                         task.getProcessVariables().get("anotherProcessVar"));

            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().includeProcessVariables().taskAssignee(KERMIT).singleResult();
            assertEquals(2,
                         task.getProcessVariables().size());
            assertEquals(1,
                         task.getTaskLocalVariables().size());
            assertEquals("test",
                         task.getTaskLocalVariables().get("localVar"));
            assertEquals(true,
                         task.getProcessVariables().get("processVar"));
            assertEquals(123,
                         task.getProcessVariables().get("anotherProcessVar"));

            task = historyService.createHistoricTaskInstanceQuery().taskAssignee(GONZO).singleResult();
            taskService.complete(task.getId());
            task = (HistoricTaskInstance) historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().finished().singleResult();
            variableMap = task.getTaskLocalVariables();
            assertEquals(2,
                         variableMap.size());
            assertEquals(0,
                         task.getProcessVariables().size());
            assertNotNull(variableMap.get("testVar"));
            assertEquals("someVariable",
                         variableMap.get("testVar"));
            assertNotNull(variableMap.get("testVar2"));
            assertEquals(123,
                         variableMap.get("testVar2"));
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
            assertEquals(2,
                         variableMap.size());
            assertEquals(0,
                         task.getProcessVariables().size());
            assertNotNull(variableMap.get("testVar"));
            assertEquals("someVariable",
                         variableMap.get("testVar"));
            assertNotNull(variableMap.get("testVar2"));
            assertEquals(123,
                         variableMap.get("testVar2"));

            List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().list();
            assertEquals(3,
                         tasks.size());

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().or().taskAssignee(GONZO).taskVariableValueEquals("localVar",
                                                                                                                                               "nonExisting").endOr().singleResult();
            assertEquals(0,
                         task.getProcessVariables().size());
            assertEquals(0,
                         task.getTaskLocalVariables().size());

            Map<String, Object> startMap = new HashMap<String, Object>();
            startMap.put("processVar",
                         true);
            runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                     startMap);

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().or().taskAssignee(KERMIT).taskVariableValueEquals("localVar",
                                                                                                                                                "nonExisting").endOr().singleResult();
            assertEquals(1,
                         task.getProcessVariables().size());
            assertEquals(0,
                         task.getTaskLocalVariables().size());
            assertTrue((Boolean) task.getProcessVariables().get("processVar"));

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

            assertNotNull(task);
            assertEquals(1,
                         task.getProcessVariables().size());
            assertEquals(0,
                         task.getTaskLocalVariables().size());
            assertTrue((Boolean) task.getProcessVariables().get("processVar"));

            taskService.setVariable(task.getId(),
                                    "anotherProcessVar",
                                    123);
            taskService.setVariableLocal(task.getId(),
                                         "localVar",
                                         "test");

            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().or().taskAssignee(KERMIT).taskVariableValueEquals("localVar",
                                                                                                                                                  "nonExisting").endOr().singleResult();
            assertEquals(0,
                         task.getProcessVariables().size());
            assertEquals(1,
                         task.getTaskLocalVariables().size());
            assertEquals("test",
                         task.getTaskLocalVariables().get("localVar"));

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().or().taskAssignee(KERMIT).taskVariableValueEquals("localVar",
                                                                                                                                                "nonExisting").endOr().singleResult();
            assertEquals(2,
                         task.getProcessVariables().size());
            assertEquals(0,
                         task.getTaskLocalVariables().size());
            assertEquals(true,
                         task.getProcessVariables().get("processVar"));
            assertEquals(123,
                         task.getProcessVariables().get("anotherProcessVar"));

            task = historyService.createHistoricTaskInstanceQuery()
                    .includeTaskLocalVariables()
                    .or()
                    .taskAssignee("nonexisting")
                    .taskVariableValueLike("testVar",
                                           "someVar%")
                    .endOr()
                    .singleResult();
            assertEquals(2,
                         task.getTaskLocalVariables().size());
            assertEquals(0,
                         task.getProcessVariables().size());
            assertEquals("someVariable",
                         task.getTaskLocalVariables().get("testVar"));
            assertEquals(123,
                         task.getTaskLocalVariables().get("testVar2"));

            task = historyService.createHistoricTaskInstanceQuery()
                    .includeTaskLocalVariables()
                    .or()
                    .taskAssignee("nonexisting")
                    .taskVariableValueLikeIgnoreCase("testVar",
                                                     "somevar%")
                    .endOr()
                    .singleResult();
            assertEquals(2,
                         task.getTaskLocalVariables().size());
            assertEquals(0,
                         task.getProcessVariables().size());
            assertEquals("someVariable",
                         task.getTaskLocalVariables().get("testVar"));
            assertEquals(123,
                         task.getTaskLocalVariables().get("testVar2"));

            task = historyService.createHistoricTaskInstanceQuery()
                    .includeTaskLocalVariables()
                    .or()
                    .taskAssignee("nonexisting")
                    .taskVariableValueLike("testVar",
                                           "someVar2%")
                    .endOr()
                    .singleResult();
            assertNull(task);

            tasks = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables()
                    .or()
                    .taskInvolvedUser(KERMIT)
                    .taskVariableValueEquals("localVar",
                                             "nonExisting")
                    .endOr()
                    .orderByTaskCreateTime().asc().list();
            assertEquals(3,
                         tasks.size());
            assertEquals(1,
                         tasks.get(0).getTaskLocalVariables().size());
            assertEquals("test",
                         tasks.get(0).getTaskLocalVariables().get("test"));
            assertEquals(0,
                         tasks.get(0).getProcessVariables().size());

            tasks = historyService.createHistoricTaskInstanceQuery().includeProcessVariables()
                    .or()
                    .taskInvolvedUser(KERMIT)
                    .taskVariableValueEquals("localVar",
                                             "nonExisting")
                    .endOr()
                    .orderByTaskCreateTime().asc().list();
            assertEquals(3,
                         tasks.size());
            assertEquals(0,
                         tasks.get(0).getProcessVariables().size());
            assertEquals(0,
                         tasks.get(0).getTaskLocalVariables().size());

            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().taskAssignee(KERMIT).or().taskVariableValueEquals("localVar",
                                                                                                                                                  "test")
                    .taskVariableValueEquals("localVar",
                                             "nonExisting").endOr().singleResult();
            assertEquals(0,
                         task.getProcessVariables().size());
            assertEquals(1,
                         task.getTaskLocalVariables().size());
            assertEquals("test",
                         task.getTaskLocalVariables().get("localVar"));

            task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().taskAssignee(KERMIT).or().taskVariableValueEquals("localVar",
                                                                                                                                                "test")
                    .taskVariableValueEquals("localVar",
                                             "nonExisting").endOr().singleResult();
            assertEquals(2,
                         task.getProcessVariables().size());
            assertEquals(0,
                         task.getTaskLocalVariables().size());
            assertEquals(true,
                         task.getProcessVariables().get("processVar"));
            assertEquals(123,
                         task.getProcessVariables().get("anotherProcessVar"));

            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().includeProcessVariables().or().taskAssignee(KERMIT).taskVariableValueEquals("localVar",
                                                                                                                                                                            "nonExisting")
                    .endOr().singleResult();
            assertEquals(2,
                         task.getProcessVariables().size());
            assertEquals(1,
                         task.getTaskLocalVariables().size());
            assertEquals("test",
                         task.getTaskLocalVariables().get("localVar"));
            assertEquals(true,
                         task.getProcessVariables().get("processVar"));
            assertEquals(123,
                         task.getProcessVariables().get("anotherProcessVar"));

            task = historyService.createHistoricTaskInstanceQuery().taskAssignee(GONZO).singleResult();
            taskService.complete(task.getId());
            task = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables().or().finished().taskVariableValueEquals("localVar",
                                                                                                                                        "nonExisting").endOr().singleResult();
            variableMap = task.getTaskLocalVariables();
            assertEquals(2,
                         variableMap.size());
            assertEquals(0,
                         task.getProcessVariables().size());
            assertNotNull(variableMap.get("testVar"));
            assertEquals("someVariable",
                         variableMap.get("testVar"));
            assertNotNull(variableMap.get("testVar2"));
            assertEquals(123,
                         variableMap.get("testVar2"));
        }
    }

    @Deployment
    public void testOrQueryMultipleVariableValues() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            Map<String, Object> startMap = new HashMap<String, Object>();
            startMap.put("processVar",
                         true);
            startMap.put("anotherProcessVar",
                         123);
            runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                     startMap);

            startMap.put("anotherProcessVar",
                         999);
            runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                     startMap);

            HistoricTaskInstanceQuery query0 = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().or();
            for (int i = 0; i < 20; i++) {
                query0 = query0.processVariableValueEquals("anotherProcessVar",
                                                           i);
            }
            query0 = query0.endOr();
            assertNull(query0.singleResult());

            HistoricTaskInstanceQuery query1 = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().or().processVariableValueEquals("anotherProcessVar",
                                                                                                                                                          123);
            for (int i = 0; i < 20; i++) {
                query1 = query1.processVariableValueEquals("anotherProcessVar",
                                                           i);
            }
            query1 = query1.endOr();
            HistoricTaskInstance task = query1.singleResult();
            assertEquals(2,
                         task.getProcessVariables().size());
            assertEquals(true,
                         task.getProcessVariables().get("processVar"));
            assertEquals(123,
                         task.getProcessVariables().get("anotherProcessVar"));
        }
    }

    @Deployment
    public void testCandidate() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
            List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT,
                                                                                                                  KERMITSGROUPS).list();
            assertEquals(3,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO,
                                                                                       GONZOSGROUPS).list();
            assertEquals(0,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(FOZZIE,
                                                                                       FOZZIESGROUPS).list();
            assertEquals(1,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateGroup("management").list();
            assertEquals(1,
                         tasks.size());
            List<String> groups = new ArrayList<String>();
            groups.add("management");
            groups.add("accountancy");
            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateGroupIn(groups).list();
            assertEquals(1,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT,
                                                                                       KERMITSGROUPS).taskCandidateGroupIn(groups).list();
            assertEquals(3,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO,
                                                                                       GONZOSGROUPS).taskCandidateGroupIn(groups).list();
            assertEquals(1,
                         tasks.size());

            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
            taskService.complete(task.getId());

            assertEquals(0,
                         taskService.createTaskQuery().processInstanceId(processInstance.getId()).count());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT,
                                                                                       KERMITSGROUPS).list();
            assertEquals(3,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO,
                                                                                       GONZOSGROUPS).list();
            assertEquals(0,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(FOZZIE,
                                                                                       FOZZIESGROUPS).list();
            assertEquals(1,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateGroup("management").list();
            assertEquals(1,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT,
                                                                                       KERMITSGROUPS).taskCandidateGroupIn(Arrays.asList("management")).list();
            assertEquals(3,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO,
                                                                                       GONZOSGROUPS).taskCandidateGroupIn(Arrays.asList("management")).list();
            assertEquals(1,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO,
                                                                                       GONZOSGROUPS).taskCandidateGroupIn(Arrays.asList("invalid")).list();
            assertEquals(0,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateGroupIn(groups).list();
            assertEquals(1,
                         tasks.size());
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
            assertEquals(3,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO).list();
            assertEquals(0,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(FOZZIE).list();
            assertEquals(1,
                         tasks.size());

            List<String> groups = new ArrayList<String>();
            groups.add("management");
            groups.add("accountancy");

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT).taskCandidateGroupIn(groups).list();
            assertEquals(3,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO).taskCandidateGroupIn(groups).list();
            assertEquals(1,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT).list();
            assertEquals(3,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO).list();
            assertEquals(0,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(FOZZIE).list();
            assertEquals(1,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(KERMIT).taskCandidateGroupIn(Arrays.asList("management")).list();
            assertEquals(3,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO).taskCandidateGroupIn(Arrays.asList("management")).list();
            assertEquals(1,
                         tasks.size());

            tasks = historyService.createHistoricTaskInstanceQuery().taskCandidateUser(GONZO).taskCandidateGroupIn(Arrays.asList("invalid")).list();
            assertEquals(0,
                         tasks.size());
        }
    }

    public void testQueryWithPagingAndVariables() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().includeTaskLocalVariables().orderByTaskPriority().desc().listPage(0,
                                                                                                                                                                                            1);
            assertEquals(1,
                         tasks.size());
            HistoricTaskInstance task = tasks.get(0);
            Map<String, Object> variableMap = task.getTaskLocalVariables();
            assertEquals(2,
                         variableMap.size());
            assertEquals("someVariable",
                         variableMap.get("testVar"));
            assertEquals(123,
                         variableMap.get("testVar2"));

            tasks = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().includeTaskLocalVariables().orderByTaskPriority().asc().listPage(1,
                                                                                                                                                                2);
            assertEquals(2,
                         tasks.size());
            task = tasks.get(1);
            variableMap = task.getTaskLocalVariables();
            assertEquals(2,
                         variableMap.size());
            assertEquals("someVariable",
                         variableMap.get("testVar"));
            assertEquals(123,
                         variableMap.get("testVar2"));

            tasks = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().includeTaskLocalVariables().orderByTaskPriority().asc().listPage(2,
                                                                                                                                                                4);
            assertEquals(1,
                         tasks.size());
            task = tasks.get(0);
            variableMap = task.getTaskLocalVariables();
            assertEquals(2,
                         variableMap.size());
            assertEquals("someVariable",
                         variableMap.get("testVar"));
            assertEquals(123,
                         variableMap.get("testVar2"));

            tasks = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().includeTaskLocalVariables().orderByTaskPriority().asc().listPage(4,
                                                                                                                                                                2);
            assertEquals(0,
                         tasks.size());
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
    public void testWithoutDueDateQuery() throws Exception {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
            HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).withoutTaskDueDate().singleResult();
            assertNotNull(historicTask);
            assertNull(historicTask.getDueDate());

            // Set due-date on task
            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
            Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 01:12:13");
            task.setDueDate(dueDate);
            taskService.saveTask(task);

            assertEquals(0,
                         historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).withoutTaskDueDate().count());

            task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

            // Clear due-date on task
            task.setDueDate(null);
            taskService.saveTask(task);

            assertEquals(1,
                         historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).withoutTaskDueDate().count());
        }
    }

    // Unit test for https://activiti.atlassian.net/browse/ACT-4152
    public void testQueryWithIncludeTaskVariableAndTaskCategory() {
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().taskAssignee(GONZO).list();
        for (HistoricTaskInstance task : tasks) {
            assertNotNull(task.getCategory());
            assertEquals("testCategory",
                         task.getCategory());
        }

        tasks = historyService.createHistoricTaskInstanceQuery().taskAssignee(GONZO).includeTaskLocalVariables().list();
        for (HistoricTaskInstance task : tasks) {
            assertNotNull(task.getCategory());
            assertEquals("testCategory",
                         task.getCategory());
        }

        tasks = historyService.createHistoricTaskInstanceQuery().taskAssignee(GONZO).includeProcessVariables().list();
        for (HistoricTaskInstance task : tasks) {
            assertNotNull(task.getCategory());
            assertEquals("testCategory",
                         task.getCategory());
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
