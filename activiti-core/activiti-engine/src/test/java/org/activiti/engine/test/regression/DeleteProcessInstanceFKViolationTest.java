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
package org.activiti.engine.test.regression;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeleteProcessInstanceFKViolationTest {

    private ProcessEngine processEngine;
    private ProcessEngineConfigurationImpl processEngineConfiguration;
    private RepositoryService repositoryService;
    private RuntimeService runtimeService;

    @BeforeEach
    void setUp() {
        String processEngineName = "activiti-fk-test-" + UUID.randomUUID();
        processEngine = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
            .setProcessEngineName(processEngineName)
            .setJdbcUrl("jdbc:h2:mem:" + processEngineName + ";DB_CLOSE_DELAY=1000")
            .setJdbcUsername("sa")
            .setJdbcPassword("")
            .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP)
            .buildProcessEngine();

        processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
        repositoryService = processEngine.getRepositoryService();
        runtimeService = processEngine.getRuntimeService();
    }

    @AfterEach
    void tearDown() {
        if (processEngine != null) {
            processEngine.close();
        }
    }

    /**
     * Reproduces the FK violation caused by duplicate-named variables on the same execution.
     *
     * <p>In production, this happens when parallel multi-instance subprocess instances have
     * connector service tasks whose results arrive concurrently. Each result is processed in a
     * separate {@code REQUIRES_NEW} transaction via {@code ServiceTaskIntegrationResultEventHandler}.
     * {@code VariablesPropagator.propagate()} sets output variables on the root process instance.
     * When two concurrent transactions both create a variable with the same name (e.g., "csvFileName"),
     * neither sees the other's uncommitted INSERT, so both create a new row. Result: two DB rows
     * with the same name on the same execution.
     *
     * <p>When the process later completes, {@code deleteExecutionEntity()} previously loaded variables
     * into a {@code HashMap} keyed by name — the duplicate was overwritten. Only one was deleted.
     * The other blocked the execution DELETE with: {@code violates foreign key constraint "act_fk_var_exe"}.
     */
    @Test
    void should_deleteProcessInstance_whenDuplicateNamedVariablesExist() throws Exception {
        repositoryService
            .createDeployment()
            .addClasspathResource("org/activiti/engine/test/regression/deleteProcessFKTest-simpleUserTask.bpmn20.xml")
            .deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleUserTaskProcess");
        String executionId = processInstance.getId();

        try (Connection conn = getConnection()) {
            String byteArrayId = insertByteArray(conn, "var:duplicateVar");
            insertVariable(conn, "duplicateVar", executionId, "value-from-mi-instance-1");
            insertVariable(conn, "duplicateVar", executionId, "longString", byteArrayId);

            assertThat(countVariables(conn, executionId, "duplicateVar"))
                .as("Both duplicate-named variables should exist in DB")
                .isEqualTo(2);
        }

        runtimeService.deleteProcessInstance(processInstance.getId(), "test");

        assertThat(
            runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count()
        ).isZero();

        try (Connection conn = getConnection()) {
            assertThat(countVariablesByProcessInstance(conn, executionId))
                .as("No variables should remain after deletion")
                .isZero();

            assertThat(countByteArraysByName(conn, "var:duplicateVar"))
                .as("No byte arrays should remain after deletion")
                .isZero();
        }
    }

    private Connection getConnection() throws SQLException {
        return processEngineConfiguration.getDataSource().getConnection();
    }

    private void insertVariable(Connection conn, String name, String executionId, String textValue)
        throws SQLException {
        try (
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO ACT_RU_VARIABLE (ID_, REV_, TYPE_, NAME_, EXECUTION_ID_, PROC_INST_ID_, TEXT_) " +
                "VALUES (?, 1, 'string', ?, ?, ?, ?)"
            )
        ) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, name);
            ps.setString(3, executionId);
            ps.setString(4, executionId);
            ps.setString(5, textValue);
            ps.executeUpdate();
        }
    }

    private void insertVariable(Connection conn, String name, String executionId, String type, String byteArrayId)
        throws SQLException {
        try (
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO ACT_RU_VARIABLE (ID_, REV_, TYPE_, NAME_, EXECUTION_ID_, PROC_INST_ID_, BYTEARRAY_ID_) " +
                "VALUES (?, 1, ?, ?, ?, ?, ?)"
            )
        ) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, type);
            ps.setString(3, name);
            ps.setString(4, executionId);
            ps.setString(5, executionId);
            ps.setString(6, byteArrayId);
            ps.executeUpdate();
        }
    }

    private String insertByteArray(Connection conn, String name) throws SQLException {
        String id = UUID.randomUUID().toString();
        try (
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO ACT_GE_BYTEARRAY (ID_, REV_, NAME_, BYTES_) VALUES (?, 1, ?, ?)"
            )
        ) {
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setBytes(3, "test-byte-content".getBytes());
            ps.executeUpdate();
        }
        return id;
    }

    private int countVariables(Connection conn, String executionId, String name) throws SQLException {
        try (
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM ACT_RU_VARIABLE WHERE EXECUTION_ID_ = ? AND NAME_ = ?"
            )
        ) {
            ps.setString(1, executionId);
            ps.setString(2, name);
            return executeCount(ps);
        }
    }

    private int countVariablesByProcessInstance(Connection conn, String processInstanceId) throws SQLException {
        try (
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM ACT_RU_VARIABLE WHERE PROC_INST_ID_ = ?")
        ) {
            ps.setString(1, processInstanceId);
            return executeCount(ps);
        }
    }

    private int countByteArraysByName(Connection conn, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM ACT_GE_BYTEARRAY WHERE NAME_ = ?")) {
            ps.setString(1, name);
            return executeCount(ps);
        }
    }

    private int executeCount(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
