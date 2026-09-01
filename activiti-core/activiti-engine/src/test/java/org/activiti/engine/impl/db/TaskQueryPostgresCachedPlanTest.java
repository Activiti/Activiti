/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.engine.impl.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;

class TaskQueryPostgresCachedPlanTest {

    private static final String ENGINE_NAME = "cachedPlanTestEngine";
    private static final String TASK_NAME = "cached plan task";
    private static final String SUB_TASK_NAME = "cached plan sub task";
    private static final String VARIABLE_NAME = "cachedPlanVariable";
    private static final String VARIABLE_VALUE = "someValue";
    private static final String ASSIGNEE = "kermit";
    private static final int PRIORITY = 42;

    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    private ProcessEngine processEngine;
    private TaskService taskService;

    @BeforeAll
    static void startDatabase() {
        postgres.start();
    }

    @AfterAll
    static void stopDatabase() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        processEngine = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
            .setProcessEngineName(ENGINE_NAME)
            .setJdbcUrl(jdbcUrlWithImmediateServerSidePrepare())
            .setJdbcUsername(postgres.getUsername())
            .setJdbcPassword(postgres.getPassword())
            .setJdbcMaxActiveConnections(1)
            .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
            .buildProcessEngine();
        taskService = processEngine.getTaskService();
    }

    @AfterEach
    void tearDown() {
        processEngine.close();
    }

    @Test
    void should_keepQueryingTasks_when_taskTableGainsColumnAfterStatementsWerePrepared() throws SQLException {
        String taskId = createTask(TASK_NAME, null);
        String subTaskId = createTask(SUB_TASK_NAME, taskId);
        taskService.setVariableLocal(taskId, VARIABLE_NAME, VARIABLE_VALUE);
        prepareStatementsOnServer(taskId);

        addColumnToTaskTable();

        assertThat(queryTasks()).extracting(Task::getId).containsExactlyInAnyOrder(taskId, subTaskId);
        assertThat(taskService.getSubTasks(taskId)).extracting(Task::getId).containsExactly(subTaskId);
        assertThat(queryTaskWithLocalVariables(taskId).getTaskLocalVariables()).containsEntry(
            VARIABLE_NAME,
            VARIABLE_VALUE
        );

        taskService.setAssignee(taskId, ASSIGNEE);
        assertThat(queryTasks())
            .filteredOn(task -> task.getId().equals(taskId))
            .singleElement()
            .satisfies(task -> {
                assertThat(task.getName()).isEqualTo(TASK_NAME);
                assertThat(task.getAssignee()).isEqualTo(ASSIGNEE);
                assertThat(task.getPriority()).isEqualTo(PRIORITY);
            });
    }

    /*
     * prepareThreshold=1 makes the driver switch to a server side prepared statement
     * right after the first execution, which is when PostgreSQL starts caching the plan.
     */
    private void prepareStatementsOnServer(String taskId) {
        queryTasks();
        queryTaskWithLocalVariables(taskId);
        taskService.getSubTasks(taskId);
        taskService.setPriority(taskId, PRIORITY);
    }

    private void addColumnToTaskTable() throws SQLException {
        try (
            Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
            );
            Statement statement = connection.createStatement()
        ) {
            statement.execute("alter table ACT_RU_TASK add column NEW_COL_ varchar(64)");
        }
    }

    private String createTask(String name, String parentTaskId) {
        Task task = taskService.newTask();
        task.setName(name);
        task.setParentTaskId(parentTaskId);
        taskService.saveTask(task);
        return task.getId();
    }

    private List<Task> queryTasks() {
        return taskService.createTaskQuery().orderByTaskName().asc().list();
    }

    private Task queryTaskWithLocalVariables(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).includeTaskLocalVariables().singleResult();
    }

    private static String jdbcUrlWithImmediateServerSidePrepare() {
        String jdbcUrl = postgres.getJdbcUrl();
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "prepareThreshold=1";
    }
}
