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

import java.sql.SQLException;

import org.activiti.engine.DbProcessEngineBuilder;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.rules.ExpectedException;

/**
 * @author Tom Baeyens
 */
public class NoDbConnectionTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testNoDbConnection() {
    exception.expect(RuntimeException.class);
    exception.expect(new SqlExceptionMatcher());
    new DbProcessEngineBuilder().configureFromPropertiesResource("org/activiti/test/connection/activiti.properties").buildProcessEngine();
  }

  private static final class SqlExceptionMatcher extends TypeSafeMatcher<RuntimeException> {

    public boolean matchesSafely(RuntimeException e) {
      return containsSqlException(e);
    }
    public void describeTo(Description description) {
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

}
