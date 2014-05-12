package org.activiti.engine.test.api.history;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricData;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.history.ProcessInstanceHistoryLog;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;

/**
 * @author Joram Barrez
 */
public class ProcessInstanceLogQueryTest extends PluggableActivitiTestCase {
	
	protected String processInstanceId;
	
	@Override
	protected void setUp() throws Exception {
	  super.setUp();
	  
	  // Deploy test process
	  deployTwoTasksTestProcess();
	  
	  // Start process instance
	  Map<String, Object> vars = new HashMap<String, Object>();
	  vars.put("var1", "Hello");
	  vars.put("var2", 123);
	  this.processInstanceId = runtimeService.startProcessInstanceByKey("twoTasksProcess", vars).getId();
	  
	  // Add some comments
	  taskService.addComment(null, processInstanceId, "Hello World");
	  taskService.addComment(null, processInstanceId, "Hello World2");
	  taskService.addComment(null, processInstanceId, "Hello World3");
	  
	  // Change some variables
	  runtimeService.setVariable(processInstanceId, "var1", "new Value");
	  
	  // Finish tasks
	  for (Task task : taskService.createTaskQuery().list()) {
	  	taskService.complete(task.getId());
	  }
	}
	
	@Override
	protected void tearDown() throws Exception {
		
		for (Comment comment : taskService.getProcessInstanceComments(processInstanceId)) {
			taskService.deleteComment(comment.getId());
		}
		
	  super.tearDown();
	}
	
	public void testBaseProperties() {
		ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId).singleResult();
		assertNotNull(log.getId());
		assertNotNull(log.getProcessDefinitionId());
		assertNotNull(log.getStartActivityId());
		assertNotNull(log.getDurationInMillis());
		assertNotNull(log.getEndTime());
		assertNotNull(log.getStartTime());
	}
	
	public void testIncludeTasks() {
		ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId)
			.includeTasks()
			.singleResult();
		List<HistoricData> events = log.getHistoricData();
		assertEquals(2, events.size());
		
		for (HistoricData event : events) {
			assertTrue(event instanceof HistoricTaskInstance);
		}
	}
	
	public void testIncludeComments() {
		ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId)
			.includeComments()
			.singleResult();
		List<HistoricData> events = log.getHistoricData();
		assertEquals(3, events.size());
		
		for (HistoricData event : events) {
			assertTrue(event instanceof Comment);
		}
	}
	
	public void testIncludeTasksandComments() {
		ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId)
			.includeTasks()
			.includeComments()
			.singleResult();
		List<HistoricData> events = log.getHistoricData();
		assertEquals(5, events.size());
		
		for (int i=0; i<5; i++) {
			HistoricData event = events.get(i);
			if (i<2) { // tasks are created before comments
				assertTrue(event instanceof HistoricTaskInstance);
			} else {
				assertTrue(event instanceof Comment);
			}
		}
	}
	
	public void testIncludeActivities() {
		ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId)
			.includeActivities()
			.singleResult();
		List<HistoricData> events = log.getHistoricData();
		assertEquals(5, events.size());
		
		for (HistoricData event : events) {
			assertTrue(event instanceof HistoricActivityInstance);
		}
	}
	
	
	public void testIncludeVariables() {
		if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
			ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId)
				.includeVariables()
				.singleResult();
			List<HistoricData> events = log.getHistoricData();
			assertEquals(2, events.size());
			
			for (HistoricData event : events) {
				assertTrue(event instanceof HistoricVariableInstance);
			}
		}
	}
	
	public void testIncludeVariableUpdates() {
		if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
			ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId)
				.includeVariableUpdates()
				.singleResult();
			List<HistoricData> events = log.getHistoricData();
			assertEquals(3, events.size());
			
			for (HistoricData event : events) {
				assertTrue(event instanceof HistoricVariableUpdate);
			}
		}
	}
	
	public void testEverything() {
		if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
			ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId)
					.includeTasks()
					.includeActivities()
					.includeComments()
					.includeVariables()
					.includeVariableUpdates()
					.singleResult();
			List<HistoricData> events = log.getHistoricData();
			assertEquals(15, events.size());
		}
	}

}
