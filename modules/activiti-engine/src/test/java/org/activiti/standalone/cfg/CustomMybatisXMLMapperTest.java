package org.activiti.standalone.cfg;

import java.util.List;

import org.activiti.engine.impl.ManagementServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.task.Task;

/**
 * @author Bassam Al-Sarori
 */
public class CustomMybatisXMLMapperTest extends ResourceActivitiTestCase {

  public CustomMybatisXMLMapperTest() {
    super("org/activiti/standalone/cfg/custom-mybatis-xml-mappers-activiti.cfg.xml");
  }

  public void testSelectOneTask() {
    // Create test data
    for (int i=0; i<5; i++) {
      Task task = taskService.newTask();
      task.setName(i + "");
      taskService.saveTask(task);
    }
    
    Task task = managementService.executeCommand(new Command<Task>() {
      @Override
      public Task execute(CommandContext commandContext) {
        return (Task) commandContext.getDbSqlSession().selectOne("customSelectOneTask", "2");
      }
    });
    
    assertEquals("2", task.getName());
    
    // Cleanup
    for (Task taskToDelete : taskService.createTaskQuery().list()) {
      taskService.deleteTask(taskToDelete.getId());
      historyService.deleteHistoricTaskInstance(taskToDelete.getId());
    }
  }
  
  public void testSelectTaskList() {
    // Create test data
    for (int i=0; i<5; i++) {
      Task task = taskService.newTask();
      task.setName(i + "");
      taskService.saveTask(task);
    }
    
    List<Task> tasks = managementService.executeCommand(new Command<List<Task>>() {

      @SuppressWarnings("unchecked")
      @Override
      public List<Task> execute(CommandContext commandContext) {
        return (List<Task>) commandContext.getDbSqlSession().selectListWithRawParameter("customSelectTaskList", 0, 0, Integer.MAX_VALUE);
      }
    });
    
    assertEquals(5, tasks.size());
    
    // Cleanup
    for (Task taskToDelete : tasks) {
      taskService.deleteTask(taskToDelete.getId());
      historyService.deleteHistoricTaskInstance(taskToDelete.getId());
    }
  }
  
  public void testSelectTasksByCustomQuery() {
    // Create test data
    for (int i=0; i<5; i++) {
      Task task = taskService.newTask();
      task.setName(i + "");
      taskService.saveTask(task);
    }
    Task task = taskService.newTask();
    task.setOwner("kermit");
    task.setName("Owned task");
    taskService.saveTask(task);
    
    CommandExecutor commandExecutor = ((ManagementServiceImpl)managementService).getCommandExecutor();
    List<Task> tasks = new CustomTaskQuery(commandExecutor).unOwned().list();
    
    assertEquals(5, tasks.size());
    assertEquals(5, new CustomTaskQuery(commandExecutor).unOwned().count());
    
    tasks = new CustomTaskQuery(commandExecutor).list();
    // Cleanup
    for (Task taskToDelete : tasks) {
      taskService.deleteTask(taskToDelete.getId());
      historyService.deleteHistoricTaskInstance(taskToDelete.getId());
    }
  }
  
  public void testSelectTaskByCustomQuery() {
    // Create test data
    for (int i=0; i<5; i++) {
      Task task = taskService.newTask();
      task.setName(i + "");
      taskService.saveTask(task);
    }
    Task task = taskService.newTask();
    task.setOwner("kermit");
    task.setName("Owned task");
    taskService.saveTask(task);
    
    CommandExecutor commandExecutor = ((ManagementServiceImpl)managementService).getCommandExecutor();
    task = new CustomTaskQuery(commandExecutor).taskOwner("kermit").singleResult();
    
    assertEquals("kermit", task.getOwner());
    
    List<Task> tasks = new CustomTaskQuery(commandExecutor).list();
    // Cleanup
    for (Task taskToDelete : tasks) {
      taskService.deleteTask(taskToDelete.getId());
      historyService.deleteHistoricTaskInstance(taskToDelete.getId());
    }
  }
}
