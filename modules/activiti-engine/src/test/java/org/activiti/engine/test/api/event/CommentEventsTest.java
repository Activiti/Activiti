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
 * @author Frederik Heremans
 */
public class CommentEventsTest extends PluggableActivitiTestCase {

	private TestActivitiEntityEventListener listener;

	/**
	 * Test create, update and delete events of comments on a task/process.
	 */
	@Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
	public void testCommentEntityEvents() throws Exception {
		if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
			
			Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
			assertNotNull(task);
			
			// Create link-comment
			Comment comment = taskService.addComment(task.getId(), task.getProcessInstanceId(), "comment");
			assertEquals(2, listener.getEventsReceived().size());
			ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
			assertEquals(processInstance.getId(), event.getProcessInstanceId());
			assertEquals(processInstance.getId(), event.getExecutionId());
			assertEquals(processInstance.getProcessDefinitionId(), event.getProcessDefinitionId());
			Comment commentFromEvent = (Comment) event.getEntity();
			assertEquals(comment.getId(), commentFromEvent.getId());
			
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
			assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
			listener.clearEventsReceived();
			
			// Finally, delete comment
			taskService.deleteComment(comment.getId());
			assertEquals(1, listener.getEventsReceived().size());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
			assertEquals(processInstance.getId(), event.getProcessInstanceId());
			assertEquals(processInstance.getId(), event.getExecutionId());
			assertEquals(processInstance.getProcessDefinitionId(), event.getProcessDefinitionId());
			commentFromEvent = (Comment) event.getEntity();
			assertEquals(comment.getId(), commentFromEvent.getId());
		}
	}
	
	public void testCommentEntityEventsStandaloneTask() throws Exception {
		if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
			Task task = null;
			try {
				task = taskService.newTask();
				taskService.saveTask(task);
				assertNotNull(task);
				
				// Create link-comment
				Comment comment = taskService.addComment(task.getId(), null, "comment");
				assertEquals(2, listener.getEventsReceived().size());
				ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
				assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
				assertNull(event.getProcessInstanceId());
				assertNull(event.getExecutionId());
				assertNull(event.getProcessDefinitionId());
				Comment commentFromEvent = (Comment) event.getEntity();
				assertEquals(comment.getId(), commentFromEvent.getId());
				
				event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
				assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
				listener.clearEventsReceived();
				
				// Finally, delete comment
				taskService.deleteComment(comment.getId());
				assertEquals(1, listener.getEventsReceived().size());
				event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
				assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
				assertNull(event.getProcessInstanceId());
				assertNull(event.getExecutionId());
				assertNull(event.getProcessDefinitionId());
				commentFromEvent = (Comment) event.getEntity();
				assertEquals(comment.getId(), commentFromEvent.getId());
				
			} finally {
				if(task != null && task.getId() != null) {
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
