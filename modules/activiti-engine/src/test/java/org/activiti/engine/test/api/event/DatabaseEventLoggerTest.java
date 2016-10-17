package org.activiti.engine.test.api.event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.event.logger.EventLogger;
import org.activiti.engine.impl.event.logger.handler.Fields;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Joram Barrez
 */
public class DatabaseEventLoggerTest extends PluggableActivitiTestCase {
	
	protected EventLogger databaseEventLogger;
	
	protected ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void setUp() throws Exception {
	  super.setUp();
	  
	  // Database event logger setup
	  databaseEventLogger = new EventLogger(processEngineConfiguration.getClock(), processEngineConfiguration.getObjectMapper());
	  runtimeService.addEventListener(databaseEventLogger);
	}
	
	@Override
	protected void tearDown() throws Exception {
		
		// Database event logger teardown
		runtimeService.removeEventListener(databaseEventLogger);
		
	  super.tearDown();
	}
	
	@Deployment(resources = {"org/activiti/engine/test/api/event/DatabaseEventLoggerProcess.bpmn20.xml"})
	public void testDatabaseEvents() throws IOException {
		
		String testTenant = "testTenant";
		
		String deploymentId = repositoryService.createDeployment()
				.addClasspathResource("org/activiti/engine/test/api/event/DatabaseEventLoggerProcess.bpmn20.xml")
				.tenantId(testTenant)
				.deploy().getId();
		
		// Run process to gather data
		ProcessInstance processInstance = 
				runtimeService.startProcessInstanceByKeyAndTenantId("DatabaseEventLoggerProcess", CollectionUtil.singletonMap("testVar", "helloWorld"), testTenant);
		
		// Verify event log entries
		List<EventLogEntry> eventLogEntries = managementService.getEventLogEntries(null, null);
		
		String processDefinitionId = processInstance.getProcessDefinitionId();
		Iterator<EventLogEntry> iterator = eventLogEntries.iterator();
		while (iterator.hasNext()) {
			EventLogEntry entry = iterator.next();
			if (entry.getProcessDefinitionId() != null && !entry.getProcessDefinitionId().equals(processDefinitionId)) {
				iterator.remove();
			}
		}
		
		assertEquals(15, eventLogEntries.size());
		
		long lastLogNr = -1;
		for (int i=0; i< eventLogEntries.size(); i++) {
			
			EventLogEntry entry = eventLogEntries.get(i);
			
			if (i == 0) {
				
				assertNotNull(entry.getType());
				assertEquals(entry.getType(), ActivitiEventType.VARIABLE_CREATED.name());
				assertNotNull(entry.getProcessDefinitionId());
				assertNotNull(entry.getProcessInstanceId());
				assertNotNull(entry.getTimeStamp());
				assertNull(entry.getTaskId());
				
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNotNull(data.get(Fields.PROCESS_DEFINITION_ID));
				assertNotNull(data.get(Fields.PROCESS_INSTANCE_ID));
				assertNotNull(data.get(Fields.VALUE_STRING));
				assertEquals(testTenant, data.get(Fields.TENANT_ID));
			}
			
			// process instance start
			if (i == 1) {
				
				assertNotNull(entry.getType());
				assertEquals("PROCESSINSTANCE_START", entry.getType());
				assertNotNull(entry.getProcessDefinitionId());
				assertNotNull(entry.getProcessInstanceId());
				assertNotNull(entry.getTimeStamp());
				assertNull(entry.getExecutionId());
				assertNull(entry.getTaskId());
				
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNotNull(data.get(Fields.ID));
				assertNotNull(data.get(Fields.PROCESS_DEFINITION_ID));
				assertNotNull(data.get(Fields.TENANT_ID));
				assertEquals(testTenant, data.get(Fields.TENANT_ID));
				
        Map<String, Object> variableMap = (Map<String, Object>) data.get(Fields.VARIABLES);
        assertEquals(1, variableMap.size());
        assertEquals("helloWorld", variableMap.get("testVar"));
				
				assertFalse(data.containsKey(Fields.NAME));
				assertFalse(data.containsKey(Fields.BUSINESS_KEY));
			}
			
			// Activity started
			if (i == 2 || i == 5 || i == 8 || i == 12) {
				assertNotNull(entry.getType());
				assertEquals(entry.getType(), ActivitiEventType.ACTIVITY_STARTED.name());
				assertNotNull(entry.getProcessDefinitionId());
				assertNotNull(entry.getProcessInstanceId());
				assertNotNull(entry.getTimeStamp());
				assertNotNull(entry.getExecutionId());
				assertNull(entry.getTaskId());
				
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNotNull(data.get(Fields.ACTIVITY_ID));
				assertNotNull(data.get(Fields.PROCESS_DEFINITION_ID));
				assertNotNull(data.get(Fields.PROCESS_INSTANCE_ID));
				assertNotNull(data.get(Fields.EXECUTION_ID));
				assertNotNull(data.get(Fields.ACTIVITY_TYPE));
				assertNotNull(data.get(Fields.BEHAVIOR_CLASS));
				assertEquals(testTenant, data.get(Fields.TENANT_ID));
			}
			
			
			// Leaving start
			if (i == 3) {
				
				assertNotNull(entry.getType());
				assertEquals(entry.getType(), ActivitiEventType.ACTIVITY_COMPLETED.name());
				assertNotNull(entry.getProcessDefinitionId());
				assertNotNull(entry.getProcessInstanceId());
				assertNotNull(entry.getTimeStamp());
				assertNotNull(entry.getExecutionId());
				assertNull(entry.getTaskId());
				
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNotNull(data.get(Fields.ACTIVITY_ID));
				assertEquals("startEvent1", data.get(Fields.ACTIVITY_ID));
				assertNotNull(data.get(Fields.PROCESS_DEFINITION_ID));
				assertNotNull(data.get(Fields.PROCESS_INSTANCE_ID));
				assertNotNull(data.get(Fields.EXECUTION_ID));
				assertNotNull(data.get(Fields.ACTIVITY_TYPE));
				assertNotNull(data.get(Fields.BEHAVIOR_CLASS));
				assertEquals(testTenant, data.get(Fields.TENANT_ID));
				
			}
			
			// Sequence flow taken
			if (i == 4 || i == 7 || i == 11) {
				assertNotNull(entry.getType());
				assertEquals(entry.getType(), ActivitiEventType.SEQUENCEFLOW_TAKEN.name());
				assertNotNull(entry.getProcessDefinitionId());
				assertNotNull(entry.getProcessInstanceId());
				assertNotNull(entry.getTimeStamp());
				assertNotNull(entry.getExecutionId());
				assertNull(entry.getTaskId());
				
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNotNull(data.get(Fields.ID));
				assertNotNull(data.get(Fields.SOURCE_ACTIVITY_ID));
				assertNotNull(data.get(Fields.SOURCE_ACTIVITY_NAME));
				assertNotNull(data.get(Fields.SOURCE_ACTIVITY_TYPE));
				assertNotNull(data.get(Fields.SOURCE_ACTIVITY_BEHAVIOR_CLASS));
				assertNotNull(data.get(Fields.TARGET_ACTIVITY_ID));
				assertNotNull(data.get(Fields.TARGET_ACTIVITY_NAME));
				assertNotNull(data.get(Fields.TARGET_ACTIVITY_TYPE));
				assertNotNull(data.get(Fields.TARGET_ACTIVITY_BEHAVIOR_CLASS));
				assertEquals(testTenant, data.get(Fields.TENANT_ID));
			}
			
			// Leaving parallel gateway
			if (i == 6) {
				
				assertNotNull(entry.getType());
				assertEquals(entry.getType(), ActivitiEventType.ACTIVITY_COMPLETED.name());
				assertNotNull(entry.getProcessDefinitionId());
				assertNotNull(entry.getProcessInstanceId());
				assertNotNull(entry.getTimeStamp());
				assertNotNull(entry.getExecutionId());
				assertNull(entry.getTaskId());
				
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNotNull(data.get(Fields.ACTIVITY_ID));
				assertNotNull(data.get(Fields.PROCESS_DEFINITION_ID));
				assertNotNull(data.get(Fields.PROCESS_INSTANCE_ID));
				assertNotNull(data.get(Fields.EXECUTION_ID));
				assertNotNull(data.get(Fields.ACTIVITY_TYPE));
				assertNotNull(data.get(Fields.BEHAVIOR_CLASS));
				assertEquals(testTenant, data.get(Fields.TENANT_ID));
				
			}
			
			// Tasks
			if (i == 10 || i == 14) {
			
				assertNotNull(entry.getType());
				assertEquals(entry.getType(), ActivitiEventType.TASK_CREATED.name());
				assertNotNull(entry.getTimeStamp());
				assertNotNull(entry.getProcessDefinitionId());
				assertNotNull(entry.getProcessInstanceId());
				assertNotNull(entry.getExecutionId());
				assertNotNull(entry.getTaskId());
				
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNotNull(data.get(Fields.ID));
				assertNotNull(data.get(Fields.NAME));
				assertNotNull(data.get(Fields.ASSIGNEE));
				assertNotNull(data.get(Fields.CREATE_TIME));
				assertNotNull(data.get(Fields.PRIORITY));
				assertNotNull(data.get(Fields.PROCESS_DEFINITION_ID));
				assertNotNull(data.get(Fields.EXECUTION_ID));
				assertNotNull(data.get(Fields.TENANT_ID));
				
				assertFalse(data.containsKey(Fields.DESCRIPTION));
				assertFalse(data.containsKey(Fields.CATEGORY));
				assertFalse(data.containsKey(Fields.OWNER));
				assertFalse(data.containsKey(Fields.DUE_DATE));
				assertFalse(data.containsKey(Fields.FORM_KEY));
				assertFalse(data.containsKey(Fields.USER_ID));
				
				assertEquals(testTenant, data.get(Fields.TENANT_ID));
				
			}
			
			if (i == 9 || i == 13) {
				
				assertNotNull(entry.getType());
				assertEquals(entry.getType(), ActivitiEventType.TASK_ASSIGNED.name());
				assertNotNull(entry.getTimeStamp());
				assertNotNull(entry.getProcessDefinitionId());
				assertNotNull(entry.getProcessInstanceId());
				assertNotNull(entry.getExecutionId());
				assertNotNull(entry.getTaskId());
				
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNotNull(data.get(Fields.ID));
				assertNotNull(data.get(Fields.NAME));
				assertNotNull(data.get(Fields.ASSIGNEE));
				assertNotNull(data.get(Fields.CREATE_TIME));
				assertNotNull(data.get(Fields.PRIORITY));
				assertNotNull(data.get(Fields.PROCESS_DEFINITION_ID));
				assertNotNull(data.get(Fields.EXECUTION_ID));
				assertNotNull(data.get(Fields.TENANT_ID));
				
				assertFalse(data.containsKey(Fields.DESCRIPTION));
				assertFalse(data.containsKey(Fields.CATEGORY));
				assertFalse(data.containsKey(Fields.OWNER));
				assertFalse(data.containsKey(Fields.DUE_DATE));
				assertFalse(data.containsKey(Fields.FORM_KEY));
				assertFalse(data.containsKey(Fields.USER_ID));
				
				assertEquals(testTenant, data.get(Fields.TENANT_ID));
				
			}
			
			lastLogNr = entry.getLogNumber();
		}
		
		// Completing two tasks
		for (Task task : taskService.createTaskQuery().list()) {
			Authentication.setAuthenticatedUserId(task.getAssignee());
			Map<String, Object> varMap = new HashMap<String, Object>();
	    varMap.put("test", "test");
			taskService.complete(task.getId(), varMap);
			Authentication.setAuthenticatedUserId(null);
		}
		
		// Verify events
		eventLogEntries = managementService.getEventLogEntries(lastLogNr, 100L);
		assertEquals(17, eventLogEntries.size());
		
		for (int i=0; i< eventLogEntries.size(); i++) {
			
			EventLogEntry entry = eventLogEntries.get(i);
			
			// Task completion 
			if (i == 1 || i == 6) {
				assertNotNull(entry.getType());
				assertEquals(ActivitiEventType.TASK_COMPLETED.name(), entry.getType());
				assertNotNull(entry.getProcessDefinitionId());
				assertNotNull(entry.getProcessInstanceId());
				assertNotNull(entry.getExecutionId());
				assertNotNull(entry.getTaskId());
				
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNotNull(data.get(Fields.ID));
				assertNotNull(data.get(Fields.NAME));
				assertNotNull(data.get(Fields.ASSIGNEE));
				assertNotNull(data.get(Fields.CREATE_TIME));
				assertNotNull(data.get(Fields.PRIORITY));
				assertNotNull(data.get(Fields.PROCESS_DEFINITION_ID));
				assertNotNull(data.get(Fields.EXECUTION_ID));
				assertNotNull(data.get(Fields.TENANT_ID));
				assertNotNull(data.get(Fields.USER_ID));
				
        Map<String, Object> variableMap = (Map<String, Object>) data.get(Fields.VARIABLES);
				assertEquals(1, variableMap.size());
				assertEquals("test", variableMap.get("test"));
				
				assertFalse(data.containsKey(Fields.DESCRIPTION));
				assertFalse(data.containsKey(Fields.CATEGORY));
				assertFalse(data.containsKey(Fields.OWNER));
				assertFalse(data.containsKey(Fields.DUE_DATE));
				assertFalse(data.containsKey(Fields.FORM_KEY));
				
				assertEquals(testTenant, data.get(Fields.TENANT_ID));
				
			}
			
			// Activity Completed
			if (i == 2 || i == 7 || i == 10 || i == 13) {
				assertNotNull(entry.getType());
				assertEquals(ActivitiEventType.ACTIVITY_COMPLETED.name(), entry.getType());
				assertNotNull(entry.getProcessDefinitionId());
				assertNotNull(entry.getProcessInstanceId());
				assertNotNull(entry.getTimeStamp());
				assertNotNull(entry.getExecutionId());
				assertNull(entry.getTaskId());
				
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNotNull(data.get(Fields.ACTIVITY_ID));
				assertNotNull(data.get(Fields.PROCESS_DEFINITION_ID));
				assertNotNull(data.get(Fields.PROCESS_INSTANCE_ID));
				assertNotNull(data.get(Fields.EXECUTION_ID));
				assertNotNull(data.get(Fields.ACTIVITY_TYPE));
				assertNotNull(data.get(Fields.BEHAVIOR_CLASS));
				
				assertEquals(testTenant, data.get(Fields.TENANT_ID));
				
				if (i == 2) {
					assertEquals("userTask", data.get(Fields.ACTIVITY_TYPE));
				} else if (i == 7) {
					assertEquals("userTask", data.get(Fields.ACTIVITY_TYPE));
				} else if (i == 10) {
					assertEquals("parallelGateway", data.get(Fields.ACTIVITY_TYPE));
				} else if (i == 13) {
					assertEquals("endEvent", data.get(Fields.ACTIVITY_TYPE));
				}
				
			}
			
			// Sequence flow taken
			if (i == 3 || i == 8 || i == 11) {
				assertNotNull(entry.getType());
				assertEquals(entry.getType(), ActivitiEventType.SEQUENCEFLOW_TAKEN.name());
				assertNotNull(entry.getProcessDefinitionId());
				assertNotNull(entry.getProcessInstanceId());
				assertNotNull(entry.getTimeStamp());
				assertNotNull(entry.getExecutionId());
				assertNull(entry.getTaskId());
				
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNotNull(data.get(Fields.ID));
				assertNotNull(data.get(Fields.SOURCE_ACTIVITY_ID));
				assertNotNull(data.get(Fields.SOURCE_ACTIVITY_TYPE));
				assertNotNull(data.get(Fields.SOURCE_ACTIVITY_BEHAVIOR_CLASS));
				assertNotNull(data.get(Fields.TARGET_ACTIVITY_ID));
				assertNotNull(data.get(Fields.TARGET_ACTIVITY_TYPE));
				assertNotNull(data.get(Fields.TARGET_ACTIVITY_BEHAVIOR_CLASS));
				
				assertEquals(testTenant, data.get(Fields.TENANT_ID));
			}
				
			if (i == 14 || i == 15) {
			  assertEquals(entry.getType(), ActivitiEventType.VARIABLE_DELETED.name());
			  // process definition Id can't be recognized in  DB flush
			  assertNull(entry.getProcessDefinitionId());
			  assertNotNull(entry.getProcessInstanceId());
			  assertNotNull(entry.getTimeStamp());
			  assertNull(entry.getTaskId());
			}
			
			if (i == 16) {
				assertNotNull(entry.getType());
				assertEquals("PROCESSINSTANCE_END", entry.getType());
				assertNotNull(entry.getProcessDefinitionId());
				assertNotNull(entry.getProcessInstanceId());
				assertNotNull(entry.getTimeStamp());
				assertNull(entry.getExecutionId());
				assertNull(entry.getTaskId());
				
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNotNull(data.get(Fields.ID));
				assertNotNull(data.get(Fields.PROCESS_DEFINITION_ID));
				assertNotNull(data.get(Fields.TENANT_ID));
				
				assertFalse(data.containsKey(Fields.NAME));
				assertFalse(data.containsKey(Fields.BUSINESS_KEY));
				
				assertEquals(testTenant, data.get(Fields.TENANT_ID));
			}
		}
		
		// Cleanup
		for (EventLogEntry eventLogEntry : managementService.getEventLogEntries(null, null)) {
			managementService.deleteEventLogEntry(eventLogEntry.getLogNumber());
		}
		
		repositoryService.deleteDeployment(deploymentId, true);
		
	}
	
