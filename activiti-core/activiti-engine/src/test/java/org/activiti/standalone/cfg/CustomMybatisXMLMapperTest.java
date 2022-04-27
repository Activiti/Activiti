/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.standalone.cfg;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;


public class CustomMybatisXMLMapperTest extends ResourceActivitiTestCase {

  public CustomMybatisXMLMapperTest() {
    super("org/activiti/standalone/cfg/custom-mybatis-xml-mappers-activiti.cfg.xml");
  }

  public void testSelectOneTask() {
    // Create test data
    for (int i = 0; i < 4; i++) {
      createTask(i + "", null, null, 0);
    }

    final String taskId = createTask("4", null, null, 0);

    CustomTask customTask = managementService.executeCommand(new Command<CustomTask>() {
      @Override
      public CustomTask execute(CommandContext commandContext) {
        return (CustomTask) commandContext.getDbSqlSession().selectOne("selectOneCustomTask", taskId);
      }
    });

    assertThat(customTask.getName()).isEqualTo("4");

    // test default query as well
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(5);

    Task task = taskService.createTaskQuery().taskName("2").singleResult();
    assertThat(task.getName()).isEqualTo("2");

    // Cleanup
    deleteTasks(taskService.createTaskQuery().list());
  }

  public void testSelectTaskList() {
    // Create test data
    for (int i = 0; i < 5; i++) {
      createTask(i + "", null, null, 0);
    }

    List<CustomTask> tasks = managementService.executeCommand(new Command<List<CustomTask>>() {

      @SuppressWarnings("unchecked")
      @Override
      public List<CustomTask> execute(CommandContext commandContext) {
        return (List<CustomTask>) commandContext.getDbSqlSession().selectList("selectCustomTaskList");
      }
    });

    assertThat(tasks).hasSize(5);

    // Cleanup
    deleteCustomTasks(tasks);
  }

  public void testSelectTasksByCustomQuery() {
    // Create test data
    for (int i = 0; i < 5; i++) {
      createTask(i + "", null, null, 0);
    }
    createTask("Owned task", "kermit", null, 0);

    List<CustomTask> tasks = new CustomTaskQuery(managementService).unOwned().list();

    assertThat(tasks).hasSize(5);
    assertThat(new CustomTaskQuery(managementService).unOwned().count()).isEqualTo(5);

    tasks = new CustomTaskQuery(managementService).list();

    // Cleanup
    deleteCustomTasks(tasks);
  }

  public void testSelectTaskByCustomQuery() {
    // Create test data
    for (int i = 0; i < 5; i++) {
      createTask(i + "", null, null, 0);
    }
    createTask("Owned task", "kermit", null, 0);

    CustomTask task = new CustomTaskQuery(managementService).taskOwner("kermit").singleResult();

    assertThat(task.getOwner()).isEqualTo("kermit");

    List<CustomTask> tasks = new CustomTaskQuery(managementService).list();
    // Cleanup
    deleteCustomTasks(tasks);
  }

  public void testCustomQueryListPage() {
    // Create test data
    for (int i = 0; i < 15; i++) {
      createTask(i + "", null, null, 0);
    }

    List<CustomTask> tasks = new CustomTaskQuery(managementService).listPage(0, 10);

    assertThat(tasks).hasSize(10);

    tasks = new CustomTaskQuery(managementService).list();

    // Cleanup
    deleteCustomTasks(tasks);
  }

  public void testCustomQueryOrderBy() {
    // Create test data
    for (int i = 0; i < 5; i++) {
      createTask(i + "", null, null, i * 20);
    }

    List<CustomTask> tasks = new CustomTaskQuery(managementService).orderByTaskPriority().desc().list();

    assertThat(tasks).hasSize(5);

    for (int i = 0, j = 4; i < 5; i++, j--) {
      CustomTask task = tasks.get(i);
      assertThat(task.getPriority()).isEqualTo(j * 20);
    }

    tasks = new CustomTaskQuery(managementService).orderByTaskPriority().asc().list();

    assertThat(tasks).hasSize(5);

    for (int i = 0; i < 5; i++) {
      CustomTask task = tasks.get(i);
      assertThat(task.getPriority()).isEqualTo(i * 20);
    }
    // Cleanup
    deleteCustomTasks(tasks);
  }

  public void testAttachmentQuery() {
    String taskId = createTask("task1", null, null, 0);

    Authentication.setAuthenticatedUserId("kermit");

    String attachmentId = taskService.createAttachment("image/png", taskId, null, "attachment1", "", "http://activiti.org/").getId();
    taskService.createAttachment("image/jpeg", taskId, null, "attachment2", "Attachment Description", "http://activiti.org/");

    Authentication.setAuthenticatedUserId("gonzo");

    taskService.createAttachment("image/png", taskId, null, "zattachment3", "Attachment Description", "http://activiti.org/");

    Authentication.setAuthenticatedUserId("fozzie");

    for (int i = 0; i < 15; i++) {
      taskService.createAttachment(null, createTask(i + "", null, null, 0), null, "attachmentName" + i, "", "http://activiti.org/" + i);
    }

    assertThat(new AttachmentQuery(managementService).attachmentId(attachmentId).singleResult().getId()).isEqualTo(attachmentId);

    assertThat(new AttachmentQuery(managementService).attachmentName("attachment1").singleResult().getName()).isEqualTo("attachment1");

    assertThat(new AttachmentQuery(managementService).count()).isEqualTo(18);
    List<Attachment> attachments = new AttachmentQuery(managementService).list();
    assertThat(attachments).hasSize(18);

    attachments = new AttachmentQuery(managementService).listPage(0, 10);
    assertThat(attachments).hasSize(10);

    assertThat(new AttachmentQuery(managementService).taskId(taskId).count()).isEqualTo(3);
    attachments = new AttachmentQuery(managementService).taskId(taskId).list();
    assertThat(attachments).hasSize(3);

    assertThat(new AttachmentQuery(managementService).userId("kermit").count()).isEqualTo(2);
    attachments = new AttachmentQuery(managementService).userId("kermit").list();
    assertThat(attachments).hasSize(2);

    assertThat(new AttachmentQuery(managementService).attachmentType("image/jpeg").count()).isEqualTo(1);
    attachments = new AttachmentQuery(managementService).attachmentType("image/jpeg").list();
    assertThat(attachments).hasSize(1);

    assertThat(new AttachmentQuery(managementService).orderByAttachmentName().desc().list().get(0).getName()).isEqualTo("zattachment3");

    // Cleanup
    deleteTasks(taskService.createTaskQuery().list());
  }

  protected String createTask(String name, String owner, String assignee, int priority) {
    Task task = taskService.newTask();
    task.setName(name);
    task.setOwner(owner);
    task.setAssignee(assignee);
    task.setPriority(priority);
    taskService.saveTask(task);
    return task.getId();
  }

  protected void deleteTask(String taskId) {
    taskService.deleteTask(taskId);
    historyService.deleteHistoricTaskInstance(taskId);
  }

  protected void deleteTasks(List<Task> tasks) {
    for (Task task : tasks)
      deleteTask(task.getId());
  }

  protected void deleteCustomTasks(List<CustomTask> tasks) {
    for (CustomTask task : tasks)
      deleteTask(task.getId());
  }
}
