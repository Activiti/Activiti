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

import java.util.List;

import junit.framework.AssertionFailedError;

import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**


 */
public class TaskIdentityLinksTest extends PluggableActivitiTestCase {

  @Deployment(resources = "org/activiti/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  public void testCandidateUserLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.addCandidateUser(taskId, "kermit");

    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    IdentityLink identityLink = identityLinks.get(0);

    assertThat(identityLink.getGroupId()).isNull();
    assertThat(identityLink.getUserId()).isEqualTo("kermit");
    assertThat(identityLink.getType()).isEqualTo(IdentityLinkType.CANDIDATE);
    assertThat(identityLink.getTaskId()).isEqualTo(taskId);

    assertThat(identityLinks).hasSize(1);

    taskService.deleteCandidateUser(taskId, "kermit");

    assertThat(taskService.getIdentityLinksForTask(taskId)).hasSize(0);
  }

  @Deployment(resources = "org/activiti/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  public void testCandidateGroupLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.addCandidateGroup(taskId, "muppets");

    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    IdentityLink identityLink = identityLinks.get(0);

    assertThat(identityLink.getGroupId()).isEqualTo("muppets");
    assertThat(identityLink.getUserId()).as("kermit").isNull();
    assertThat(identityLink.getType()).isEqualTo(IdentityLinkType.CANDIDATE);
    assertThat(identityLink.getTaskId()).isEqualTo(taskId);

    assertThat(identityLinks).hasSize(1);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      List<Event> taskEvents = taskService.getTaskEvents(taskId);
      assertThat(taskEvents).hasSize(1);
      Event taskEvent = taskEvents.get(0);
      assertThat(taskEvent.getAction()).isEqualTo(Event.ACTION_ADD_GROUP_LINK);
      List<String> taskEventMessageParts = taskEvent.getMessageParts();
      assertThat(taskEventMessageParts.get(0)).isEqualTo("muppets");
      assertThat(taskEventMessageParts.get(1)).isEqualTo(IdentityLinkType.CANDIDATE);
      assertThat(taskEventMessageParts).hasSize(2);
    }

