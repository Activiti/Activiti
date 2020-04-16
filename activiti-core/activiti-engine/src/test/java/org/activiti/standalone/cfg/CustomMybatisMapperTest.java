package org.activiti.standalone.cfg;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.cmd.CustomSqlExecution;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.task.Task;

/**

 */
public class CustomMybatisMapperTest extends ResourceActivitiTestCase {

  public CustomMybatisMapperTest() {
    super("org/activiti/standalone/cfg/custom-mybatis-mappers-activiti.cfg.xml");
  }

  public void testSelectTaskColumns() {

    // Create test data
    for (int i = 0; i < 5; i++) {
      Task task = taskService.newTask();
      task.setName(i + "");
      taskService.saveTask(task);
    }

    // Fetch the columns we're interested in
    CustomSqlExecution<MyTestMapper, List<Map<String, Object>>> customSqlExecution = new AbstractCustomSqlExecution<MyTestMapper, List<Map<String, Object>>>(MyTestMapper.class) {

      public List<Map<String, Object>> execute(MyTestMapper customMapper) {
        return customMapper.selectTasks();
      }
    };

    // Verify
    List<Map<String, Object>> tasks = managementService.executeCustomSql(customSqlExecution);
    assertThat(tasks).hasSize(5);
    for (int i = 0; i < 5; i++) {
      Map<String, Object> task = tasks.get(i);
      assertThat(task.get("ID")).isNotNull();
      assertThat(task.get("NAME")).isNotNull();
      assertThat(task.get("CREATETIME")).isNotNull();
    }

    // Cleanup
    for (Task task : taskService.createTaskQuery().list()) {
      taskService.deleteTask(task.getId());
      historyService.deleteHistoricTaskInstance(task.getId());
    }

  }

  public void testFetchTaskWithSpecificVariable() {

    // Create test data
    for (int i = 0; i < 5; i++) {
      Task task = taskService.newTask();
      task.setName(i + "");
      taskService.saveTask(task);

      taskService.setVariable(task.getId(), "myVar", Long.valueOf(task.getId()) * 2);
      taskService.setVariable(task.getId(), "myVar2", "SomeOtherValue");
    }

    // Fetch data with custom query
    CustomSqlExecution<MyTestMapper, List<Map<String, Object>>> customSqlExecution = new AbstractCustomSqlExecution<MyTestMapper, List<Map<String, Object>>>(MyTestMapper.class) {

      public List<Map<String, Object>> execute(MyTestMapper customMapper) {
        return customMapper.selectTaskWithSpecificVariable("myVar");
      }

    };

    // Verify
    List<Map<String, Object>> results = managementService.executeCustomSql(customSqlExecution);
    assertThat(results).hasSize(5);
    for (int i = 0; i < 5; i++) {
      Map<String, Object> result = results.get(i);
      Long id = Long.valueOf((String) result.get("TASKID"));
      Long variableValue = (Long) result.get("VARIABLEVALUE");
      assertThat(variableValue.longValue()).isEqualTo(id * 2);
    }

    // Cleanup
    for (Task task : taskService.createTaskQuery().list()) {
      taskService.deleteTask(task.getId());
      historyService.deleteHistoricTaskInstance(task.getId());
    }

  }

}
