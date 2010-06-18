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
package org.activiti.test.ibatis;

import junit.framework.TestCase;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

/**
 * @author Joram Barrez
 */
public class ProgrammaticSqlSessionFactoryTest extends TestCase {

  public void testProgrammticSqlSessionFactory() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    PooledDataSource dataSource = new PooledDataSource(contextClassLoader, "org.hsqldb.jdbcDriver", "jdbc:hsqldb:.", "sa", "");
    TransactionFactory transactionFactory = new JdbcTransactionFactory(); 
    Environment environment = new Environment("development", transactionFactory, dataSource);
    Configuration configuration = new Configuration(environment);
    configuration.setLazyLoadingEnabled(false);
    
    
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    assertNotNull(sqlSessionFactory);
  }
}
