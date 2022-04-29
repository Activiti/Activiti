/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
package org.activiti.engine.test.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;

import junit.framework.TestCase;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.util.ReflectUtil;
import org.apache.ibatis.datasource.pooled.PooledDataSource;


public class DatabaseTablePrefixTest extends TestCase {

  public void testPerformDatabaseSchemaOperationCreate() throws Exception {

    // both process engines will be using this datasource.
    PooledDataSource pooledDataSource = new PooledDataSource(ReflectUtil.getClassLoader(), "org.h2.Driver", "jdbc:h2:mem:activiti-test;DB_CLOSE_DELAY=1000", "sa", "");

    // create two schemas is the database
    Connection connection = pooledDataSource.getConnection();
    connection.createStatement().execute("drop schema if exists SCHEMA1");
    connection.createStatement().execute("drop schema if exists SCHEMA2");
    connection.createStatement().execute("create schema SCHEMA1");
    connection.createStatement().execute("create schema SCHEMA2");
    connection.close();

    // configure & build two different process engines, each having a
    // separate table prefix
    ProcessEngineConfigurationImpl config1 = (ProcessEngineConfigurationImpl) ProcessEngineConfigurationImpl.createStandaloneInMemProcessEngineConfiguration().setDataSource(pooledDataSource)
        .setDatabaseSchemaUpdate("NO_CHECK"); // disable auto create/drop schema
    config1.setDatabaseTablePrefix("SCHEMA1.");
    config1.getPerformanceSettings().setValidateExecutionRelationshipCountConfigOnBoot(false);
    ProcessEngine engine1 = config1.buildProcessEngine();

    ProcessEngineConfigurationImpl config2 = (ProcessEngineConfigurationImpl) ProcessEngineConfigurationImpl.createStandaloneInMemProcessEngineConfiguration().setDataSource(pooledDataSource)
        .setDatabaseSchemaUpdate("NO_CHECK"); // disable auto create/drop schema
    config2.setDatabaseTablePrefix("SCHEMA2.");
    config2.getPerformanceSettings().setValidateExecutionRelationshipCountConfigOnBoot(false);
    ProcessEngine engine2 = config2.buildProcessEngine();

    // create the tables in SCHEMA1
    connection = pooledDataSource.getConnection();
    connection.createStatement().execute("set schema SCHEMA1");
    engine1.getManagementService().databaseSchemaUpgrade(connection, "", "SCHEMA1");
    connection.close();

    // create the tables in SCHEMA2
    connection = pooledDataSource.getConnection();
    connection.createStatement().execute("set schema SCHEMA2");
    engine2.getManagementService().databaseSchemaUpgrade(connection, "", "SCHEMA2");
    connection.close();

    // if I deploy a process to one engine, it is not visible to the other
    // engine:
    try {
      engine1.getRepositoryService().createDeployment().addClasspathResource("org/activiti/engine/test/db/oneJobProcess.bpmn20.xml").deploy();

      assertThat(engine1.getRepositoryService().createDeploymentQuery().count()).isEqualTo(1);
      assertThat(engine2.getRepositoryService().createDeploymentQuery().count()).isEqualTo(0);

    } finally {
      engine1.close();
      engine2.close();
    }
  }

}
