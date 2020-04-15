package org.activiti.engine.test.api.history;

import static org.activiti.engine.impl.util.CollectionUtil.map;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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
 */
public class ProcessInstanceLogQueryTest extends PluggableActivitiTestCase {

  protected String processInstanceId;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // Deploy test process
    deployTwoTasksTestProcess();

    // Start process instance
    this.processInstanceId = runtimeService.startProcessInstanceByKey("twoTasksProcess", map(
      "var1", "Hello",
      "var2", 123
    )).getId();

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
    assertThat(log.getId()).isNotNull();
    assertThat(log.getProcessDefinitionId()).isNotNull();
    assertThat(log.getStartActivityId()).isNotNull();
    assertThat(log.getDurationInMillis()).isNotNull();
    assertThat(log.getEndTime()).isNotNull();
    assertThat(log.getStartTime()).isNotNull();
  }

  public void testIncludeTasks() {
    ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId).includeTasks().singleResult();
    List<HistoricData> events = log.getHistoricData();
    assertThat(events).hasSize(2);

    for (HistoricData event : events) {
      assertThat(event).isInstanceOf(HistoricTaskInstance.class);
    }
  }

  public void testIncludeComments() {
    ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId).includeComments().singleResult();
    List<HistoricData> events = log.getHistoricData();
    assertThat(events).hasSize(3);

    for (HistoricData event : events) {
      assertThat(event).isInstanceOf(Comment.class);
    }
  }

  public void testIncludeTasksandComments() {
    ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId).includeTasks().includeComments().singleResult();
    List<HistoricData> events = log.getHistoricData();
    assertThat(events).hasSize(5);

    for (int i = 0; i < 5; i++) {
      HistoricData event = events.get(i);
      if (i < 2) { // tasks are created before comments
        assertThat(event).isInstanceOf(HistoricTaskInstance.class);
      } else {
        assertThat(event).isInstanceOf(Comment.class);
      }
    }
  }

  public void testIncludeActivities() {
    ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId).includeActivities().singleResult();
    List<HistoricData> events = log.getHistoricData();
    assertThat(events).hasSize(5);

    for (HistoricData event : events) {
      assertThat(event).isInstanceOf(HistoricActivityInstance.class);
    }
  }

  public void testIncludeVariables() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId).includeVariables().singleResult();
      List<HistoricData> events = log.getHistoricData();
      assertThat(events).hasSize(2);

      for (HistoricData event : events) {
        assertThat(event).isInstanceOf(HistoricVariableInstance.class);
      }
    }
  }

  public void testIncludeVariableUpdates() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId).includeVariableUpdates().singleResult();
      List<HistoricData> events = log.getHistoricData();
      assertThat(events).hasSize(3);

      for (HistoricData event : events) {
        assertThat(event).isInstanceOf(HistoricVariableUpdate.class);
      }
    }
  }

  public void testEverything() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
      ProcessInstanceHistoryLog log = historyService.createProcessInstanceHistoryLogQuery(processInstanceId).includeTasks().includeActivities().includeComments().includeVariables()
          .includeVariableUpdates().singleResult();
      List<HistoricData> events = log.getHistoricData();
      assertThat(events).hasSize(15);
    }
  }

}
