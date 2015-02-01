/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.db;

import javax.sql.DataSource;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.test.PvmTestCase;
import org.apache.ibatis.datasource.pooled.PooledDataSource;


/**
 * @author Joram Barrez
 */
public class ConnectionPoolTest extends PvmTestCase {
  
  public void testMyBatisConnectionPoolProperlyConfigured() {
    ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/activiti/engine/test/db/connection-pool.activiti.cfg.xml");
    
    config.buildProcessEngine();
    
    // Expected values
    int maxActive = 25;
    int maxIdle = 10;
    int maxCheckoutTime = 30000;
    int maxWaitTime = 25000;
    
    assertEquals(maxActive, config.getJdbcMaxActiveConnections());
    assertEquals(maxIdle, config.getJdbcMaxIdleConnections());
    assertEquals(maxCheckoutTime, config.getJdbcMaxCheckoutTime());
    assertEquals(maxWaitTime, config.getJdbcMaxWaitTime());
    
    // Verify that these properties are correctly set in the MyBatis datasource
    DataSource datasource = config.getDbSqlSessionFactory().getSqlSessionFactory().getConfiguration().getEnvironment().getDataSource();
    assertTrue(datasource instanceof PooledDataSource);
    
    PooledDataSource pooledDataSource = (PooledDataSource) datasource;
    assertEquals(maxActive, pooledDataSource.getPoolMaximumActiveConnections());
    assertEquals(maxIdle, pooledDataSource.getPoolMaximumIdleConnections());
    assertEquals(maxCheckoutTime, pooledDataSource.getPoolMaximumCheckoutTime());
    assertEquals(maxWaitTime, pooledDataSource.getPoolTimeToWait());
  }

}