    taskService.deleteCandidateGroup(taskId, "muppets");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      List<Event> taskEvents = taskService.getTaskEvents(taskId);
      Event taskEvent = findTaskEvent(taskEvents, Event.ACTION_DELETE_GROUP_LINK);
      assertThat(taskEvent.getAction()).isEqualTo(Event.ACTION_DELETE_GROUP_LINK);
      List<String> taskEventMessageParts = taskEvent.getMessageParts();
      assertThat(taskEventMessageParts.get(0)).isEqualTo("muppets");
      assertThat(taskEventMessageParts.get(1)).isEqualTo(IdentityLinkType.CANDIDATE);
      assertThat(taskEventMessageParts).hasSize(2);
      assertThat(taskEvents).hasSize(2);
    }

    assertThat(taskService.getIdentityLinksForTask(taskId)).hasSize(0);
  }

  private Event findTaskEvent(List<Event> taskEvents, String action) {
    for (Event event : taskEvents) {
      if (action.equals(event.getAction())) {
        return event;
      }
    }
    throw new AssertionFailedError("no task event found with action " + action);
  }

  @Deployment(resources = "org/activiti/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  public void testCustomTypeUserLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.addUserIdentityLink(taskId, "kermit", "interestee");

    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    IdentityLink identityLink = identityLinks.get(0);

    assertThat(identityLink.getGroupId()).isNull();
    assertThat(identityLink.getUserId()).isEqualTo("kermit");
    assertThat(identityLink.getType()).isEqualTo("interestee");
    assertThat(identityLink.getTaskId()).isEqualTo(taskId);

    assertThat(identityLinks).hasSize(1);

    taskService.deleteUserIdentityLink(taskId, "kermit", "interestee");

    assertThat(taskService.getIdentityLinksForTask(taskId)).hasSize(0);
  }

  @Deployment(resources = "org/activiti/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  public void testCustomLinkGroupLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.addGroupIdentityLink(taskId, "muppets", "playing");

    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    IdentityLink identityLink = identityLinks.get(0);

    assertThat(identityLink.getGroupId()).isEqualTo("muppets");
    assertThat(identityLink.getUserId()).as("kermit").isNull();
    assertThat(identityLink.getType()).isEqualTo("playing");
    assertThat(identityLink.getTaskId()).isEqualTo(taskId);

    assertThat(identityLinks).hasSize(1);

    taskService.deleteGroupIdentityLink(taskId, "muppets", "playing");

    assertThat(taskService.getIdentityLinksForTask(taskId)).hasSize(0);
  }

  public void testDeleteAssignee() {
    Task task = taskService.newTask();
    task.setAssignee("nonExistingUser");
    taskService.saveTask(task);

    taskService.deleteUserIdentityLink(task.getId(), "nonExistingUser", IdentityLinkType.ASSIGNEE);

    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertThat(task.getAssignee()).isNull();
    assertThat(taskService.getIdentityLinksForTask(task.getId())).hasSize(0);

    // cleanup
    taskService.deleteTask(task.getId(), true);
  }

  public void testDeleteOwner() {
    Task task = taskService.newTask();
    task.setOwner("nonExistingUser");
    taskService.saveTask(task);

    taskService.deleteUserIdentityLink(task.getId(), "nonExistingUser", IdentityLinkType.OWNER);

    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertThat(task.getOwner()).isNull();
    assertThat(taskService.getIdentityLinksForTask(task.getId())).hasSize(0);

    // cleanup
    taskService.deleteTask(task.getId(), true);
  }

  @Deployment(resources = "org/activiti/engine/test/api/task/TaskIdentityLinksTest.testDeleteCandidateUser.bpmn20.xml")
  public void testDeleteCandidateUser() {
    runtimeService.startProcessInstanceByKey("TaskIdentityLinks");

    String taskId = taskService.createTaskQuery().singleResult().getId();

    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);

    assertThat(identityLinks).hasSize(1);
    IdentityLink identityLink = identityLinks.get(0);

    assertThat(identityLink.getUserId()).isEqualTo("user");
  }

  @Deployment(resources = "org/activiti/engine/test/api/task/IdentityLinksProcess.bpmn20.xml")
  public void testEmptyCandidateUserLink() {
    runtimeService.startProcessInstanceByKey("IdentityLinksProcess");

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.addCandidateGroup(taskId, "muppets");
    taskService.deleteCandidateUser(taskId, "kermit");

    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    assertThat(identityLinks).isNotNull();
    assertThat(identityLinks).hasSize(1);

    IdentityLink identityLink = identityLinks.get(0);
    assertThat(identityLink.getGroupId()).isEqualTo("muppets");
    assertThat(identityLink.getUserId()).isEqualTo(null);
    assertThat(identityLink.getType()).isEqualTo(IdentityLinkType.CANDIDATE);
    assertThat(identityLink.getTaskId()).isEqualTo(taskId);

    taskService.deleteCandidateGroup(taskId, "muppets");

    assertThat(taskService.getIdentityLinksForTask(taskId)).hasSize(0);
  }

  // Test custom identity links
  @Deployment
  public void testCustomIdentityLink() {
    runtimeService.startProcessInstanceByKey("customIdentityLink");

    List<Task> tasks = taskService.createTaskQuery().taskInvolvedUser("kermit").list();
    assertThat(tasks).hasSize(1);

    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(tasks.get(0).getId());
    assertThat(identityLinks).hasSize(2);

    for (IdentityLink idLink : identityLinks) {
      assertThat(idLink.getType()).isEqualTo("businessAdministrator");
      String userId = idLink.getUserId();
      if (userId == null) {
        assertThat(idLink.getGroupId()).isEqualTo("management");
      } else {
        assertThat(userId).isEqualTo("kermit");
      }
    }
  }
}