	public void testDatabaseEventsNoTenant() throws IOException {
		
		String deploymentId = repositoryService.createDeployment()
				.addClasspathResource("org/activiti/engine/test/api/event/DatabaseEventLoggerProcess.bpmn20.xml")
				.deploy().getId();
		
		// Run process to gather data
		ProcessInstance processInstance = 
				runtimeService.startProcessInstanceByKey("DatabaseEventLoggerProcess", CollectionUtil.singletonMap("testVar", "helloWorld"));
		
		// Verify event log entries
		List<EventLogEntry> eventLogEntries = managementService.getEventLogEntries(null, null);
		
		String processDefinitionId = processInstance.getProcessDefinitionId();
		Iterator<EventLogEntry> iterator = eventLogEntries.iterator();
		while (iterator.hasNext()) {
			EventLogEntry entry = iterator.next();
			if (entry.getProcessDefinitionId() != null && !entry.getProcessDefinitionId().equals(processDefinitionId)) {
				iterator.remove();
			}
		}
		
		assertEquals(15, eventLogEntries.size());
		
		for (int i=0; i< eventLogEntries.size(); i++) {
			
			EventLogEntry entry = eventLogEntries.get(i);
			
			if (i == 0) {
				assertEquals(entry.getType(), ActivitiEventType.VARIABLE_CREATED.name());
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNull(data.get(Fields.TENANT_ID));
			}
			
			// process instance start
			if (i == 1) {
				assertEquals("PROCESSINSTANCE_START", entry.getType());
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNull(data.get(Fields.TENANT_ID));
			}
			
			// Activity started
			if (i == 2 || i == 5 || i == 8 || i == 12) {
				assertEquals(entry.getType(), ActivitiEventType.ACTIVITY_STARTED.name());
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNull(data.get(Fields.TENANT_ID));
			}
			
			
			// Leaving start
			if (i == 3) {
				assertEquals(entry.getType(), ActivitiEventType.ACTIVITY_COMPLETED.name());
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNull(data.get(Fields.TENANT_ID));
			}
			
			// Sequence flow taken
			if (i == 4 || i == 7 || i == 11) {
				assertEquals(entry.getType(), ActivitiEventType.SEQUENCEFLOW_TAKEN.name());
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNull(data.get(Fields.TENANT_ID));
			}
			
			// Leaving parallel gateway
			if (i == 6) {
				assertEquals(entry.getType(), ActivitiEventType.ACTIVITY_COMPLETED.name());
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNull(data.get(Fields.TENANT_ID));
			}
			
			// Tasks
			if (i == 10 || i == 14) {
				assertEquals(entry.getType(), ActivitiEventType.TASK_CREATED.name());
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNull(data.get(Fields.TENANT_ID));
			}
			
			if (i == 9 || i == 13) {
				assertEquals(entry.getType(), ActivitiEventType.TASK_ASSIGNED.name());
				Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>(){});
				assertNull(data.get(Fields.TENANT_ID));
			}
			
		}
		
