package org.activiti5.standalone.cfg;

import java.util.List;
import java.util.Map;

import org.activiti.engine.task.Task;
import org.activiti5.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti5.engine.impl.cmd.CustomSqlExecution;
import org.activiti5.engine.impl.test.ResourceActivitiTestCase;

/**
 * @author jbarrez
 */
public class CustomMybatisMapperTest extends ResourceActivitiTestCase {
	
	public CustomMybatisMapperTest() {
		super("org/activiti5/standalone/cfg/custom-mybatis-mappers-activiti.cfg.xml");
	}
	
	public void testSelectTaskColumns() {
		
		// Create test data
		for (int i=0; i<5; i++) {
			Task task = taskService.newTask();
			task.setName(i + "");
			taskService.saveTask(task);
		}
		
		// Fetch the columns we're interested in
		CustomSqlExecution<MyTestMapper, List<Map<String, Object>>> customSqlExecution = 
				new AbstractCustomSqlExecution<MyTestMapper, List<Map<String, Object>>>(MyTestMapper.class) {
			
			public List<Map<String, Object>> execute(MyTestMapper customMapper) {
				return customMapper.selectTasks();
			}
		
		};
		
		org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5ProcessEngineConfig = (org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl)
        processEngineConfiguration.getActiviti5CompatibilityHandler().getRawProcessConfiguration();
		
		// Verify
		List<Map<String, Object>> tasks = activiti5ProcessEngineConfig.getManagementService().executeCustomSql(customSqlExecution);
		assertEquals(5, tasks.size());
		for (int i=0; i<5; i++) {
			Map<String, Object> task = tasks.get(i);
			assertNotNull(task.get("ID"));
			assertNotNull(task.get("NAME"));
			assertNotNull(task.get("CREATETIME"));
		}
		
		// Cleanup
		for (Task task : taskService.createTaskQuery().list()) {
			taskService.deleteTask(task.getId());
			historyService.deleteHistoricTaskInstance(task.getId());
		}
		
	}
	
	public void testFetchTaskWithSpecificVariable() {
		
		// Create test data
		for (int i=0; i<5; i++) {
			Task task = taskService.newTask();
			task.setName(i + "");
			taskService.saveTask(task);
			
			taskService.setVariable(task.getId(), "myVar", Long.valueOf(task.getId()) * 2);
			taskService.setVariable(task.getId(), "myVar2", "SomeOtherValue");
		}
		
		// Fetch data with custom query
		CustomSqlExecution<MyTestMapper, List<Map<String, Object>>> customSqlExecution = 
				new AbstractCustomSqlExecution<MyTestMapper, List<Map<String, Object>>>(MyTestMapper.class) {
			
			public List<Map<String, Object>> execute(MyTestMapper customMapper) {
				return customMapper.selectTaskWithSpecificVariable("myVar");
			}
		
		};
		
		// Verify
		org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5ProcessEngineConfig = (org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl)
        processEngineConfiguration.getActiviti5CompatibilityHandler().getRawProcessConfiguration();
		
		List<Map<String, Object>> results = activiti5ProcessEngineConfig.getManagementService().executeCustomSql(customSqlExecution);
		assertEquals(5, results.size());
		for (int i=0; i<5; i++) {
			Map<String, Object> result = results.get(i);
			Long id = Long.valueOf((String) result.get("TASKID"));
			Long variableValue = (Long) result.get("VARIABLEVALUE");
			assertEquals(id * 2, variableValue.longValue());
		}
		
		// Cleanup
		for (Task task : taskService.createTaskQuery().list()) {
			taskService.deleteTask(task.getId());
			historyService.deleteHistoricTaskInstance(task.getId());
		}
		
	}
	
}
