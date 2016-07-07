package org.activiti.engine.test.api.history;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricData;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.history.ProcessInstanceHistoryLog;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;

/**
 * @author Daisuke Yoshimoto
 */
public class ProcessInstanceLogQueryAndByteArrayTypeVariableTest extends PluggableActivitiTestCase {
	
	protected String processInstanceId;
	
	private static String LARGE_STRING_VALUE;
	
	static {
		StringBuilder sb = new StringBuilder("a");
		for(int i = 0; i < 4001; i++) {
		     sb.append("a");
		}
		LARGE_STRING_VALUE = sb.toString();
	}
	
	@Override
	protected void setUp() throws Exception {
	  super.setUp();
	  
	  // Deploy test process
	  deployTwoTasksTestProcess();
	  
	  // Start process instance
	  Map<String, Object> vars = new HashMap<String, Object>();
	  // ByteArrayType Variable
	  vars.put("var", LARGE_STRING_VALUE);
	  this.processInstanceId = runtimeService.startProcessInstanceByKey("twoTasksProcess", vars).getId();
	  
	  // Finish tasks
	  for (Task task : taskService.createTaskQuery().list()) {
	  	taskService.complete(task.getId());
	  }
	}
	
	@Override
	protected void tearDown() throws Exception {
	  super.tearDown();
	}

	public void testIncludeVariables() {
		if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
			
			HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery()
					.processInstanceId(processInstanceId).variableName("var").singleResult();
			assertEquals(historicVariableInstance.getValue(), LARGE_STRING_VALUE);
			
			ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId)
				.includeVariables()
				.singleResult();
			List<HistoricData> events = log.getHistoricData();
			assertEquals(1, events.size());
			
			for (HistoricData event : events) {
				assertTrue(event instanceof HistoricVariableInstance);
				assertEquals(((HistoricVariableInstanceEntity) event).getValue(), LARGE_STRING_VALUE);
			}
		}
	}
	
	public void testIncludeVariableUpdates() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      
      HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery()
          .processInstanceId(processInstanceId).variableName("var").singleResult();
      assertEquals(historicVariableInstance.getValue(), LARGE_STRING_VALUE);
      
      ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId)
        .includeVariableUpdates()
        .singleResult();
      List<HistoricData> events = log.getHistoricData();
      assertEquals(1, events.size());
      
      for (HistoricData event : events) {
        assertTrue(event instanceof HistoricVariableUpdate);
        assertEquals(((HistoricDetailVariableInstanceUpdateEntity) event).getValue(), LARGE_STRING_VALUE);
      }
    }
  }
}
