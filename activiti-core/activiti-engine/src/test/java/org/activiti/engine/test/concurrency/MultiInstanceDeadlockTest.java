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
package org.activiti.engine.test.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.CompleteTaskCmd;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Reproduces the deadlock that occurs when {@code DbSqlSession.flushUpdates()}
 * processes entities in non-deterministic order during concurrent multi-instance
 * task completions.
 *
 * <p>Uses a {@link CommandContextCloseListener} barrier to synchronize two
 * threads right before {@code flushSessions()}, maximizing the window for
 * concurrent SQL flushes through Activiti's actual code path.
 */
class MultiInstanceDeadlockTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    private ProcessEngine processEngine;
    private RepositoryService repositoryService;
    private RuntimeService runtimeService;
    private TaskService taskService;
    private ManagementService managementService;

    static volatile CyclicBarrier BARRIER;

    @BeforeEach
    void setUp() {
        postgres.start();

        ProcessEngineConfigurationImpl engineConfig =
            (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
        engineConfig.setJdbcUrl(postgres.getJdbcUrl());
        engineConfig.setJdbcUsername(postgres.getUsername());
        engineConfig.setJdbcPassword(postgres.getPassword());
        engineConfig.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
        engineConfig.setDbHistoryUsed(false);
        engineConfig.setHistoryLevel(HistoryLevel.NONE);
        processEngine = engineConfig.buildProcessEngine();

        repositoryService = processEngine.getRepositoryService();
        runtimeService = processEngine.getRuntimeService();
        taskService = processEngine.getTaskService();
        managementService = processEngine.getManagementService();
    }

    @AfterEach
    void tearDown() {
        if (processEngine != null) {
            processEngine.close();
        }
        postgres.stop();
    }

    @Test
    void testConcurrentUserTaskCompletionCausesDeadlock() throws Exception {
        repositoryService
            .createDeployment()
            .addClasspathResource(
                "org/activiti/engine/test/concurrency/" +
                "MultiInstanceDeadlockTest.testConcurrentUserTaskCompletion.bpmn20.xml"
            )
            .deploy();

        boolean deadlockOccurred = false;

        for (int iteration = 0; iteration < 20; iteration++) {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miDeadlockProcess");

            List<Task> tasks = taskService
                .createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();
            assertThat(tasks).hasSize(2);

            BARRIER = new CyclicBarrier(2);

            AtomicReference<Throwable> exceptionA = new AtomicReference<>();
            AtomicReference<Throwable> exceptionB = new AtomicReference<>();

            Thread threadA = new Thread(() -> {
                try {
                    managementService.executeCommand(
                        new CompleteTaskWithBarrierCmd(tasks.get(0).getId())
                    );
                } catch (Throwable e) {
                    exceptionA.set(e);
                }
            });

            Thread threadB = new Thread(() -> {
                try {
                    managementService.executeCommand(
                        new CompleteTaskWithBarrierCmd(tasks.get(1).getId())
                    );
                } catch (Throwable e) {
                    exceptionB.set(e);
                }
            });

            threadA.start();
            threadB.start();
            threadA.join(15_000);
            threadB.join(15_000);

            if (isDeadlockException(exceptionA.get()) || isDeadlockException(exceptionB.get())) {
                System.out.println("Deadlock detected on iteration " + (iteration + 1));
                deadlockOccurred = true;
                break;
            }

            // Clean up if process still running
            try {
                runtimeService.deleteProcessInstance(processInstance.getId(), "test cleanup");
            } catch (Exception ignored) {
            }
        }

        assertThat(deadlockOccurred)
            .as(
                "Expected a PostgreSQL deadlock when two transactions flush " +
                "entity updates in non-deterministic order during concurrent " +
                "multi-instance task completion"
            )
            .isTrue();
    }

    static class CompleteTaskWithBarrierCmd implements Command<Void> {

        private final String taskId;

        CompleteTaskWithBarrierCmd(String taskId) {
            this.taskId = taskId;
        }

        @Override
        public Void execute(CommandContext commandContext) {
            commandContext.addCloseListener(new DeadlockBarrierCloseListener());
            return new CompleteTaskCmd(taskId, null).execute(commandContext);
        }
    }

    static class DeadlockBarrierCloseListener implements CommandContextCloseListener {

        @Override
        public void closing(CommandContext commandContext) {
            try {
                BARRIER.await(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Barrier interrupted in closing()", e);
            }
        }

        @Override
        public void closed(CommandContext commandContext) {}

        @Override
        public void afterSessionsFlush(CommandContext commandContext) {}

        @Override
        public void closeFailure(CommandContext commandContext) {}
    }

    private boolean isDeadlockException(Throwable exception) {
        if (exception == null) {
            return false;
        }
        Throwable current = exception;
        while (current != null) {
            if (current instanceof SQLException sqlEx) {
                String sqlState = sqlEx.getSQLState();
                if ("40P01".equals(sqlState) || "40001".equals(sqlState)) {
                    return true;
                }
            }
            if (current.getMessage() != null && current.getMessage().contains("deadlock detected")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
