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
      createTask(i + "", null, null, 0);
    }
    
    Task task = managementService.executeCommand(new Command<Task>() {
      @Override
      public Task execute(CommandContext commandContext) {
        return (Task) commandContext.getDbSqlSession().selectOne("customSelectOneTask", "2");
      }
    });
    
    assertEquals("2", task.getName());
    
    // test default query as well
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(5, tasks.size());
    
    task = taskService.createTaskQuery().taskName("2").singleResult();
    assertEquals("2", task.getName());
    
    // Cleanup
    deleteTasks(taskService.createTaskQuery().list());
  }
  
  public void testSelectTaskList() {
    // Create test data
    for (int i=0; i<5; i++) {
      createTask(i + "", null, null, 0);
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
    deleteTasks(tasks);
  }
  
  public void testSelectTasksByCustomQuery() {
    // Create test data
    for (int i=0; i<5; i++) {
      createTask(i + "", null, null, 0);
    }
    createTask("Owned task", "kermit", null, 0);
    
    CommandExecutor commandExecutor = ((ManagementServiceImpl) managementService).getCommandExecutor();
    List<Task> tasks = new CustomTaskQuery(commandExecutor).unOwned().list();
    
    assertEquals(5, tasks.size());
    assertEquals(5, new CustomTaskQuery(commandExecutor).unOwned().count());
    
    tasks = new CustomTaskQuery(commandExecutor).list();
    
    // Cleanup
    deleteTasks(tasks);
  }
  
  public void testSelectTaskByCustomQuery() {
    // Create test data
    for (int i=0; i<5; i++) {
      createTask(i + "", null, null, 0);
    }
    createTask("Owned task", "kermit", null, 0);
    
    CommandExecutor commandExecutor = ((ManagementServiceImpl) managementService).getCommandExecutor();
    Task task = new CustomTaskQuery(commandExecutor).taskOwner("kermit").singleResult();
    
    assertEquals("kermit", task.getOwner());
    
    List<Task> tasks = new CustomTaskQuery(commandExecutor).list();
    // Cleanup
    deleteTasks(tasks);
  }
  
  public void testCustomQueryListPage() {
    // Create test data
    for (int i=0; i<15; i++) {
      createTask(i + "", null, null, 0);
    }
    
    CommandExecutor commandExecutor = ((ManagementServiceImpl) managementService).getCommandExecutor();
    List<Task> tasks = new CustomTaskQuery(commandExecutor).listPage(0, 10);
    
    assertEquals(10, tasks.size());
    
    tasks = new CustomTaskQuery(commandExecutor).list();
    
    // Cleanup
    deleteTasks(tasks);
  }
  
  public void testCustomQueryOrderBy() {
    // Create test data
    for (int i=0; i<5; i++) {
      createTask(i + "", null, null, i*20);
    }
    
    CommandExecutor commandExecutor = ((ManagementServiceImpl)managementService).getCommandExecutor();
    List<Task> tasks = new CustomTaskQuery(commandExecutor).orderByTaskPriority().desc().list();
    
    assertEquals(5, tasks.size());
    
    for (int i=0,j=4; i<5; i++,j--) {
      Task task = tasks.get(i);
      assertEquals(j*20, task.getPriority());
    }
    
    tasks = new CustomTaskQuery(commandExecutor).orderByTaskPriority().asc().list();
    
    assertEquals(5, tasks.size());
    
    for (int i=0; i<5; i++) {
      Task task = tasks.get(i);
      assertEquals(i*20, task.getPriority());
    }
    // Cleanup
    deleteTasks(tasks);
  }
  
  protected void createTask(String name, String owner, String assignee, int priority){
    Task task = taskService.newTask();
    task.setName(name);
    task.setOwner(owner);
    task.setAssignee(assignee);
    task.setPriority(priority);
    taskService.saveTask(task);
  }
  
  protected void deleteTask(Task task){
    taskService.deleteTask(task.getId());
    historyService.deleteHistoricTaskInstance(task.getId());
  }
  
  protected void deleteTasks(List<Task> tasks){
    for (Task task : tasks) 
      deleteTask(task);
  }
}
