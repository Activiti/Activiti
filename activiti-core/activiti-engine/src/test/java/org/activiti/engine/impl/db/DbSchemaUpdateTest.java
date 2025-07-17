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
package org.activiti.engine.impl.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.PropertyEntity;
import org.activiti.engine.impl.test.AbstractTestCase;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

public class DbSchemaUpdateTest extends AbstractTestCase {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withCopyFileToContainer(MountableFile.forHostPath(
                Path.of("target/activiti-engine/org/activiti/db/create/activiti.postgres.create.engine.sql")),
            "/docker-entrypoint-initdb.d/engine.sql"
        )
        .withCopyFileToContainer(MountableFile.forHostPath(
                Path.of("target/activiti-engine/org/activiti/db/create/activiti.postgres.create.history.sql")),
            "/docker-entrypoint-initdb.d/history.sql"
        );

    @Override
    protected void setUp() throws Exception {
        postgres.start();
    }

    @Override
    protected void tearDown() throws Exception {
        postgres.stop();
    }

    public void testDbSchemaUpdateToLatestEngineVersion() {
        // given
        ProcessEngineImpl processEngine = (ProcessEngineImpl) ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
            .setJdbcUrl(postgres.getJdbcUrl())
            .setJdbcUsername(postgres.getUsername())
            .setJdbcPassword(postgres.getPassword())
            .setDbHistoryUsed(true)
            .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
            .buildProcessEngine();

        CommandExecutor commandExecutor = processEngine.getProcessEngineConfiguration().getCommandExecutor();
        CommandConfig config = new CommandConfig().transactionNotSupported();

        // and when
        PropertyEntity schemaVersion = commandExecutor.execute(config,
            commandContext -> commandContext.getDbSqlSession()
                .selectById(PropertyEntity.class,"schema.version"));

        // then
        assertThat(schemaVersion.getValue()).isEqualTo(ProcessEngine.VERSION);

        // and when
        PropertyEntity schemaHistory = commandExecutor.execute(config,
            commandContext -> commandContext.getDbSqlSession()
                .selectById(PropertyEntity.class,"schema.history"));

        // then
        assertThat(schemaHistory.getValue()).contains(ProcessEngine.VERSION);

        processEngine.close();
    }
}
