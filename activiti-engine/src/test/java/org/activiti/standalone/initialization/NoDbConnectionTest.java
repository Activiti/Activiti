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
package org.activiti.standalone.initialization;

import java.sql.SQLException;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.test.AbstractTestCase;

/**

 */
public class NoDbConnectionTest extends AbstractTestCase {

  public void testNoDbConnection() {
    try {
      ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("org/activiti/standalone/initialization/nodbconnection.activiti.cfg.xml").buildProcessEngine();
      fail("expected exception");
    } catch (RuntimeException e) {
      assertTrue(containsSqlException(e));
    }
  }

  private boolean containsSqlException(Throwable e) {
    if (e == null) {
      return false;
    }
    if (e instanceof SQLException) {
      return true;
    }
    return containsSqlException(e.getCause());
  }
}