		repositoryService.deleteDeployment(deploymentId, true);
		
		// Cleanup
		for (EventLogEntry eventLogEntry : managementService.getEventLogEntries(null, null)) {
			managementService.deleteEventLogEntry(eventLogEntry.getLogNumber());
		}
		
		
	}
	
	public void testStandaloneTaskEvents() throws JsonParseException, JsonMappingException, IOException {
		
		Task task = taskService.newTask();
		task.setAssignee("kermit");
		task.setTenantId("myTenant");
		taskService.saveTask(task);
		
		List<EventLogEntry> events = managementService.getEventLogEntries(null, null);
		assertEquals(2, events.size());
		assertEquals("TASK_CREATED", events.get(0).getType());
		assertEquals("TASK_ASSIGNED", events.get(1).getType());
		
		for (EventLogEntry eventLogEntry : events) {
			Map<String, Object> data = objectMapper.readValue(eventLogEntry.getData(), new TypeReference<HashMap<String, Object>>(){});
			assertEquals("myTenant", data.get(Fields.TENANT_ID));
		}
		
		// Cleanup
		taskService.deleteTask(task.getId(),true);
		for (EventLogEntry eventLogEntry : managementService.getEventLogEntries(null, null)) {
			managementService.deleteEventLogEntry(eventLogEntry.getLogNumber());
		}
		
	}
	
	
}
