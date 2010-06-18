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
package org.activiti.test.connection;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.activiti.DbProcessEngineBuilder;


/**
 * @author Tom Baeyens
 */
public class NoDbConnectionTest extends TestCase {

  public void testNoDbConnection() {
    try {
      new DbProcessEngineBuilder()
        .configureFromPropertiesResource("org/activiti/test/connection/activiti.properties")
        .buildProcessEngine();
      fail("expected exception");
    } catch (RuntimeException e) {
      if (!containsSqlException(e)) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        fail("expected sql exception.  but was: "+stringWriter.toString());
      }
    }
  }

  protected boolean containsSqlException(Throwable e) {
    if (e==null) {
      return false;
    }
    if (e instanceof SQLException) {
      return true;
    }
    return containsSqlException(e.getCause());
  }
}
