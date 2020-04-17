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
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.text.SimpleDateFormat;
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
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

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
        assertThat(task.getDescription()).isEqualTo("description");
        assertThat(task.getName()).isEqualTo("taskname");
        assertThat(task.getAssignee()).isEqualTo("taskassignee");
        assertThat(task.getOwner()).isEqualTo("taskowner");
        assertThat(task.getDueDate()).isEqualTo(dueDate);
        assertThat(task.getPriority()).isEqualTo(0);

        task.setName("updatedtaskname");
        task.setDescription("updateddescription");
        task.setPriority(1);
        task.setAssignee("updatedassignee");
        task.setOwner("updatedowner");
        dueDate = sdf.parse("01/02/2003 04:05:06");
        task.setDueDate(dueDate);
        taskService.saveTask(task);

        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertThat(task.getName()).isEqualTo("updatedtaskname");
        assertThat(task.getDescription()).isEqualTo("updateddescription");
        assertThat(task.getAssignee()).isEqualTo("updatedassignee");
        assertThat(task.getOwner()).isEqualTo("updatedowner");
        assertThat(task.getDueDate()).isEqualTo(dueDate);
        assertThat(task.getPriority()).isEqualTo(1);

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
            assertThat(historicTaskInstance.getName()).isEqualTo("updatedtaskname");
            assertThat(historicTaskInstance.getDescription()).isEqualTo("updateddescription");
            assertThat(historicTaskInstance.getAssignee()).isEqualTo("updatedassignee");
            assertThat(historicTaskInstance.getOwner()).isEqualTo("updatedowner");
            assertThat(historicTaskInstance.getDueDate()).isEqualTo(dueDate);
            assertThat(historicTaskInstance.getPriority()).isEqualTo(1);
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
        assertThat(task.getOwner()).isEqualTo("johndoe");

        task.setOwner("joesmoe");
        taskService.saveTask(task);

        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertThat(task.getOwner()).isEqualTo("joesmoe");

        // Finally, delete task
        taskService.deleteTask(task.getId(), true);
    }

    public void testTaskComments() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            Task task = taskService.newTask();
            task.setOwner("johndoe");
            taskService.saveTask(task);
            String taskId = task.getId();

            Authentication.setAuthenticatedUserId("johndoe");
            // Fetch the task again and update
            taskService
                    .addComment(
                            taskId,
                            null,
                            "look at this \n       isn't this great? slkdjf sldkfjs ldkfjs ldkfjs ldkfj sldkfj sldkfj sldkjg laksfg sdfgsd;flgkj ksajdhf skjdfh ksjdhf skjdhf kalskjgh lskh dfialurhg kajsh dfuieqpgkja rzvkfnjviuqerhogiuvysbegkjz lkhf ais liasduh flaisduh ajiasudh vaisudhv nsfd");
            Comment comment = taskService.getTaskComments(taskId).get(0);
            assertThat(comment.getUserId()).isEqualTo("johndoe");
            assertThat(comment.getTaskId()).isEqualTo(taskId);
            assertThat(comment.getProcessInstanceId()).isNull();
            assertThat(((Event) comment).getMessage())
                .isEqualTo("look at this isn't this great? slkdjf sldkfjs ldkfjs ldkfjs ldkfj sldkfj sldkfj sldkjg laksfg sdfgsd;flgkj ksajdhf skjdfh ksjdhf skjdhf kalskjgh lskh dfialurhg ...");
            assertThat(comment.getFullMessage())
                .isEqualTo("look at this \n       isn't this great? slkdjf sldkfjs ldkfjs ldkfjs ldkfj sldkfj sldkfj sldkjg laksfg sdfgsd;flgkj ksajdhf skjdfh ksjdhf skjdhf kalskjgh lskh dfialurhg kajsh dfuieqpgkja rzvkfnjviuqerhogiuvysbegkjz lkhf ais liasduh flaisduh ajiasudh vaisudhv nsfd");
            assertThat(comment.getTime()).isNotNull();

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

            Authentication.setAuthenticatedUserId("johndoe");
            String customType1 = "Type1";
            String customType2 = "Type2";

            Comment comment = taskService.addComment(taskId,
                                                     null,
                                                     "This is a regular comment");
            Comment customComment1 = taskService.addComment(taskId,
                                                            null,
                                                            customType1,
                                                            "This is a custom comment of type Type1");
            Comment customComment2 = taskService.addComment(taskId,
                                                            null,
                                                            customType1,
                                                            "This is another Type1 comment");
            Comment customComment3 = taskService.addComment(taskId,
                                                            null,
                                                            customType2,
                                                            "This is another custom comment. Type2 this time!");

            assertThat(comment.getType()).isEqualTo(CommentEntity.TYPE_COMMENT);
            assertThat(customComment1.getType()).isEqualTo(customType1);
            assertThat(customComment3.getType()).isEqualTo(customType2);

            assertThat(taskService.getComment(comment.getId())).isNotNull();
            assertThat(taskService.getComment(customComment1.getId())).isNotNull();

            List<Comment> regularComments = taskService.getTaskComments(taskId);
            assertThat(regularComments).hasSize(1);
            assertThat(regularComments.get(0).getFullMessage()).isEqualTo("This is a regular comment");

            List<Event> allComments = taskService.getTaskEvents(taskId);
            assertThat(allComments).hasSize(4);

            List<Comment> type2Comments = taskService.getCommentsByType(customType2);
            assertThat(type2Comments).hasSize(1);
            assertThat(type2Comments.get(0).getFullMessage()).isEqualTo("This is another custom comment. Type2 this time!");

            List<Comment> taskTypeComments = taskService.getTaskComments(taskId, customType1);
            assertThat(taskTypeComments).hasSize(2);

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
            Authentication.setAuthenticatedUserId("johndoe");
            // Fetch the task again and update
            taskService.createAttachment("web page",
                                         taskId,
                                         null,
                                         "weatherforcast",
                                         "temperatures and more",
                                         "http://weather.com");
            Attachment attachment = taskService.getTaskAttachments(taskId).get(0);
            assertThat(attachment.getName()).isEqualTo("weatherforcast");
            assertThat(attachment.getDescription()).isEqualTo("temperatures and more");
            assertThat(attachment.getType()).isEqualTo("web page");
            assertThat(attachment.getTaskId()).isEqualTo(taskId);
            assertThat(attachment.getProcessInstanceId()).isNull();
            assertThat(attachment.getUrl()).isEqualTo("http://weather.com");
            assertThat(taskService.getAttachmentContent(attachment.getId())).isNull();

            // Finally, clean up
            taskService.deleteTask(taskId);

            assertThat(taskService.getTaskComments(taskId)).hasSize(0);
            assertThat(historyService.createHistoricTaskInstanceQuery().taskId(taskId).list()).hasSize(1);

            taskService.deleteTask(taskId,
                                   true);
        }
    }

    public void testSaveTaskAttachment() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            Task task = taskService.newTask();
            task.setOwner("johndoe");
            taskService.saveTask(task);
            String taskId = task.getId();
            Authentication.setAuthenticatedUserId("johndoe");

            // Fetch attachment and update its name
            taskService.createAttachment("web page",
                                         taskId,
                                         null,
                                         "weatherforcast",
                                         "temperatures and more",
                                         "http://weather.com");
            Attachment attachment = taskService.getTaskAttachments(taskId).get(0);
            attachment.setName("UpdatedName");
            taskService.saveAttachment(attachment);

            // Refetch and verify
            attachment = taskService.getTaskAttachments(taskId).get(0);
            assertThat(attachment.getName()).isEqualTo("UpdatedName");

            // Finally, clean up
            taskService.deleteTask(taskId);

            assertThat(taskService.getTaskComments(taskId)).hasSize(0);
            assertThat(historyService.createHistoricTaskInstanceQuery().taskId(taskId).list()).hasSize(1);

            taskService.deleteTask(taskId,
                                   true);
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testTaskAttachmentWithProcessInstanceId() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {

            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

            String processInstanceId = processInstance.getId();
            taskService.createAttachment("web page",
                                         null,
                                         processInstanceId,
                                         "weatherforcast",
                                         "temperatures and more",
                                         "http://weather.com");
            Attachment attachment = taskService.getProcessInstanceAttachments(processInstanceId).get(0);
            assertThat(attachment.getName()).isEqualTo("weatherforcast");
            assertThat(attachment.getDescription()).isEqualTo("temperatures and more");
            assertThat(attachment.getType()).isEqualTo("web page");
            assertThat(attachment.getProcessInstanceId()).isEqualTo(processInstanceId);
            assertThat(attachment.getTaskId()).isNull();
            assertThat(attachment.getUrl()).isEqualTo("http://weather.com");
            assertThat(taskService.getAttachmentContent(attachment.getId())).isNull();

            // Finally, clean up
            taskService.deleteAttachment(attachment.getId());

            // TODO: Bad API design. Need to fix attachment/comment properly
            ((TaskServiceImpl) taskService).deleteComments(null,
                                                           processInstanceId);
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testMultipleProcessesStarted() {

        // Start a few process instances
        for (int i = 0; i < 20; i++) {
            processEngine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess");
        }

        // See if there are tasks for kermit
        List<Task> tasks = processEngine.getTaskService().createTaskQuery().list();
        assertThat(tasks).hasSize(20);
    }

    public void testTaskDelegation() {
        Task task = taskService.newTask();
        task.setOwner("johndoe");
        taskService.saveTask(task);
        taskService.delegateTask(task.getId(), "joesmoe");
        String taskId = task.getId();

        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertThat(task.getOwner()).isEqualTo("johndoe");
        assertThat(task.getAssignee()).isEqualTo("joesmoe");
        assertThat(task.getDelegationState()).isEqualTo(DelegationState.PENDING);

        // try to complete (should fail)
        Task exceptionTask = task;
        assertThatExceptionOfType(ActivitiException.class)
            .isThrownBy(() -> taskService.complete(exceptionTask.getId()));

        taskService.resolveTask(taskId);
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertThat(task.getOwner()).isEqualTo("johndoe");
        assertThat(task.getAssignee()).isEqualTo("johndoe");
        assertThat(task.getDelegationState()).isEqualTo(DelegationState.RESOLVED);

        task.setAssignee(null);
        task.setDelegationState(null);
        taskService.saveTask(task);
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertThat(task.getOwner()).isEqualTo("johndoe");
        assertThat(task.getAssignee()).isNull();
        assertThat(task.getDelegationState()).isNull();

        task.setAssignee("jackblack");
        task.setDelegationState(DelegationState.RESOLVED);
        taskService.saveTask(task);
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertThat(task.getOwner()).isEqualTo("johndoe");
        assertThat(task.getAssignee()).isEqualTo("jackblack");
        assertThat(task.getDelegationState()).isEqualTo(DelegationState.RESOLVED);

        // Finally, delete task
        taskService.deleteTask(taskId,
                               true);
    }

    public void testTaskDelegationThroughServiceCall() {
        Task task = taskService.newTask();
        task.setOwner("johndoe");
        taskService.saveTask(task);
        String taskId = task.getId();

        // Fetch the task again and update
        task = taskService.createTaskQuery().taskId(taskId).singleResult();

        taskService.delegateTask(task.getId(),
                                 "joesmoe");

        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertThat(task.getOwner()).isEqualTo("johndoe");
        assertThat(task.getAssignee()).isEqualTo("joesmoe");
        assertThat(task.getDelegationState()).isEqualTo(DelegationState.PENDING);

        taskService.resolveTask(taskId);

        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertThat(task.getOwner()).isEqualTo("johndoe");
        assertThat(task.getAssignee()).isEqualTo("johndoe");
        assertThat(task.getDelegationState()).isEqualTo(DelegationState.RESOLVED);

        // Finally, delete task
        taskService.deleteTask(taskId,
                               true);
    }

    public void testTaskAssignee() {
        Task task = taskService.newTask();
        task.setAssignee("johndoe");
        taskService.saveTask(task);

        // Fetch the task again and update
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertThat(task.getAssignee()).isEqualTo("johndoe");

        task.setAssignee("joesmoe");
        taskService.saveTask(task);

        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertThat(task.getAssignee()).isEqualTo("joesmoe");

        // Finally, delete task
        taskService.deleteTask(task.getId(),
                               true);
    }

    public void testSaveTaskNullTask() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.saveTask(null))
            .withMessageContaining("task is null");
    }

    public void testDeleteTaskNullTaskId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.deleteTask(null));
    }

    public void testDeleteTaskUnexistingTaskId() {
        // Deleting unexisting task should be silently ignored
        taskService.deleteTask("unexistingtaskid");
    }

    public void testDeleteTasksNullTaskIds() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.deleteTasks(null));
    }

    public void testDeleteTasksTaskIdsUnexistingTaskId() {

        Task existingTask = taskService.newTask();
        taskService.saveTask(existingTask);

        // The unexisting taskId's should be silently ignored. Existing task should have been deleted.
        taskService.deleteTasks(asList("unexistingtaskid1", existingTask.getId()), true);

        existingTask = taskService.createTaskQuery().taskId(existingTask.getId()).singleResult();
        assertThat(existingTask).isNull();
    }

    public void testDeleteTaskIdentityLink() {
        Task task = null;
        try {
            task = taskService.newTask();
            task.setName("test");
            taskService.saveTask(task);

            taskService.addCandidateGroup(task.getId(),
                                          "sales");
            taskService.addCandidateUser(task.getId(),
                                         "kermit");

            assertThat(taskService.createTaskQuery().taskCandidateGroup("sales").singleResult()).isNotNull();
            assertThat(taskService.createTaskQuery().taskCandidateUser("kermit").singleResult()).isNotNull();

            // Delete identity link for group
            taskService.deleteGroupIdentityLink(task.getId(),
                                                "sales",
                                                "candidate");

            // Link should be removed
            assertThat(taskService.createTaskQuery().taskCandidateGroup("sales").singleResult()).isNull();

            // User link should remain unaffected
            assertThat(taskService.createTaskQuery().taskCandidateUser("kermit").singleResult()).isNotNull();
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
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.claim(null, "userid"))
            .withMessageContaining("taskId is null");
    }

    public void testClaimUnexistingTaskId() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> taskService.claim("unexistingtaskid", "user"))
            .withMessageContaining("Cannot find task with id unexistingtaskid")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Task.class));
    }

    public void testClaimAlreadyClaimedTaskByOtherUser() {
        Task task = taskService.newTask();
        taskService.saveTask(task);

        // Claim task the first time
        taskService.claim(task.getId(), "firstuser");

        assertThatExceptionOfType(ActivitiTaskAlreadyClaimedException.class)
            .isThrownBy(() -> taskService.claim(task.getId(), "seconduser"))
            .withMessageContaining("Task '" + task.getId() + "' is already claimed by someone else.");

        taskService.deleteTask(task.getId(), true);
    }

    public void testClaimAlreadyClaimedTaskBySameUser() {
        Task task = taskService.newTask();
        taskService.saveTask(task);

        // Claim task the first time
        taskService.claim(task.getId(), "user");
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();

        // Claim the task again with the same user. No exception should be thrown
        taskService.claim(task.getId(), "user");

        taskService.deleteTask(task.getId(), true);
    }

    public void testUnClaimTask() {
        Task task = taskService.newTask();
        taskService.saveTask(task);

        // Claim task the first time
        taskService.claim(task.getId(), "user");
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertThat(task.getAssignee()).isEqualTo("user");

        // Unclaim the task
        taskService.unclaim(task.getId());

        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertThat(task.getAssignee()).isNull();

        taskService.deleteTask(task.getId(), true);
    }

    public void testCompleteTaskNullTaskId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.complete(null))
            .withMessageContaining("taskId is null");
    }

    public void testCompleteTaskUnexistingTaskId() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> taskService.complete("unexistingtask"))
            .withMessageContaining("Cannot find task with id unexistingtask")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Task.class));
    }

    public void testCompleteTaskWithParametersNullTaskId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.complete(null))
            .withMessageContaining("taskId is null");
    }

    public void testCompleteTaskWithParametersUnexistingTaskId() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> taskService.complete("unexistingtask"))
            .withMessageContaining("Cannot find task with id unexistingtask")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Task.class));
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
        assertThat(task).isNull();
    }

    @SuppressWarnings("unchecked")
    public void testCompleteTaskWithParametersEmptyParameters() {
        Task task = taskService.newTask();
        taskService.saveTask(task);

        String taskId = task.getId();
        taskService.complete(taskId,
                             Collections.EMPTY_MAP);

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            historyService.deleteHistoricTaskInstance(taskId);
        }

        // Fetch the task again
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertThat(task).isNull();
    }

    @Deployment(resources = {"org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml"})
    public void testCompleteWithParametersTask() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

        // Fetch first task
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task.getName()).isEqualTo("First task");

        // Complete first task
        Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("myParam",
                       "myValue");
        taskService.complete(task.getId(),
                             taskParams);

        // Fetch second task
        task = taskService.createTaskQuery().singleResult();
        assertThat(task.getName()).isEqualTo("Second task");

        // Verify task parameters set on execution
        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
        assertThat(variables).hasSize(1);
        assertThat(variables.get("myParam")).isEqualTo("myValue");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml"})
    public void testCompleteWithParametersTask2() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

        // Fetch first task
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task.getName()).isEqualTo("First task");

        // Complete first task
        Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("myParam", "myValue");
        taskService.complete(task.getId(), taskParams, false); // Only
        // difference with previous test

        // Fetch second task
        task = taskService.createTaskQuery().singleResult();
        assertThat(task.getName()).isEqualTo("Second task");

        // Verify task parameters set on execution
        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
        assertThat(variables).hasSize(1);
        assertThat(variables.get("myParam")).isEqualTo("myValue");
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
        assertThat(runtimeService.getVariable(processInstance.getId(), "a")).isNull();
        assertThat(runtimeService.getVariable(processInstance.getId(), "b")).isNull();

        // verify script listener has done its job
        assertThat(runtimeService.getVariable(processInstance.getId(), "sum")).isEqualTo(Integer.valueOf(2));

        // Fetch second task
        taskService.createTaskQuery().singleResult();
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskWithFormKeyProcess.bpmn20.xml"})
    public void testCompleteTaskWithFormKey() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskWithFormProcess");

        // Fetch task
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getName()).isEqualTo("my task");
        assertThat(task.getFormKey()).isEqualTo("myFormKey");
        assertThat(task.getAssignee()).isEqualTo("myAssignee");
        assertThat(task.getOwner()).isEqualTo("myOwner");
        assertThat(task.getCategory()).isEqualTo("myCategory");
        assertThat(task.getPriority()).isEqualTo(60);
        assertThat(task.getDueDate()).isNotNull();

        // Complete task
        taskService.complete(task.getId());

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
            assertThat(historicTask.getName()).isEqualTo("my task");
            assertThat(historicTask.getFormKey()).isEqualTo("myFormKey");
            assertThat(historicTask.getAssignee()).isEqualTo("myAssignee");
            assertThat(historicTask.getOwner()).isEqualTo("myOwner");
            assertThat(historicTask.getCategory()).isEqualTo("myCategory");
            assertThat(historicTask.getPriority()).isEqualTo(60);
            assertThat(historicTask.getDueDate()).isNotNull();
        }
    }

    public void testSetAssignee() {

        Task task = taskService.newTask();
        assertThat(task.getAssignee()).isNull();
        taskService.saveTask(task);

        // Set assignee
        taskService.setAssignee(task.getId(), "user");

        // Fetch task again
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertThat(task.getAssignee()).isEqualTo("user");

        // Set assignee to null
        taskService.setAssignee(task.getId(), null);

        taskService.deleteTask(task.getId(), true);
    }

    public void testSetAssigneeNullTaskId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.setAssignee(null, "userId"))
            .withMessageContaining("taskId is null");
    }

    public void testSetAssigneeUnexistingTask() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> taskService.setAssignee("unexistingTaskId", "user"))
            .withMessageContaining("Cannot find task with id unexistingTaskId")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Task.class));
    }

    public void testAddCandidateUserDuplicate() {
        // Check behavior when adding the same user twice as candidate

        Task task = taskService.newTask();
        taskService.saveTask(task);

        taskService.addCandidateUser(task.getId(), "user");

        // Add as candidate the second time
        taskService.addCandidateUser(task.getId(), "user");

        taskService.deleteTask(task.getId(), true);
    }

    public void testAddCandidateUserNullTaskId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.addCandidateUser(null, "userId"))
            .withMessageContaining("taskId is null");
    }

    public void testAddCandidateUserNullUserId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.addCandidateUser("taskId", null))
            .withMessageContaining("identityId is null");
    }

    public void testAddCandidateUserUnexistingTask() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> taskService.addCandidateUser("unexistingTaskId", "user"))
            .withMessageContaining("Cannot find task with id unexistingTaskId")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Task.class));
    }

    public void testAddCandidateGroupNullTaskId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.addCandidateGroup(null, "groupId"))
            .withMessageContaining("taskId is null");
    }

    public void testAddCandidateGroupNullGroupId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.addCandidateGroup("taskId", null))
            .withMessageContaining("identityId is null");
    }

    public void testAddCandidateGroupUnexistingTask() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> taskService.addCandidateGroup("unexistingTaskId", "group"))
            .withMessageContaining("Cannot find task with id unexistingTaskId")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Task.class));
    }

    public void testAddGroupIdentityLinkNullTaskId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.addGroupIdentityLink(null, "groupId", IdentityLinkType.CANDIDATE))
            .withMessageContaining("taskId is null");
    }

    public void testAddGroupIdentityLinkNullUserId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.addGroupIdentityLink("taskId", null, IdentityLinkType.CANDIDATE))
            .withMessageContaining("identityId is null");
    }

    public void testAddGroupIdentityLinkUnexistingTask() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> taskService.addGroupIdentityLink("unexistingTaskId", "user", IdentityLinkType.CANDIDATE))
            .withMessageContaining("Cannot find task with id unexistingTaskId")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Task.class));
    }

    public void testAddUserIdentityLinkNullTaskId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.addUserIdentityLink(null, "userId", IdentityLinkType.CANDIDATE))
            .withMessageContaining("taskId is null");
    }

    public void testAddUserIdentityLinkNullUserId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.addUserIdentityLink("taskId", null, IdentityLinkType.CANDIDATE))
            .withMessageContaining("identityId is null");
    }

    public void testAddUserIdentityLinkUnexistingTask() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> taskService.addUserIdentityLink("unexistingTaskId", "user", IdentityLinkType.CANDIDATE))
            .withMessageContaining("Cannot find task with id unexistingTaskId")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Task.class));
    }

    public void testGetIdentityLinksWithCandidateUser() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        String taskId = task.getId();

        taskService.addCandidateUser(taskId, "kermit");
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
        assertThat(identityLinks).hasSize(1);
        assertThat(identityLinks.get(0).getUserId()).isEqualTo("kermit");
        assertThat(identityLinks.get(0).getGroupId()).isNull();
        assertThat(identityLinks.get(0).getType()).isEqualTo(IdentityLinkType.CANDIDATE);

        // cleanup
        taskService.deleteTask(taskId, true);
    }

    public void testGetIdentityLinksWithCandidateGroup() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        String taskId = task.getId();

        taskService.addCandidateGroup(taskId,
                                      "muppets");
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
        assertThat(identityLinks).hasSize(1);
        assertThat(identityLinks.get(0).getGroupId()).isEqualTo("muppets");
        assertThat(identityLinks.get(0).getUserId()).isNull();
        assertThat(identityLinks.get(0).getType()).isEqualTo(IdentityLinkType.CANDIDATE);

        // cleanup
        taskService.deleteTask(taskId,
                               true);
    }

    public void testGetIdentityLinksWithAssignee() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        String taskId = task.getId();

        taskService.claim(taskId,
                          "kermit");
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
        assertThat(identityLinks).hasSize(1);
        assertThat(identityLinks.get(0).getUserId()).isEqualTo("kermit");
        assertThat(identityLinks.get(0).getGroupId()).isNull();
        assertThat(identityLinks.get(0).getType()).isEqualTo(IdentityLinkType.ASSIGNEE);

        // cleanup
        taskService.deleteTask(taskId,
                               true);
    }

    public void testGetIdentityLinksWithNonExistingAssignee() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        String taskId = task.getId();

        taskService.claim(taskId,
                          "nonExistingAssignee");
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
        assertThat(identityLinks).hasSize(1);
        assertThat(identityLinks.get(0).getUserId()).isEqualTo("nonExistingAssignee");
        assertThat(identityLinks.get(0).getGroupId()).isNull();
        assertThat(identityLinks.get(0).getType()).isEqualTo(IdentityLinkType.ASSIGNEE);

        // cleanup
        taskService.deleteTask(taskId,
                               true);
    }

    public void testGetIdentityLinksWithOwner() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        String taskId = task.getId();

        taskService.claim(taskId,
                          "kermit");
        taskService.delegateTask(taskId,
                                 "fozzie");

        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
        assertThat(identityLinks).hasSize(2);

        IdentityLink assignee = identityLinks.get(0);
        assertThat(assignee.getUserId()).isEqualTo("fozzie");
        assertThat(assignee.getGroupId()).isNull();
        assertThat(assignee.getType()).isEqualTo(IdentityLinkType.ASSIGNEE);

        IdentityLink owner = identityLinks.get(1);
        assertThat(owner.getUserId()).isEqualTo("kermit");
        assertThat(owner.getGroupId()).isNull();
        assertThat(owner.getType()).isEqualTo(IdentityLinkType.OWNER);

        // cleanup
        taskService.deleteTask(taskId,
                               true);
    }

    public void testGetIdentityLinksWithNonExistingOwner() {
        Task task = taskService.newTask();
        taskService.saveTask(task);
        String taskId = task.getId();

        taskService.claim(taskId,
                          "nonExistingOwner");
        taskService.delegateTask(taskId,
                                 "nonExistingAssignee");
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
        assertThat(identityLinks).hasSize(2);

        IdentityLink assignee = identityLinks.get(0);
        assertThat(assignee.getUserId()).isEqualTo("nonExistingAssignee");
        assertThat(assignee.getGroupId()).isNull();
        assertThat(assignee.getType()).isEqualTo(IdentityLinkType.ASSIGNEE);

        IdentityLink owner = identityLinks.get(1);
        assertThat(owner.getUserId()).isEqualTo("nonExistingOwner");
        assertThat(owner.getGroupId()).isNull();
        assertThat(owner.getType()).isEqualTo(IdentityLinkType.OWNER);

        // cleanup
        taskService.deleteTask(taskId,
                               true);
    }

    public void testSetPriority() {
        Task task = taskService.newTask();
        taskService.saveTask(task);

        taskService.setPriority(task.getId(),
                                12345);

        // Fetch task again to check if the priority is set
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertThat(task.getPriority()).isEqualTo(12345);

        taskService.deleteTask(task.getId(),
                               true);
    }

    public void testSetPriorityUnexistingTaskId() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> taskService.setPriority("unexistingtask", 12345))
            .withMessageContaining("Cannot find task with id unexistingtask")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Task.class));
    }

    public void testSetPriorityNullTaskId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.setPriority(null, 12345))
            .withMessageContaining("taskId is null");
    }

    public void testSetDueDate() {
        Task task = taskService.newTask();
        taskService.saveTask(task);

        // Set the due date to a non-null value
        Date now = new Date();
        taskService.setDueDate(task.getId(),
                               now);

        // Fetch task to check if the due date was persisted
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertThat(task.getDueDate()).isNotNull();

        // Set the due date to null
        taskService.setDueDate(task.getId(),
                               null);

        // Re-fetch the task to make sure the due date was set to null
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        assertThat(task.getDueDate()).isNull();

        taskService.deleteTask(task.getId(),
                               true);
    }

    public void testSetDueDateUnexistingTaskId() {
        assertThatExceptionOfType(ActivitiException.class)
            .isThrownBy(() -> taskService.setDueDate("unexistingtask", new Date()))
            .withMessageContaining("Cannot find task with id unexistingtask");
    }

    public void testSetDueDateNullTaskId() {
        assertThatExceptionOfType(ActivitiException.class)
            .isThrownBy(() -> taskService.setDueDate(null, new Date()))
            .withMessageContaining("taskId is null");
    }

    /**
     * @see <a href="https://activiti.atlassian.net/browse/ACT-1059">https://activiti.atlassian.net/browse/ACT-1059</a>
     */
    public void testSetDelegationState() {
        Task task = taskService.newTask();
        task.setOwner("wuzh");
        taskService.saveTask(task);
        taskService.delegateTask(task.getId(),
                                 "other");
        String taskId = task.getId();

        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertThat(task.getOwner()).isEqualTo("wuzh");
        assertThat(task.getAssignee()).isEqualTo("other");
        assertThat(task.getDelegationState()).isEqualTo(DelegationState.PENDING);

        task.setDelegationState(DelegationState.RESOLVED);
        taskService.saveTask(task);

        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertThat(task.getOwner()).isEqualTo("wuzh");
        assertThat(task.getAssignee()).isEqualTo("other");
        assertThat(task.getDelegationState()).isEqualTo(DelegationState.RESOLVED);

        taskService.deleteTask(taskId,
                               true);
    }

    private void checkHistoricVariableUpdateEntity(String variableName,
                                                   String processInstanceId) {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
            boolean deletedVariableUpdateFound = false;

            List<HistoricDetail> resultSet = historyService.createHistoricDetailQuery().processInstanceId(processInstanceId).list();
            for (HistoricDetail currentHistoricDetail : resultSet) {
                assertThat(currentHistoricDetail).isInstanceOf(HistoricDetailVariableInstanceUpdateEntity.class);
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

            assertThat(deletedVariableUpdateFound).isTrue();
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testRemoveVariable() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        taskService.setVariable(currentTask.getId(), "variable1", "value1");
        assertThat(taskService.getVariable(currentTask.getId(), "variable1")).isEqualTo("value1");
        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable1")).isNull();

        taskService.removeVariable(currentTask.getId(), "variable1");

        assertThat(taskService.getVariable(currentTask.getId(), "variable1")).isNull();

        checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
    }

    public void testRemoveVariableNullTaskId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.removeVariable(null, "variable"))
            .withMessageContaining("taskId is null");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableByHistoricActivityInstance() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
            assertThat(processInstance).isNotNull();
            Task task = taskService.createTaskQuery().singleResult();

            taskService.setVariable(task.getId(),
                                    "variable1",
                                    "value1");
            taskService.setVariable(task.getId(),
                                    "variable1",
                                    "value2");

            HistoricActivityInstance historicActivitiInstance = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId())
                    .activityId("theTask").singleResult();
            assertThat(historicActivitiInstance).isNotNull();

            List<HistoricDetail> resultSet = historyService.createHistoricDetailQuery().variableUpdates().activityInstanceId(historicActivitiInstance.getId())
                    .list();

            assertThat(resultSet).hasSize(2);
            assertThat(resultSet)
                    .extracting(h -> ((HistoricVariableUpdate) h).getValue())
                    .containsExactlyInAnyOrder("value1", "value2");
            assertThat(resultSet)
                    .extracting(h -> ((HistoricVariableUpdate) h).getVariableName())
                    .containsOnly("variable1");
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testRemoveVariables() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        Map<String, Object> varsToDelete = new HashMap<String, Object>();
        varsToDelete.put("variable1", "value1");
        varsToDelete.put("variable2", "value2");
        taskService.setVariables(currentTask.getId(), varsToDelete);
        taskService.setVariable(currentTask.getId(), "variable3", "value3");

        assertThat(taskService.getVariable(currentTask.getId(), "variable1")).isEqualTo("value1");
        assertThat(taskService.getVariable(currentTask.getId(), "variable2")).isEqualTo("value2");
        assertThat(taskService.getVariable(currentTask.getId(), "variable3")).isEqualTo("value3");
        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable1")).isNull();
        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable2")).isNull();
        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable3")).isNull();

        taskService.removeVariables(currentTask.getId(), varsToDelete.keySet());

        assertThat(taskService.getVariable(currentTask.getId(), "variable1")).isNull();
        assertThat(taskService.getVariable(currentTask.getId(), "variable2")).isNull();
        assertThat(taskService.getVariable(currentTask.getId(), "variable3")).isEqualTo("value3");

        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable1")).isNull();
        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable2")).isNull();
        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable3")).isNull();

        checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
        checkHistoricVariableUpdateEntity("variable2", processInstance.getId());
    }

    public void testRemoveVariablesNullTaskId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.removeVariables(null, emptyList()))
            .withMessageContaining("taskId is null");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testRemoveVariableLocal() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        taskService.setVariableLocal(currentTask.getId(), "variable1", "value1");
        assertThat(taskService.getVariable(currentTask.getId(), "variable1")).isEqualTo("value1");
        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable1")).isEqualTo("value1");

        taskService.removeVariableLocal(currentTask.getId(), "variable1");

        assertThat(taskService.getVariable(currentTask.getId(), "variable1")).isNull();
        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable1")).isNull();

        checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
    }

    public void testRemoveVariableLocalNullTaskId() {
        assertThatExceptionOfType(ActivitiException.class)
            .isThrownBy(() -> taskService.removeVariableLocal(null, "variable"))
            .withMessageContaining("taskId is null");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testRemoveVariablesLocal() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        Map<String, Object> varsToDelete = new HashMap<String, Object>();
        varsToDelete.put("variable1", "value1");
        varsToDelete.put("variable2", "value2");
        taskService.setVariablesLocal(currentTask.getId(), varsToDelete);
        taskService.setVariableLocal(currentTask.getId(), "variable3", "value3");

        assertThat(taskService.getVariable(currentTask.getId(), "variable1")).isEqualTo("value1");
        assertThat(taskService.getVariable(currentTask.getId(), "variable2")).isEqualTo("value2");
        assertThat(taskService.getVariable(currentTask.getId(), "variable3")).isEqualTo("value3");
        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable1")).isEqualTo("value1");
        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable2")).isEqualTo("value2");
        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable3")).isEqualTo("value3");

        taskService.removeVariables(currentTask.getId(), varsToDelete.keySet());

        assertThat(taskService.getVariable(currentTask.getId(), "variable1")).isNull();
        assertThat(taskService.getVariable(currentTask.getId(), "variable2")).isNull();
        assertThat(taskService.getVariable(currentTask.getId(), "variable3")).isEqualTo("value3");

        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable1")).isNull();
        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable2")).isNull();
        assertThat(taskService.getVariableLocal(currentTask.getId(), "variable3")).isEqualTo("value3");

        checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
        checkHistoricVariableUpdateEntity("variable2", processInstance.getId());
    }

    public void testRemoveVariablesLocalNullTaskId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> taskService.removeVariablesLocal(null, emptyList()))
            .withMessageContaining("taskId is null");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testUserTaskOptimisticLocking() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task task1 = taskService.createTaskQuery().singleResult();
        Task task2 = taskService.createTaskQuery().singleResult();

        task1.setDescription("test description one");
        taskService.saveTask(task1);

        assertThatExceptionOfType(ActivitiOptimisticLockingException.class)
            .isThrownBy(() -> {
                task2.setDescription("test description two");
                taskService.saveTask(task2);
            });
    }

    public void testDeleteTaskWithDeleteReason() {
        // ACT-900: deleteReason can be manually specified - can only be
        // validated when historyLevel > ACTIVITY
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {

            Task task = taskService.newTask();
            task.setName("test task");
            taskService.saveTask(task);

            assertThat(task.getId()).isNotNull();

            taskService.deleteTask(task.getId(),
                                   "deleted for testing purposes");

            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();

            assertThat(historicTaskInstance).isNotNull();
            assertThat(historicTaskInstance.getDeleteReason()).isEqualTo("deleted for testing purposes");

            // Delete historic task that is left behind, will not be cleaned up
            // because this is not part of a process
            taskService.deleteTask(task.getId(),
                                   true);
        }
    }

    public void testResolveTaskNullTaskId() {
        assertThatExceptionOfType(ActivitiException.class)
            .isThrownBy(() -> taskService.resolveTask(null))
            .withMessageContaining("taskId is null");
    }

    public void testResolveTaskUnexistingTaskId() {
        assertThatExceptionOfType(ActivitiException.class)
            .isThrownBy(() -> taskService.resolveTask("blergh"))
            .withMessageContaining("Cannot find task with id");
    }

    public void testResolveTaskWithParametersNullParameters() {
        Task task = taskService.newTask();
        task.setDelegationState(DelegationState.PENDING);
        taskService.saveTask(task);

        String taskId = task.getId();
        taskService.resolveTask(taskId,
                                null);

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            historyService.deleteHistoricTaskInstance(taskId);
        }

        // Fetch the task again
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertThat(task.getDelegationState()).isEqualTo(DelegationState.RESOLVED);

        taskService.deleteTask(taskId,
                               true);
    }

    public void testResolveTaskWithParametersEmptyParameters() {
        Task task = taskService.newTask();
        task.setDelegationState(DelegationState.PENDING);
        taskService.saveTask(task);

        String taskId = task.getId();
        taskService.resolveTask(taskId, emptyMap());

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            historyService.deleteHistoricTaskInstance(taskId);
        }

        // Fetch the task again
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        assertThat(task.getDelegationState()).isEqualTo(DelegationState.RESOLVED);

        taskService.deleteTask(taskId, true);
    }

    @Deployment(resources = {"org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml"})
    public void testResolveWithParametersTask() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

        // Fetch first task
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task.getName()).isEqualTo("First task");

        taskService.delegateTask(task.getId(), "johndoe");

        // Resolve first task
        Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("myParam", "myValue");
        taskService.resolveTask(task.getId(), taskParams);

        // Verify that task is resolved
        task = taskService.createTaskQuery().taskDelegationState(DelegationState.RESOLVED).singleResult();
        assertThat(task.getName()).isEqualTo("First task");

        // Verify task parameters set on execution
        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
        assertThat(variables).hasSize(1);
        assertThat(variables.get("myParam")).isEqualTo("myValue");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testDeleteTaskPartOfProcess() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();

        assertThatExceptionOfType(ActivitiException.class)
            .isThrownBy(() -> taskService.deleteTask(task.getId()))
            .withMessage("The task cannot be deleted because is part of a running process");

        assertThatExceptionOfType(ActivitiException.class)
            .isThrownBy(() -> taskService.deleteTask(task.getId(), true))
            .withMessage("The task cannot be deleted because is part of a running process");

        assertThatExceptionOfType(ActivitiException.class)
            .isThrownBy(() -> taskService.deleteTask(task.getId(), "test"))
            .withMessage("The task cannot be deleted because is part of a running process");

        assertThatExceptionOfType(ActivitiException.class)
            .isThrownBy(() -> taskService.deleteTasks(singletonList(task.getId())))
            .withMessage("The task cannot be deleted because is part of a running process");

        assertThatExceptionOfType(ActivitiException.class)
            .isThrownBy(() -> taskService.deleteTasks(singletonList(task.getId()), true))
            .withMessage("The task cannot be deleted because is part of a running process");

        assertThatExceptionOfType(ActivitiException.class)
            .isThrownBy(() -> taskService.deleteTasks(singletonList(task.getId()), "test"))
            .withMessage("The task cannot be deleted because is part of a running process");
    }

    @Deployment
    public void testFormKeyExpression() {
        runtimeService.startProcessInstanceByKey("testFormExpression",
                                                 singletonMap("var", "abc"));

        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task.getFormKey()).isEqualTo("first-form.json");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().singleResult();
        assertThat(task.getFormKey()).isEqualTo("form-abc.json");

        task.setFormKey("form-changed.json");
        taskService.saveTask(task);
        task = taskService.createTaskQuery().singleResult();
        assertThat(task.getFormKey()).isEqualTo("form-changed.json");

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
            assertThat(historicTaskInstance.getFormKey()).isEqualTo("form-changed.json");
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableLocalWithCast() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        taskService.setVariableLocal(currentTask.getId(), "variable1", "value1");

        String variable = taskService.getVariableLocal(currentTask.getId(), "variable1", String.class);

        assertThat(variable).isEqualTo("value1");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableLocalNotExistingWithCast() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        String variable = taskService.getVariableLocal(currentTask.getId(), "variable1", String.class);

        assertThat(variable).isNull();
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableLocalWithInvalidCast() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        taskService.setVariableLocal(currentTask.getId(),
                                     "variable1",
                                     "value1");

        assertThatExceptionOfType(ClassCastException.class)
            .isThrownBy(() -> taskService.getVariableLocal(currentTask.getId(), "variable1", Boolean.class));
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableWithCast() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        taskService.setVariable(currentTask.getId(), "variable1", "value1");

        String variable = taskService.getVariable(currentTask.getId(), "variable1", String.class);

        assertThat(variable).isEqualTo("value1");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableNotExistingWithCast() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        String variable = taskService.getVariable(currentTask.getId(), "variable1", String.class);

        assertThat(variable).isNull();
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableWithInvalidCast() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        Task currentTask = taskService.createTaskQuery().singleResult();

        taskService.setVariable(currentTask.getId(), "variable1", "value1");

        assertThatExceptionOfType(ClassCastException.class)
            .isThrownBy(() -> taskService.getVariable(currentTask.getId(), "variable1", Boolean.class));
    }

    public void testClaimTime() {
        Task task = taskService.newTask();
        taskService.saveTask(task);

        assertThat(task.getClaimTime()).isNull();

        // Claim task
        taskService.claim(task.getId(), "user");
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();

        assertThat(task.getClaimTime()).isNotNull();

        // Unclaim task
        taskService.unclaim(task.getId());
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();

        assertThat(task.getClaimTime()).isNull();

        taskService.deleteTask(task.getId(), true);
    }
}
