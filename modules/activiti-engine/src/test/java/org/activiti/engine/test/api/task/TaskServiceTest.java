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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 * @author Falko Menge
 */
public class TaskServiceTest extends PluggableActivitiTestCase {

    public void testSaveTaskUpdate() throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        Task task = taskService.newTask();
        task.setDescription("description");
        task.setName("taskname");
        task.setPriority(0);
        task.setAssignee("taskassignee");
        task.setOwner("taskowner");
        Date dueDate = sdf.parse("01/02/2003 04:05:06");
        task.setDueDate(dueDate);
        taskService.saveTask(task);

        // Fetch the task again and update
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertEquals("description", task.getDescription());
        assertEquals("taskname", task.getName());
        assertEquals("taskassignee", task.getAssignee());
        assertEquals("taskowner", task.getOwner());
        assertEquals(dueDate, task.getDueDate());
        assertEquals(0, task.getPriority());

        task.setName("updatedtaskname");
        task.setDescription("updateddescription");
        task.setPriority(1);
        task.setAssignee("updatedassignee");
        task.setOwner("updatedowner");
        dueDate = sdf.parse("01/02/2003 04:05:06");
        task.setDueDate(dueDate);
        taskService.saveTask(task);

        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertEquals("updatedtaskname", task.getName());
        assertEquals("updateddescription", task.getDescription());
        assertEquals("updatedassignee", task.getAssignee());
        assertEquals("updatedowner", task.getOwner());
        assertEquals(dueDate, task.getDueDate());
        assertEquals(1, task.getPriority());

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstance historicTaskInstance = historyService
                    .createHistoricTaskInstanceQuery()
                    .taskId(task.getId())
                    .singleResult();
            assertEquals("updatedtaskname", historicTaskInstance.getName());
            assertEquals("updateddescription", historicTaskInstance.getDescription());
            assertEquals("updatedassignee", historicTaskInstance.getAssignee());
            assertEquals("updatedowner", historicTaskInstance.getOwner());
            assertEquals(dueDate, historicTaskInstance.getDueDate());
            assertEquals(1, historicTaskInstance.getPriority());
        }

        // Finally, delete task
        taskService.deleteTask(task.getId(), true);
    }

    public void testTaskOwner() {
        Task task = taskService.newTask();
        task.setOwner("johndoe");
        taskService.saveTask(task);

        // Fetch the task again and update
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertEquals("johndoe", task.getOwner());

        task.setOwner("joesmoe");
        taskService.saveTask(task);

        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertEquals("joesmoe", task.getOwner());

        // Finally, delete task
        taskService.deleteTask(task.getId(), true);
    }

    public void testTaskComments() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            Task task = taskService.newTask();
            task.setOwner("johndoe");
            taskService.saveTask(task);
            String taskId = task.getId();

            identityService.setAuthenticatedUserId("johndoe");
            // Fetch the task again and update
            taskService.addComment(taskId, null, "look at this \n       isn't this great? slkdjf sldkfjs ldkfjs ldkfjs ldkfj sldkfj sldkfj sldkjg laksfg sdfgsd;flgkj ksajdhf skjdfh ksjdhf skjdhf kalskjgh lskh dfialurhg kajsh dfuieqpgkja rzvkfnjviuqerhogiuvysbegkjz lkhf ais liasduh flaisduh ajiasudh vaisudhv nsfd");
            Comment comment = taskService.getTaskComments(taskId).get(0);
            assertEquals("johndoe", comment.getUserId());
            assertEquals(taskId, comment.getTaskId());
            assertNull(comment.getProcessInstanceId());
            assertEquals("look at this isn't this great? slkdjf sldkfjs ldkfjs ldkfjs ldkfj sldkfj sldkfj sldkjg laksfg sdfgsd;flgkj ksajdhf skjdfh ksjdhf skjdhf kalskjgh lskh dfialurhg ...", ((Event) comment).getMessage());
            assertEquals("look at this \n       isn't this great? slkdjf sldkfjs ldkfjs ldkfjs ldkfj sldkfj sldkfj sldkjg laksfg sdfgsd;flgkj ksajdhf skjdfh ksjdhf skjdhf kalskjgh lskh dfialurhg kajsh dfuieqpgkja rzvkfnjviuqerhogiuvysbegkjz lkhf ais liasduh flaisduh ajiasudh vaisudhv nsfd", comment.getFullMessage());
            assertNotNull(comment.getTime());

            // Finally, delete task
            taskService.deleteTask(taskId, true);
        }
    }

    public void testCustomTaskComments() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            Task task = taskService.newTask();
            task.setOwner("johndoe");
            taskService.saveTask(task);
            String taskId = task.getId();

            identityService.setAuthenticatedUserId("johndoe");
            String customType1 = "Type1";
            String customType2 = "Type2";

            Comment comment = taskService.addComment(taskId, null, "This is a regular comment");
            Comment customComment1 = taskService.addComment(taskId, null, customType1, "This is a custom comment of type Type1");
            Comment customComment2 = taskService.addComment(taskId, null, customType1, "This is another Type1 comment");
            Comment customComment3 = taskService.addComment(taskId, null, customType2, "This is another custom comment. Type2 this time!");

            assertEquals(CommentEntity.TYPE_COMMENT, comment.getType());
            assertEquals(customType1, customComment1.getType());
            assertEquals(customType2, customComment3.getType());

            assertNotNull(taskService.getComment(comment.getId()));
            assertNotNull(taskService.getComment(customComment1.getId()));

            List<Comment> regularComments = taskService.getTaskComments(taskId);
            assertEquals(1, regularComments.size());
            assertEquals("This is a regular comment", regularComments.get(0).getFullMessage());

            List<Event> allComments = taskService.getTaskEvents(taskId);
            assertEquals(4, allComments.size());

            List<Comment> type2Comments = taskService.getCommentsByType(customType2);
            assertEquals(1, type2Comments.size());
            assertEquals("This is another custom comment. Type2 this time!", type2Comments.get(0).getFullMessage());

            List<Comment> taskTypeComments = taskService.getTaskComments(taskId, customType1);
            assertEquals(2, taskTypeComments.size());

            // Clean up
            taskService.deleteTask(taskId, true);
        }
    }

    public void testTaskAttachments() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            Task task = taskService.newTask();
            task.setOwner("johndoe");
            taskService.saveTask(task);
            String taskId = task.getId();
            identityService.setAuthenticatedUserId("johndoe");
            // Fetch the task again and update
            taskService.createAttachment("web page", taskId, null, "weatherforcast", "temperatures and more", "http://weather.com");
            Attachment attachment = taskService.getTaskAttachments(taskId).get(0);
            assertEquals("weatherforcast", attachment.getName());
            assertEquals("temperatures and more", attachment.getDescription());
            assertEquals("web page", attachment.getType());
            assertEquals(taskId, attachment.getTaskId());
            assertNull(attachment.getProcessInstanceId());
            assertEquals("http://weather.com", attachment.getUrl());
            assertNull(taskService.getAttachmentContent(attachment.getId()));

            // Finally, clean up
            taskService.deleteTask(taskId);

            assertEquals(0, taskService.getTaskComments(taskId).size());
            assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskId(taskId).list().size());

            taskService.deleteTask(taskId, true);
        }
    }

    public void testSaveTaskAttachment() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            Task task = taskService.newTask();
            task.setOwner("johndoe");
            taskService.saveTask(task);
            String taskId = task.getId();
            identityService.setAuthenticatedUserId("johndoe");

            // Fetch attachment and update its name
            taskService.createAttachment("web page", taskId, null, "weatherforcast", "temperatures and more", "http://weather.com");
            Attachment attachment = taskService.getTaskAttachments(taskId).get(0);
            attachment.setName("UpdatedName");
            taskService.saveAttachment(attachment);

            // Refetch and verify
            attachment = taskService.getTaskAttachments(taskId).get(0);
            assertEquals("UpdatedName", attachment.getName());

            // Finally, clean up
            taskService.deleteTask(taskId);

            assertEquals(0, taskService.getTaskComments(taskId).size());
            assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskId(taskId).list().size());

            taskService.deleteTask(taskId, true);
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testTaskAttachmentWithProcessInstanceId() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {

            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

            String processInstanceId = processInstance.getId();
            taskService.createAttachment("web page", null, processInstanceId, "weatherforcast", "temperatures and more", "http://weather.com");
            Attachment attachment = taskService.getProcessInstanceAttachments(processInstanceId).get(0);
            assertEquals("weatherforcast", attachment.getName());
            assertEquals("temperatures and more", attachment.getDescription());
            assertEquals("web page", attachment.getType());
            assertEquals(processInstanceId, attachment.getProcessInstanceId());
            assertNull(attachment.getTaskId());
            assertEquals("http://weather.com", attachment.getUrl());
            assertNull(taskService.getAttachmentContent(attachment.getId()));

            // Finally, clean up
            taskService.deleteAttachment(attachment.getId());

            // TODO: Bad API design. Need to fix attachment/comment properly
            ((TaskServiceImpl) taskService).deleteComments(null, processInstanceId);
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testMultipleProcessesStarted() {

        // Start a few  process instances
        for (int i = 0; i < 20; i++) {
            processEngine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess");
        }

        // See if there are tasks for kermit
        List<Task> tasks = processEngine.getTaskService().createTaskQuery().list();
        assertEquals(20, tasks.size());
    }

    public void testTaskDelegation() {
        Task task = taskService.newTask();
        task.setOwner("johndoe");
        task.delegate("joesmoe");
        taskService.saveTask(task);
        String taskId = task.getId();

        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertEquals("johndoe", task.getOwner());
        assertEquals("joesmoe", task.getAssignee());
        assertEquals(DelegationState.PENDING, task.getDelegationState());

        // try to complete (should fail)
        try {
            taskService.complete(task.getId());
            fail();
        } catch (ActivitiException e) {
        }

        taskService.resolveTask(taskId);
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertEquals("johndoe", task.getOwner());
        assertEquals("johndoe", task.getAssignee());
        assertEquals(DelegationState.RESOLVED, task.getDelegationState());

        task.setAssignee(null);
        task.setDelegationState(null);
        taskService.saveTask(task);
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertEquals("johndoe", task.getOwner());
        assertNull(task.getAssignee());
        assertNull(task.getDelegationState());

        task.setAssignee("jackblack");
        task.setDelegationState(DelegationState.RESOLVED);
        taskService.saveTask(task);
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertEquals("johndoe", task.getOwner());
        assertEquals("jackblack", task.getAssignee());
        assertEquals(DelegationState.RESOLVED, task.getDelegationState());

        // Finally, delete task
        taskService.deleteTask(taskId, true);
    }

    public void testTaskDelegationThroughServiceCall() {
        Task task = taskService.newTask();
        task.setOwner("johndoe");
        taskService.saveTask(task);
        String taskId = task.getId();

        // Fetch the task again and update
        task = taskService.createTaskQuery().taskId(taskId).singleResult();

        taskService.delegateTask(task.getId(), "joesmoe");

        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertEquals("johndoe", task.getOwner());
        assertEquals("joesmoe", task.getAssignee());
        assertEquals(DelegationState.PENDING, task.getDelegationState());

        taskService.resolveTask(taskId);

        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertEquals("johndoe", task.getOwner());
        assertEquals("johndoe", task.getAssignee());
        assertEquals(DelegationState.RESOLVED, task.getDelegationState());

        // Finally, delete task
        taskService.deleteTask(taskId, true);
    }

    public void testTaskAssignee() {
        Task task = taskService.newTask();
        task.setAssignee("johndoe");
        taskService.saveTask(task);

        // Fetch the task again and update
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertEquals("johndoe", task.getAssignee());

        task.setAssignee("joesmoe");
        taskService.saveTask(task);

        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertEquals("joesmoe", task.getAssignee());

        // Finally, delete task
        taskService.deleteTask(task.getId(), true);
    }

    public void testSaveTaskNullTask() {
        try {
            taskService.saveTask(null);
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("task is null", ae.getMessage());
        }
    }

    public void testDeleteTaskNullTaskId() {
        try {
            taskService.deleteTask(null);
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            // Expected exception
        }
    }

    public void testDeleteTaskUnexistingTaskId() {
        // Deleting unexisting task should be silently ignored
        taskService.deleteTask("unexistingtaskid");
    }

    public void testDeleteTasksNullTaskIds() {
        try {
            taskService.deleteTasks(null);
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            // Expected exception
        }
    }

    public void testDeleteTasksTaskIdsUnexistingTaskId() {

        Task existingTask = taskService.newTask();
        taskService.saveTask(existingTask);

        // The unexisting taskId's should be silently ignored. Existing task should
        // have been deleted.
        taskService.deleteTasks(Arrays.asList("unexistingtaskid1", existingTask.getId()), true);

        existingTask = taskService.createTaskQuery().taskId(existingTask.getId()).singleResult();
        assertNull(existingTask);
    }

    public void testDeleteTaskIdentityLink() {
        Task task = null;
        try {
            task = taskService.newTask();
            task.setName("test");
            taskService.saveTask(task);

            taskService.addCandidateGroup(task.getId(), "sales");
            taskService.addCandidateUser(task.getId(), "kermit");

            assertNotNull(taskService.createTaskQuery().taskCandidateGroup("sales").singleResult());
            assertNotNull(taskService.createTaskQuery().taskCandidateUser("kermit").singleResult());

            // Delete identity link for group
            taskService.deleteGroupIdentityLink(task.getId(), "sales", "candidate");

            // Link should be removed
            assertNull(taskService.createTaskQuery().taskCandidateGroup("sales").singleResult());

            // User link should remain unaffected
            assertNotNull(taskService.createTaskQuery().taskCandidateUser("kermit").singleResult());
        } finally {
            // Adhoc task not part of deployment, cleanup
            if (task != null && task.getId() != null) {
                taskService.deleteTask(task.getId());
                if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
                    historyService.deleteHistoricTaskInstance(task.getId());
                }
            }
        }
    }

    public void testClaimNullArguments() {
        try {
            taskService.claim(null, "userid");
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    public void testClaimUnexistingTaskId() {
        User user = identityService.newUser("user");
        identityService.saveUser(user);

        try {
            taskService.claim("unexistingtaskid", user.getId());
            fail("ActivitiException expected");
        } catch (ActivitiObjectNotFoundException ae) {
            assertTextPresent("Cannot find task with id unexistingtaskid", ae.getMessage());
            assertEquals(Task.class, ae.getObjectClass());
        }

        identityService.deleteUser(user.getId());
    }

    public void testClaimAlreadyClaimedTaskByOtherUser() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        User user = identityService.newUser("user");
        identityService.saveUser(user);
        User secondUser = identityService.newUser("seconduser");
        identityService.saveUser(secondUser);

        // Claim task the first time
        taskService.claim(task.getId(), user.getId());

        try {
            taskService.claim(task.getId(), secondUser.getId());
            fail("ActivitiException expected");
        } catch (ActivitiTaskAlreadyClaimedException ae) {
            assertTextPresent("Task '" + task.getId() + "' is already claimed by someone else.", ae.getMessage());
        }

        taskService.deleteTask(task.getId(), true);
        identityService.deleteUser(user.getId());
        identityService.deleteUser(secondUser.getId());
    }

    public void testClaimAlreadyClaimedTaskBySameUser() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        User user = identityService.newUser("user");
        identityService.saveUser(user);

        // Claim task the first time
        taskService.claim(task.getId(), user.getId());
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();

        // Claim the task again with the same user. No exception should be thrown
        taskService.claim(task.getId(), user.getId());

        taskService.deleteTask(task.getId(), true);
        identityService.deleteUser(user.getId());
    }

    public void testUnClaimTask() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        User user = identityService.newUser("user");
        identityService.saveUser(user);

        // Claim task the first time
        taskService.claim(task.getId(), user.getId());
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertEquals(user.getId(), task.getAssignee());

        // Unclaim the task
        taskService.unclaim(task.getId());

        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertNull(task.getAssignee());

        taskService.deleteTask(task.getId(), true);
        identityService.deleteUser(user.getId());
    }

    public void testCompleteTaskNullTaskId() {
        try {
            taskService.complete(null);
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    public void testCompleteTaskUnexistingTaskId() {
        try {
            taskService.complete("unexistingtask");
            fail("ActivitiException expected");
        } catch (ActivitiObjectNotFoundException ae) {
            assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
            assertEquals(Task.class, ae.getObjectClass());
        }
    }

    public void testCompleteTaskWithParametersNullTaskId() {
        try {
            taskService.complete(null);
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    public void testCompleteTaskWithParametersUnexistingTaskId() {
        try {
            taskService.complete("unexistingtask");
            fail("ActivitiException expected");
        } catch (ActivitiObjectNotFoundException ae) {
            assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
            assertEquals(Task.class, ae.getObjectClass());
        }
    }

    public void testCompleteTaskWithParametersNullParameters() {
        Task task = taskService.newTask();
        taskService.saveTask(task);

        String taskId = task.getId();
        taskService.complete(taskId, null);

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            historyService.deleteHistoricTaskInstance(taskId);
        }

        // Fetch the task again
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertNull(task);
    }

    @SuppressWarnings("unchecked")
    public void testCompleteTaskWithParametersEmptyParameters() {
        Task task = taskService.newTask();
        taskService.saveTask(task);

        String taskId = task.getId();
        taskService.complete(taskId, Collections.EMPTY_MAP);

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            historyService.deleteHistoricTaskInstance(taskId);
        }

        // Fetch the task again
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertNull(task);
    }

    @Deployment(resources = {
            "org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml"})
    public void testCompleteWithParametersTask() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

        // Fetch first task
        Task task = taskService.createTaskQuery().singleResult();
        assertEquals("First task", task.getName());

        // Complete first task
        Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("myParam", "myValue");
        taskService.complete(task.getId(), taskParams);

        // Fetch second task
        task = taskService.createTaskQuery().singleResult();
        assertEquals("Second task", task.getName());

        // Verify task parameters set on execution
        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
        assertEquals(1, variables.size());
        assertEquals("myValue", variables.get("myParam"));
    }

    @Deployment(resources = {
            "org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml"})
    public void testCompleteWithParametersTask2() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

        // Fetch first task
        Task task = taskService.createTaskQuery().singleResult();
        assertEquals("First task", task.getName());

        // Complete first task
        Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("myParam", "myValue");
        taskService.complete(task.getId(), taskParams, false); // Only difference with previous test

        // Fetch second task
        task = taskService.createTaskQuery().singleResult();
        assertEquals("Second task", task.getName());

        // Verify task parameters set on execution
        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
        assertEquals(1, variables.size());
        assertEquals("myValue", variables.get("myParam"));
    }

    @Deployment
    public void testCompleteWithTaskLocalParameters() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testTaskLocalVars");

        // Fetch first task
        Task task = taskService.createTaskQuery().singleResult();

        // Complete first task
        Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("a", 1);
        taskParams.put("b", 1);
        taskService.complete(task.getId(), taskParams, true);

        // Verify vars are not stored process instance wide
        assertNull(runtimeService.getVariable(processInstance.getId(), "a"));
        assertNull(runtimeService.getVariable(processInstance.getId(), "b"));

        // verify script listener has done its job
        assertEquals(new Integer(2), runtimeService.getVariable(processInstance.getId(), "sum"));

        // Fetch second task
        taskService.createTaskQuery().singleResult();
    }

    public void testSetAssignee() {
        User user = identityService.newUser("user");
        identityService.saveUser(user);

        Task task = taskService.newTask();
        assertNull(task.getAssignee());
        taskService.saveTask(task);

        // Set assignee
        taskService.setAssignee(task.getId(), user.getId());

        // Fetch task again
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertEquals(user.getId(), task.getAssignee());

        // Set assignee to null
        taskService.setAssignee(task.getId(), null);

        identityService.deleteUser(user.getId());
        taskService.deleteTask(task.getId(), true);
    }

    public void testSetAssigneeNullTaskId() {
        try {
            taskService.setAssignee(null, "userId");
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    public void testSetAssigneeUnexistingTask() {
        User user = identityService.newUser("user");
        identityService.saveUser(user);

        try {
            taskService.setAssignee("unexistingTaskId", user.getId());
            fail("ActivitiException expected");
        } catch (ActivitiObjectNotFoundException ae) {
            assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
            assertEquals(Task.class, ae.getObjectClass());
        }

        identityService.deleteUser(user.getId());
    }

    public void testAddCandidateUserDuplicate() {
        // Check behavior when adding the same user twice as candidate
        User user = identityService.newUser("user");
        identityService.saveUser(user);

        Task task = taskService.newTask();
        taskService.saveTask(task);

        taskService.addCandidateUser(task.getId(), user.getId());

        // Add as candidate the second time
        taskService.addCandidateUser(task.getId(), user.getId());

        identityService.deleteUser(user.getId());
        taskService.deleteTask(task.getId(), true);
    }

    public void testAddCandidateUserNullTaskId() {
        try {
            taskService.addCandidateUser(null, "userId");
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    public void testAddCandidateUserNullUserId() {
        try {
            taskService.addCandidateUser("taskId", null);
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
        }
    }

    public void testAddCandidateUserUnexistingTask() {
        User user = identityService.newUser("user");
        identityService.saveUser(user);

        try {
            taskService.addCandidateUser("unexistingTaskId", user.getId());
            fail("ActivitiException expected");
        } catch (ActivitiObjectNotFoundException ae) {
            assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
            assertEquals(Task.class, ae.getObjectClass());
        }

        identityService.deleteUser(user.getId());
    }

    public void testAddCandidateGroupNullTaskId() {
        try {
            taskService.addCandidateGroup(null, "groupId");
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    public void testAddCandidateGroupNullGroupId() {
        try {
            taskService.addCandidateGroup("taskId", null);
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
        }
    }

    public void testAddCandidateGroupUnexistingTask() {
        Group group = identityService.newGroup("group");
        identityService.saveGroup(group);
        try {
            taskService.addCandidateGroup("unexistingTaskId", group.getId());
            fail("ActivitiException expected");
        } catch (ActivitiObjectNotFoundException ae) {
            assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
            assertEquals(Task.class, ae.getObjectClass());
        }
        identityService.deleteGroup(group.getId());
    }

    public void testAddGroupIdentityLinkNullTaskId() {
        try {
            taskService.addGroupIdentityLink(null, "groupId", IdentityLinkType.CANDIDATE);
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    public void testAddGroupIdentityLinkNullUserId() {
        try {
            taskService.addGroupIdentityLink("taskId", null, IdentityLinkType.CANDIDATE);
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
        }
    }

    public void testAddGroupIdentityLinkUnexistingTask() {
        User user = identityService.newUser("user");
        identityService.saveUser(user);

        try {
            taskService.addGroupIdentityLink("unexistingTaskId", user.getId(), IdentityLinkType.CANDIDATE);
            fail("ActivitiException expected");
        } catch (ActivitiObjectNotFoundException ae) {
            assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
            assertEquals(Task.class, ae.getObjectClass());
        }

        identityService.deleteUser(user.getId());
    }

    public void testAddUserIdentityLinkNullTaskId() {
        try {
            taskService.addUserIdentityLink(null, "userId", IdentityLinkType.CANDIDATE);
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    public void testAddUserIdentityLinkNullUserId() {
        try {
            taskService.addUserIdentityLink("taskId", null, IdentityLinkType.CANDIDATE);
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
        }
    }

    public void testAddUserIdentityLinkUnexistingTask() {
        User user = identityService.newUser("user");
        identityService.saveUser(user);

        try {
            taskService.addUserIdentityLink("unexistingTaskId", user.getId(), IdentityLinkType.CANDIDATE);
            fail("ActivitiException expected");
        } catch (ActivitiObjectNotFoundException ae) {
            assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
            assertEquals(Task.class, ae.getObjectClass());
        }

        identityService.deleteUser(user.getId());
    }

    public void testGetIdentityLinksWithCandidateUser() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        String taskId = task.getId();

        identityService.saveUser(identityService.newUser("kermit"));

        taskService.addCandidateUser(taskId, "kermit");
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
        assertEquals(1, identityLinks.size());
        assertEquals("kermit", identityLinks.get(0).getUserId());
        assertNull(identityLinks.get(0).getGroupId());
        assertEquals(IdentityLinkType.CANDIDATE, identityLinks.get(0).getType());

        //cleanup
        taskService.deleteTask(taskId, true);
        identityService.deleteUser("kermit");
    }

    public void testGetIdentityLinksWithCandidateGroup() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        String taskId = task.getId();

        identityService.saveGroup(identityService.newGroup("muppets"));

        taskService.addCandidateGroup(taskId, "muppets");
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
        assertEquals(1, identityLinks.size());
        assertEquals("muppets", identityLinks.get(0).getGroupId());
        assertNull(identityLinks.get(0).getUserId());
        assertEquals(IdentityLinkType.CANDIDATE, identityLinks.get(0).getType());

        //cleanup
        taskService.deleteTask(taskId, true);
        identityService.deleteGroup("muppets");
    }

    public void testGetIdentityLinksWithAssignee() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        String taskId = task.getId();

        identityService.saveUser(identityService.newUser("kermit"));

        taskService.claim(taskId, "kermit");
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
        assertEquals(1, identityLinks.size());
        assertEquals("kermit", identityLinks.get(0).getUserId());
        assertNull(identityLinks.get(0).getGroupId());
        assertEquals(IdentityLinkType.ASSIGNEE, identityLinks.get(0).getType());

        //cleanup
        taskService.deleteTask(taskId, true);
        identityService.deleteUser("kermit");
    }

    public void testGetIdentityLinksWithNonExistingAssignee() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        String taskId = task.getId();

        taskService.claim(taskId, "nonExistingAssignee");
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
        assertEquals(1, identityLinks.size());
        assertEquals("nonExistingAssignee", identityLinks.get(0).getUserId());
        assertNull(identityLinks.get(0).getGroupId());
        assertEquals(IdentityLinkType.ASSIGNEE, identityLinks.get(0).getType());

        //cleanup
        taskService.deleteTask(taskId, true);
    }

    public void testGetIdentityLinksWithOwner() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        String taskId = task.getId();

        identityService.saveUser(identityService.newUser("kermit"));
        identityService.saveUser(identityService.newUser("fozzie"));

        taskService.claim(taskId, "kermit");
        taskService.delegateTask(taskId, "fozzie");

        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
        assertEquals(2, identityLinks.size());

        IdentityLink assignee = identityLinks.get(0);
        assertEquals("fozzie", assignee.getUserId());
        assertNull(assignee.getGroupId());
        assertEquals(IdentityLinkType.ASSIGNEE, assignee.getType());

        IdentityLink owner = identityLinks.get(1);
        assertEquals("kermit", owner.getUserId());
        assertNull(owner.getGroupId());
        assertEquals(IdentityLinkType.OWNER, owner.getType());

        //cleanup
        taskService.deleteTask(taskId, true);
        identityService.deleteUser("kermit");
        identityService.deleteUser("fozzie");
    }

    public void testGetIdentityLinksWithNonExistingOwner() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        String taskId = task.getId();

        taskService.claim(taskId, "nonExistingOwner");
        taskService.delegateTask(taskId, "nonExistingAssignee");
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
        assertEquals(2, identityLinks.size());

        IdentityLink assignee = identityLinks.get(0);
        assertEquals("nonExistingAssignee", assignee.getUserId());
        assertNull(assignee.getGroupId());
        assertEquals(IdentityLinkType.ASSIGNEE, assignee.getType());

        IdentityLink owner = identityLinks.get(1);
        assertEquals("nonExistingOwner", owner.getUserId());
        assertNull(owner.getGroupId());
        assertEquals(IdentityLinkType.OWNER, owner.getType());

        //cleanup
        taskService.deleteTask(taskId, true);
    }

    public void testSetPriority() {
        Task task = taskService.newTask();
        taskService.saveTask(task);

        taskService.setPriority(task.getId(), 12345);

        // Fetch task again to check if the priority is set
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertEquals(12345, task.getPriority());

        taskService.deleteTask(task.getId(), true);
    }

    public void testSetPriorityUnexistingTaskId() {
        try {
            taskService.setPriority("unexistingtask", 12345);
            fail("ActivitiException expected");
        } catch (ActivitiObjectNotFoundException ae) {
            assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
            assertEquals(Task.class, ae.getObjectClass());
        }
    }

    public void testSetPriorityNullTaskId() {
        try {
            taskService.setPriority(null, 12345);
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    public void testSetDueDate() {
        Task task = taskService.newTask();
        taskService.saveTask(task);

        //Set the due date to a non-null value
        Date now = new Date();
        taskService.setDueDate(task.getId(), now);

        // Fetch task to check if the due date was persisted
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertNotNull(task.getDueDate());

        //Set the due date to null
        taskService.setDueDate(task.getId(), null);

        // Re-fetch the task to make sure the due date was set to null
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertNull(task.getDueDate());

        taskService.deleteTask(task.getId(), true);
    }

    public void testSetDueDateUnexistingTaskId() {
        try {
            taskService.setDueDate("unexistingtask", new Date());
            fail("ActivitiException expected");
        } catch (ActivitiException ae) {
            assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
        }
    }

    public void testSetDueDateNullTaskId() {
        try {
            taskService.setDueDate(null, new Date());
            fail("ActivitiException expected");
        } catch (ActivitiException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    /**
     * @see <a href="https://activiti.atlassian.net/browse/ACT-1059">https://activiti.atlassian.net/browse/ACT-1059</a>
     */
    public void testSetDelegationState() {
        Task task = taskService.newTask();
        task.setOwner("wuzh");
        task.delegate("other");
        taskService.saveTask(task);
        String taskId = task.getId();

        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertEquals("wuzh", task.getOwner());
        assertEquals("other", task.getAssignee());
        assertEquals(DelegationState.PENDING, task.getDelegationState());

        task.setDelegationState(DelegationState.RESOLVED);
        taskService.saveTask(task);

        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertEquals("wuzh", task.getOwner());
        assertEquals("other", task.getAssignee());
        assertEquals(DelegationState.RESOLVED, task.getDelegationState());

        taskService.deleteTask(taskId, true);
    }

    private void checkHistoricVariableUpdateEntity(String variableName, String processInstanceId) {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
            boolean deletedVariableUpdateFound = false;

            List<HistoricDetail> resultSet = historyService.createHistoricDetailQuery().processInstanceId(processInstanceId).list();
            for (HistoricDetail currentHistoricDetail : resultSet) {
                assertTrue(currentHistoricDetail instanceof HistoricDetailVariableInstanceUpdateEntity);
                HistoricDetailVariableInstanceUpdateEntity historicVariableUpdate = (HistoricDetailVariableInstanceUpdateEntity) currentHistoricDetail;

                if (historicVariableUpdate.getName().equals(variableName)) {
                    if (historicVariableUpdate.getValue() == null) {
                        if (deletedVariableUpdateFound) {
                            fail("Mismatch: A HistoricVariableUpdateEntity with a null value already found");
                        } else {
                            deletedVariableUpdateFound = true;
                        }
                    }
                }
            }

            assertTrue(deletedVariableUpdateFound);
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testRemoveVariable() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        taskService.setVariable(currentTask.getId(), "variable1", "value1");
        assertEquals("value1", taskService.getVariable(currentTask.getId(), "variable1"));
        assertNull(taskService.getVariableLocal(currentTask.getId(), "variable1"));

        taskService.removeVariable(currentTask.getId(), "variable1");

        assertNull(taskService.getVariable(currentTask.getId(), "variable1"));

        checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
    }

    public void testRemoveVariableNullTaskId() {
        try {
            taskService.removeVariable(null, "variable");
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    @Deployment(resources = {
            "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testRemoveVariables() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        Map<String, Object> varsToDelete = new HashMap<String, Object>();
        varsToDelete.put("variable1", "value1");
        varsToDelete.put("variable2", "value2");
        taskService.setVariables(currentTask.getId(), varsToDelete);
        taskService.setVariable(currentTask.getId(), "variable3", "value3");

        assertEquals("value1", taskService.getVariable(currentTask.getId(), "variable1"));
        assertEquals("value2", taskService.getVariable(currentTask.getId(), "variable2"));
        assertEquals("value3", taskService.getVariable(currentTask.getId(), "variable3"));
        assertNull(taskService.getVariableLocal(currentTask.getId(), "variable1"));
        assertNull(taskService.getVariableLocal(currentTask.getId(), "variable2"));
        assertNull(taskService.getVariableLocal(currentTask.getId(), "variable3"));

        taskService.removeVariables(currentTask.getId(), varsToDelete.keySet());

        assertNull(taskService.getVariable(currentTask.getId(), "variable1"));
        assertNull(taskService.getVariable(currentTask.getId(), "variable2"));
        assertEquals("value3", taskService.getVariable(currentTask.getId(), "variable3"));

        assertNull(taskService.getVariableLocal(currentTask.getId(), "variable1"));
        assertNull(taskService.getVariableLocal(currentTask.getId(), "variable2"));
        assertNull(taskService.getVariableLocal(currentTask.getId(), "variable3"));

        checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
        checkHistoricVariableUpdateEntity("variable2", processInstance.getId());
    }

    @SuppressWarnings("unchecked")
    public void testRemoveVariablesNullTaskId() {
        try {
            taskService.removeVariables(null, Collections.EMPTY_LIST);
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    @Deployment(resources = {
            "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testRemoveVariableLocal() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        taskService.setVariableLocal(currentTask.getId(), "variable1", "value1");
        assertEquals("value1", taskService.getVariable(currentTask.getId(), "variable1"));
        assertEquals("value1", taskService.getVariableLocal(currentTask.getId(), "variable1"));

        taskService.removeVariableLocal(currentTask.getId(), "variable1");

        assertNull(taskService.getVariable(currentTask.getId(), "variable1"));
        assertNull(taskService.getVariableLocal(currentTask.getId(), "variable1"));

        checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
    }

    public void testRemoveVariableLocalNullTaskId() {
        try {
            taskService.removeVariableLocal(null, "variable");
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    @Deployment(resources = {
            "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testRemoveVariablesLocal() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        Map<String, Object> varsToDelete = new HashMap<String, Object>();
        varsToDelete.put("variable1", "value1");
        varsToDelete.put("variable2", "value2");
        taskService.setVariablesLocal(currentTask.getId(), varsToDelete);
        taskService.setVariableLocal(currentTask.getId(), "variable3", "value3");

        assertEquals("value1", taskService.getVariable(currentTask.getId(), "variable1"));
        assertEquals("value2", taskService.getVariable(currentTask.getId(), "variable2"));
        assertEquals("value3", taskService.getVariable(currentTask.getId(), "variable3"));
        assertEquals("value1", taskService.getVariableLocal(currentTask.getId(), "variable1"));
        assertEquals("value2", taskService.getVariableLocal(currentTask.getId(), "variable2"));
        assertEquals("value3", taskService.getVariableLocal(currentTask.getId(), "variable3"));

        taskService.removeVariables(currentTask.getId(), varsToDelete.keySet());

        assertNull(taskService.getVariable(currentTask.getId(), "variable1"));
        assertNull(taskService.getVariable(currentTask.getId(), "variable2"));
        assertEquals("value3", taskService.getVariable(currentTask.getId(), "variable3"));

        assertNull(taskService.getVariableLocal(currentTask.getId(), "variable1"));
        assertNull(taskService.getVariableLocal(currentTask.getId(), "variable2"));
        assertEquals("value3", taskService.getVariableLocal(currentTask.getId(), "variable3"));

        checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
        checkHistoricVariableUpdateEntity("variable2", processInstance.getId());
    }

    @SuppressWarnings("unchecked")
    public void testRemoveVariablesLocalNullTaskId() {
        try {
            taskService.removeVariablesLocal(null, Collections.EMPTY_LIST);
            fail("ActivitiException expected");
        } catch (ActivitiIllegalArgumentException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableByHistoricActivityInstance() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
            assertNotNull(processInstance);
            Task task = taskService.createTaskQuery().singleResult();

            taskService.setVariable(task.getId(), "variable1", "value1");
            taskService.setVariable(task.getId(), "variable1", "value2");

            HistoricActivityInstance historicActivitiInstance = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId())
                    .activityId("theTask").singleResult();
            assertNotNull(historicActivitiInstance);

            List<HistoricDetail> resultSet = historyService.createHistoricDetailQuery().variableUpdates().activityInstanceId(historicActivitiInstance.getId())
                    .orderByTime().asc().list();

            assertEquals(2, resultSet.size());
            assertEquals("variable1", ((HistoricVariableUpdate) resultSet.get(0)).getVariableName());
            assertEquals("value1", ((HistoricVariableUpdate) resultSet.get(0)).getValue());
            assertEquals("variable1", ((HistoricVariableUpdate) resultSet.get(1)).getVariableName());
            assertEquals("value2", ((HistoricVariableUpdate) resultSet.get(1)).getValue());
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testUserTaskOptimisticLocking() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task task1 = taskService.createTaskQuery().singleResult();
        Task task2 = taskService.createTaskQuery().singleResult();

        task1.setDescription("test description one");
        taskService.saveTask(task1);

        try {
            task2.setDescription("test description two");
            taskService.saveTask(task2);

            fail("Expecting exception");
        } catch (ActivitiOptimisticLockingException e) {
            // Expected exception
        }
    }

    public void testDeleteTaskWithDeleteReason() {
        // ACT-900: deleteReason can be manually specified - can only be validated when historyLevel > ACTIVITY
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {

            Task task = taskService.newTask();
            task.setName("test task");
            taskService.saveTask(task);

            assertNotNull(task.getId());

            taskService.deleteTask(task.getId(), "deleted for testing purposes");

            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                    .taskId(task.getId()).singleResult();

            assertNotNull(historicTaskInstance);
            assertEquals("deleted for testing purposes", historicTaskInstance.getDeleteReason());

            // Delete historic task that is left behind, will not be cleaned up because this is not part of a process
            taskService.deleteTask(task.getId(), true);
        }
    }

    public void testResolveTaskNullTaskId() {
        try {
            taskService.resolveTask(null);
            fail();
        } catch (ActivitiException ae) {
            assertTextPresent("taskId is null", ae.getMessage());
        }
    }

    public void testResolveTaskUnexistingTaskId() {
        try {
            taskService.resolveTask("blergh");
            fail();
        } catch (ActivitiException ae) {
            assertTextPresent("Cannot find task with id", ae.getMessage());
        }
    }

    public void testResolveTaskWithParametersNullParameters() {
        Task task = taskService.newTask();
        task.setDelegationState(DelegationState.PENDING);
        taskService.saveTask(task);

        String taskId = task.getId();
        taskService.resolveTask(taskId, null);

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            historyService.deleteHistoricTaskInstance(taskId);
        }

        // Fetch the task again
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertEquals(DelegationState.RESOLVED, task.getDelegationState());

        taskService.deleteTask(taskId, true);
    }

    @SuppressWarnings("unchecked")
    public void testResolveTaskWithParametersEmptyParameters() {
        Task task = taskService.newTask();
        task.setDelegationState(DelegationState.PENDING);
        taskService.saveTask(task);

        String taskId = task.getId();
        taskService.resolveTask(taskId, Collections.EMPTY_MAP);

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            historyService.deleteHistoricTaskInstance(taskId);
        }

        // Fetch the task again
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertEquals(DelegationState.RESOLVED, task.getDelegationState());

        taskService.deleteTask(taskId, true);
    }

    @Deployment(resources = {"org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml"})
    public void testResolveWithParametersTask() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

        // Fetch first task
        Task task = taskService.createTaskQuery().singleResult();
        assertEquals("First task", task.getName());

        task.delegate("johndoe");

        // Resolve first task
        Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("myParam", "myValue");
        taskService.resolveTask(task.getId(), taskParams);

        // Verify that task is resolved
        task = taskService.createTaskQuery().taskDelegationState(DelegationState.RESOLVED).singleResult();
        assertEquals("First task", task.getName());

        // Verify task parameters set on execution
        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
        assertEquals(1, variables.size());
        assertEquals("myValue", variables.get("myParam"));
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testDeleteTaskPartOfProcess() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");
        Task task = taskService.createTaskQuery().singleResult();
        assertNotNull(task);

        try {
            taskService.deleteTask(task.getId());
        } catch (ActivitiException ae) {
            assertEquals("The task cannot be deleted because is part of a running process", ae.getMessage());
        }

        try {
            taskService.deleteTask(task.getId(), true);
        } catch (ActivitiException ae) {
            assertEquals("The task cannot be deleted because is part of a running process", ae.getMessage());
        }

        try {
            taskService.deleteTask(task.getId(), "test");
        } catch (ActivitiException ae) {
            assertEquals("The task cannot be deleted because is part of a running process", ae.getMessage());
        }

        try {
            taskService.deleteTasks(Arrays.asList(task.getId()));
        } catch (ActivitiException ae) {
            assertEquals("The task cannot be deleted because is part of a running process", ae.getMessage());
        }

        try {
            taskService.deleteTasks(Arrays.asList(task.getId()), true);
        } catch (ActivitiException ae) {
            assertEquals("The task cannot be deleted because is part of a running process", ae.getMessage());
        }

        try {
            taskService.deleteTasks(Arrays.asList(task.getId()), "test");
        } catch (ActivitiException ae) {
            assertEquals("The task cannot be deleted because is part of a running process", ae.getMessage());
        }
    }

    @Deployment
    public void testFormKeyExpression() {
        runtimeService.startProcessInstanceByKey("testFormExpression", CollectionUtil.singletonMap("var", "abc"));

        Task task = taskService.createTaskQuery().singleResult();
        assertEquals("first-form.json", task.getFormKey());
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().singleResult();
        assertEquals("form-abc.json", task.getFormKey());

        task.setFormKey("form-changed.json");
        taskService.saveTask(task);
        task = taskService.createTaskQuery().singleResult();
        assertEquals("form-changed.json", task.getFormKey());

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
            assertEquals("form-changed.json", historicTaskInstance.getFormKey());
        }
    }

    @Deployment(resources = {
            "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableLocalWithCast() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        taskService.setVariableLocal(currentTask.getId(), "variable1", "value1");

        String variable = taskService.getVariableLocal(currentTask.getId(), "variable1", String.class);

        assertEquals("value1", variable);
    }

    @Deployment(resources = {
            "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableLocalNotExistingWithCast() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        String variable = taskService.getVariableLocal(currentTask.getId(), "variable1", String.class);

        assertNull(variable);
    }

    @Deployment(resources = {
            "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableLocalWithInvalidCast() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        taskService.setVariableLocal(currentTask.getId(), "variable1", "value1");

        try {
            taskService.getVariableLocal(currentTask.getId(), "variable1", Boolean.class);
            fail("should have thrown a ClassCastException");
        } catch (ClassCastException e) {
        }
    }

    @Deployment(resources = {
            "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableWithCast() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        taskService.setVariable(currentTask.getId(), "variable1", "value1");

        String variable = taskService.getVariable(currentTask.getId(), "variable1", String.class);

        assertEquals("value1", variable);
    }

    @Deployment(resources = {
            "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableNotExistingWithCast() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        String variable = taskService.getVariable(currentTask.getId(), "variable1", String.class);

        assertNull(variable);
    }

    @Deployment(resources = {
            "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableWithInvalidCast() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        taskService.setVariable(currentTask.getId(), "variable1", "value1");

        try {
            taskService.getVariable(currentTask.getId(), "variable1", Boolean.class);
            fail("should have thrown a ClassCastException");
        } catch (ClassCastException e) {
        }
    }
}
