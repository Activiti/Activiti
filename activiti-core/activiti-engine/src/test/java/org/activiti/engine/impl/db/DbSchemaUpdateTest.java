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
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

public class DbSchemaUpdateTest extends AbstractTestCase {


    JdbcDatabaseContainer databaseContainer;

    @Override
    protected void setUp() throws Exception {
        databaseContainer = Database.getInstance().startNewJdbcDatabaseContainer();
    }

    @Override
    protected void tearDown() throws Exception {
        databaseContainer.stop();
    }

    public void testDbSchemaUpdateToLatestEngineVersion() {
        if (databaseContainer==null) {
            return;
        }

        // given
        ProcessEngineImpl processEngine = (ProcessEngineImpl) ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
            .setJdbcUrl(databaseContainer.getJdbcUrl())
            .setJdbcUsername(databaseContainer.getUsername())
            .setJdbcPassword(databaseContainer.getPassword())
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


    public enum Database {
        POSTGRES {
            @Override
            protected JdbcDatabaseContainer startNewJdbcDatabaseContainer() {
                PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:13.7");
                postgreSQLContainer.withCopyFileToContainer(MountableFile.forHostPath(
                            Path.of("target/activiti-engine/org/activiti/db/create/activiti.postgres.create.engine.sql")),
                        "/docker-entrypoint-initdb.d/engine.sql"
                    )
                    .withCopyFileToContainer(MountableFile.forHostPath(
                            Path.of("target/activiti-engine/org/activiti/db/create/activiti.postgres.create.history.sql")),
                        "/docker-entrypoint-initdb.d/history.sql"
                    );
                postgreSQLContainer.start();
                return postgreSQLContainer;
            }
        },
        ORACLE {
            @Override
            protected JdbcDatabaseContainer startNewJdbcDatabaseContainer() {
                OracleContainer oracleContainer = new OracleContainer(
                    "gvenzl/oracle-xe:21-slim-faststart")
                    .withDatabaseName("testDB")
                    .withUsername("testUser")
                    .withPassword("testPassword");
                oracleContainer.withCopyFileToContainer(MountableFile.forHostPath(
                            Path.of("target/activiti-engine/org/activiti/db/create/activiti.oracle.create.engine.sql")),
                        "/docker-entrypoint-initdb.d/engine.sql"
                    )
                    .withCopyFileToContainer(MountableFile.forHostPath(
                            Path.of("target/activiti-engine/org/activiti/db/create/activiti.oracle.create.history.sql")),
                        "/docker-entrypoint-initdb.d/history.sql"
                    );
                oracleContainer.start();
                return oracleContainer;
            }

        },
        MYSQL {
            @Override
            protected JdbcDatabaseContainer startNewJdbcDatabaseContainer() {
                MySQLContainer mySQLContainer = new MySQLContainer("mysql:8");
                mySQLContainer.withCopyFileToContainer(MountableFile.forHostPath(
                            Path.of("target/activiti-engine/org/activiti/db/create/activiti.mysql.create.engine.sql")),
                        "/docker-entrypoint-initdb.d/engine.sql"
                    )
                    .withCopyFileToContainer(MountableFile.forHostPath(
                            Path.of("target/activiti-engine/org/activiti/db/create/activiti.mysql.create.history.sql")),
                        "/docker-entrypoint-initdb.d/history.sql"
                    );
                mySQLContainer.start();
                return mySQLContainer;
            }
        },
        MARIADB {
            @Override
            protected JdbcDatabaseContainer startNewJdbcDatabaseContainer() {
                MariaDBContainer mariaDBContainer = new MariaDBContainer("mariadb:10.6.16");
                mariaDBContainer.withCopyFileToContainer(MountableFile.forHostPath(
                            Path.of("target/activiti-engine/org/activiti/db/create/activiti.mariadb.create.engine.sql")),
                        "/docker-entrypoint-initdb.d/engine.sql"
                    )
                    .withCopyFileToContainer(MountableFile.forHostPath(
                            Path.of("target/activiti-engine/org/activiti/db/create/activiti.mariadb.create.history.sql")),
                        "/docker-entrypoint-initdb.d/history.sql"
                    );
                mariaDBContainer.start();
                return mariaDBContainer;
            }
        },
        MSSQL {
            @Override
            protected JdbcDatabaseContainer startNewJdbcDatabaseContainer() {
                MSSQLServerContainer mssqlserver = new MSSQLServerContainer("mcr.microsoft.com/mssql/server:2019-CU9-ubuntu-16.04")
                    .acceptLicense();
                mssqlserver.addEnv("MSSQL_COLLATION", "LATIN1_GENERAL_100_CS_AS_SC_UTF8");
                mssqlserver.withCopyFileToContainer(MountableFile.forHostPath(
                            Path.of("target/activiti-engine/org/activiti/db/create/activiti.mssql.create.engine.sql")),
                        "/docker-entrypoint-initdb.d/engine.sql"
                    )
                    .withCopyFileToContainer(MountableFile.forHostPath(
                            Path.of("target/activiti-engine/org/activiti/db/create/activiti.mssql.create.history.sql")),
                        "/docker-entrypoint-initdb.d/history.sql"
                    );
                mssqlserver.start();
                return mssqlserver;
            }

        },
        NONE {
            @Override
            protected JdbcDatabaseContainer startNewJdbcDatabaseContainer() {
                return null;
            }
        };

        protected JdbcDatabaseContainer startNewJdbcDatabaseContainer() {
            throw new UnsupportedOperationException(
                String.format(
                    "The [%s] database was not configured to use Testcontainers!",
                    name()
                )
            );
        }

        public static Database getInstance() {
            String database = System.getProperty("db");
            database = database == null ? "NONE" : database.toUpperCase();
            return valueOf(database);
        }

    }


}
