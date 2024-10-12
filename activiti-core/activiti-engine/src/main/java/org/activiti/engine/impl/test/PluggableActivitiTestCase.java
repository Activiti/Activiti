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


package org.activiti.engine.impl.test;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.CommandExecutorImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.CommandInvoker;
import org.activiti.engine.impl.interceptor.DebugCommandInvoker;
import org.activiti.engine.impl.interceptor.RetryInterceptor;
import org.activiti.engine.test.EnableVerboseExecutionTreeLogging;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for the activiti test cases.
 *
 * The main reason not to use our own test support classes is that we need to run our test suite with various configurations, e.g. with and without spring, standalone or on a server etc. Those
 * requirements create some complications so we think it's best to use a separate base class. That way it is much easier for us to maintain our own codebase and at the same time provide stability on
 * the test support classes that we offer as part of our api (in org.activiti.engine.test).
 *


 */
public abstract class PluggableActivitiTestCase extends AbstractActivitiTestCase {

  private static Logger pluggableActivitiTestCaseLogger = LoggerFactory.getLogger(PluggableActivitiTestCase.class);


    @Rule
    public static JdbcDatabaseContainer databaseContainer = Database.getInstance().startNewJdbcDatabaseContainer();

    public static class JDBCProperties {

        private JDBCProperties() {
        }

        public static String getUrl() {
            if (databaseContainer == null) return "jdbc:h2:mem:activiti;DB_CLOSE_DELAY=-1";
            return databaseContainer.getJdbcUrl();
        }

        public static String getUsername() {
            if (databaseContainer == null) return "sa";
            return databaseContainer.getUsername();
        }

        public static String getPassword() {
            if (databaseContainer == null) return "";
            return databaseContainer.getPassword();
        }

        public static String getDriver() {
            if (databaseContainer == null) return "org.h2.Driver";
            return databaseContainer.getDriverClassName();
        }

        public static JDBCProperties getInstance() {
            return new JDBCProperties();
        }

    }


  protected static ProcessEngine cachedProcessEngine;

  protected void initializeProcessEngine() {
    if (cachedProcessEngine == null) {

      pluggableActivitiTestCaseLogger.info("No cached process engine found for test. Retrieving the default engine.");
      ProcessEngines.destroy(); // Just to be sure we're not getting any previously cached version

      cachedProcessEngine = ProcessEngines.getDefaultProcessEngine();
      if (cachedProcessEngine == null) {
        throw new ActivitiException("no default process engine available");
      }
    }

    processEngine = cachedProcessEngine;
    processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();

    // Enable verbose execution tree debugging if needed
    if (this.getClass().isAnnotationPresent(EnableVerboseExecutionTreeLogging.class)) {
      swapCommandInvoker(true);
    }

  }

  @Override
  protected void closeDownProcessEngine() {
    super.closeDownProcessEngine();

    // Reset command invoker
    if (this.getClass().isAnnotationPresent(EnableVerboseExecutionTreeLogging.class)) {
      swapCommandInvoker(false);
    }
  }

  protected void swapCommandInvoker(boolean debug) {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    if (commandExecutor instanceof CommandExecutorImpl) {
      CommandExecutorImpl commandExecutorImpl = (CommandExecutorImpl) commandExecutor;

      CommandInterceptor previousCommandInterceptor = null;
      CommandInterceptor commandInterceptor = commandExecutorImpl.getFirst();

      while (commandInterceptor != null) {

        boolean matches = debug ? (commandInterceptor instanceof CommandInvoker) : (commandInterceptor instanceof DebugCommandInvoker);
        if (matches) {

          CommandInterceptor commandInvoker = debug ? new DebugCommandInvoker() : new CommandInvoker();
          if (previousCommandInterceptor != null) {
            previousCommandInterceptor.setNext(commandInvoker);
          } else {
            commandExecutorImpl.setFirst(previousCommandInterceptor);
          }
          break;

        } else {
          previousCommandInterceptor = commandInterceptor;
          commandInterceptor = commandInterceptor.getNext();
        }
      }

    } else {
      pluggableActivitiTestCaseLogger.warn("Not using " + CommandExecutorImpl.class + ", ignoring the "
          + EnableVerboseExecutionTreeLogging.class + " annotation");
    }
  }

  protected void withRetryInterceptor(Runnable runnable) {
    final CommandExecutorImpl commandExecutor = CommandExecutorImpl.class
        .cast(processEngineConfiguration.getCommandExecutor());

    final CommandInterceptor original = commandExecutor.getFirst();

    try {
        final RetryInterceptor retryInterceptor = new RetryInterceptor();

        retryInterceptor.setNext(original);
        commandExecutor.setFirst(retryInterceptor);

        runnable.run();
    } finally {
        commandExecutor.setFirst(original);
    }
  }

    public enum Database {
        POSTGRES {
            @Override
            protected JdbcDatabaseContainer startNewJdbcDatabaseContainer() {
                PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:13.7");
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

                oracleContainer.start();
                return oracleContainer;
            }

        },
        MYSQL {
            @Override
            protected JdbcDatabaseContainer startNewJdbcDatabaseContainer() {
                MySQLContainer mySQLContainer = new MySQLContainer("mysql:8");
                mySQLContainer.start();
                return mySQLContainer;
            }
        },
        MARIADB {
            @Override
            protected JdbcDatabaseContainer startNewJdbcDatabaseContainer() {
                MariaDBContainer mariaDBContainer = new MariaDBContainer("mariadb:10.6.16");
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
            pluggableActivitiTestCaseLogger.info("Initiating " + database + " container");
            return valueOf(database);
        }

    }

}
