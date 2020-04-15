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
package org.activiti.engine.test.api.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to comments.
 *
 */
public class CommentEventsTest extends PluggableActivitiTestCase {

  private TestActivitiEntityEventListener listener;

  /**
   * Test create, update and delete events of comments on a task/process.
   */
  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testCommentEntityEvents() throws Exception {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

      Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      assertThat(task).isNotNull();

      // Create link-comment
      Comment comment = taskService.addComment(task.getId(), task.getProcessInstanceId(), "comment");
      assertThat(listener.getEventsReceived()).hasSize(2);
      ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
      assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
      assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
      assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
      Comment commentFromEvent = (Comment) event.getEntity();
      assertThat(commentFromEvent.getId()).isEqualTo(comment.getId());

      event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
      listener.clearEventsReceived();

      // Finally, delete comment
      taskService.deleteComment(comment.getId());
      assertThat(listener.getEventsReceived()).hasSize(1);
      event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
      assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
      assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
      assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
      commentFromEvent = (Comment) event.getEntity();
      assertThat(commentFromEvent.getId()).isEqualTo(comment.getId());
    }
  }

  public void testCommentEntityEventsStandaloneTask() throws Exception {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      Task task = null;
      try {
        task = taskService.newTask();
        taskService.saveTask(task);
        assertThat(task).isNotNull();

        // Create link-comment
        Comment comment = taskService.addComment(task.getId(), null, "comment");
        assertThat(listener.getEventsReceived()).hasSize(2);
        ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
        assertThat(event.getProcessInstanceId()).isNull();
        assertThat(event.getExecutionId()).isNull();
        assertThat(event.getProcessDefinitionId()).isNull();
        Comment commentFromEvent = (Comment) event.getEntity();
        assertThat(commentFromEvent.getId()).isEqualTo(comment.getId());

        event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
        listener.clearEventsReceived();

        // Finally, delete comment
        taskService.deleteComment(comment.getId());
        assertThat(listener.getEventsReceived()).hasSize(1);
        event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
        assertThat(event.getProcessInstanceId()).isNull();
        assertThat(event.getExecutionId()).isNull();
        assertThat(event.getProcessDefinitionId()).isNull();
        commentFromEvent = (Comment) event.getEntity();
        assertThat(commentFromEvent.getId()).isEqualTo(comment.getId());

      } finally {
        if (task != null && task.getId() != null) {
          taskService.deleteTask(task.getId());
          historyService.deleteHistoricTaskInstance(task.getId());
        }
      }
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    listener = new TestActivitiEntityEventListener(Comment.class);
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    if (listener != null) {
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
  }
}
