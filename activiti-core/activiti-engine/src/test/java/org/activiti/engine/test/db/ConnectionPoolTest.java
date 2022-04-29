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

import javax.sql.DataSource;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.test.AbstractTestCase;
import org.apache.ibatis.datasource.pooled.PooledDataSource;


public class ConnectionPoolTest extends AbstractTestCase {

  public void testMyBatisConnectionPoolProperlyConfigured() {
    ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResource("org/activiti/engine/test/db/connection-pool.activiti.cfg.xml");

    config.buildProcessEngine();

    // Expected values
    int maxActive = 25;
    int maxIdle = 10;
    int maxCheckoutTime = 30000;
    int maxWaitTime = 25000;

    assertThat(config.getJdbcMaxActiveConnections()).isEqualTo(maxActive);
    assertThat(config.getJdbcMaxIdleConnections()).isEqualTo(maxIdle);
    assertThat(config.getJdbcMaxCheckoutTime()).isEqualTo(maxCheckoutTime);
    assertThat(config.getJdbcMaxWaitTime()).isEqualTo(maxWaitTime);

    // Verify that these properties are correctly set in the MyBatis
    // datasource
    DataSource datasource = config.getDbSqlSessionFactory().getSqlSessionFactory().getConfiguration().getEnvironment().getDataSource();
    assertThat(datasource).isInstanceOf(PooledDataSource.class);

    PooledDataSource pooledDataSource = (PooledDataSource) datasource;
    assertThat(pooledDataSource.getPoolMaximumActiveConnections()).isEqualTo(maxActive);
    assertThat(pooledDataSource.getPoolMaximumIdleConnections()).isEqualTo(maxIdle);
    assertThat(pooledDataSource.getPoolMaximumCheckoutTime()).isEqualTo(maxCheckoutTime);
    assertThat(pooledDataSource.getPoolTimeToWait()).isEqualTo(maxWaitTime);
  }

}
