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
package org.activiti.test.ldap;

import java.lang.reflect.Method;
import java.util.Vector;

import javax.annotation.Resource;

import junit.framework.Test;

import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.security.ldap.server.ApacheDSContainer;

/**
 * Parts of this class come from
 * http://www.kimchy.org/before_after_testcase_with_junit/
 * 
 * @author Joram Barrez
 */
public class LDAPTestCase extends SpringActivitiTestCase {

  private static int testCount = 0;
  private static int totalTestCount = -1;
  private static boolean disableAfterTestCase = false;
  
  @Resource(name="org.springframework.security.apacheDirectoryServerContainer")
  private ApacheDSContainer apacheDSContainer;

  protected LDAPTestCase() {
    super();
  }

  public void runBare() throws Throwable {
    Throwable exception = null;
    if (totalTestCount == -1) {
      totalTestCount = countTotalTests();
    }
    if (testCount == 0) {
      beforeTestCase();
    }
    testCount++;
    try {
      super.runBare();
    } catch (Throwable running) {
      exception = running;
    }
    if (testCount == totalTestCount) {
      totalTestCount = -1;
      testCount = 0;
      if (!disableAfterTestCase) {
        try {
          afterTestCase();
        } catch (Exception afterTestCase) {
          if (exception == null)
            exception = afterTestCase;
        }
      } else {
        disableAfterTestCase = false;
      }
    }
    if (exception != null)
      throw exception;
  }

  protected static void disableAfterTestCase() {
    disableAfterTestCase = true;
  }

  protected void beforeTestCase() throws Exception {

  }

  protected void afterTestCase() throws Exception {
    // Need to do this 'manually', or otherwise the ldap server won't be shut down properly
    // on the QA machine, failing the next tests
    apacheDSContainer.stop();
  }

  private int countTotalTests() {
    int count = 0;
    Class superClass = getClass();
    Vector names = new Vector();
    while (Test.class.isAssignableFrom(superClass)) {
      Method[] methods = superClass.getDeclaredMethods();
      for (Method method : methods) {
        String name = method.getName();
        if (names.contains(name))
          continue;
        names.addElement(name);
        if (isTestMethod(method)) {
          count++;
        }
      }
      superClass = superClass.getSuperclass();
    }
    return count;
  }

  private boolean isTestMethod(Method m) {
    String name = m.getName();
    Class[] parameters = m.getParameterTypes();
    Class returnType = m.getReturnType();
    return parameters.length == 0 && name.startsWith("test") && returnType.equals(Void.TYPE);
  }

}
