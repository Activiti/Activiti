package org.activiti.test.cactus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestSuite;

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

/**
 * A TestCase that returns a {@link ServletTestSuite} that can be run with
 * cactus inside a containe. This testsuite will run all tests that are
 * configured in the file 'activiti.cactus.tests.txt' on the classpath.
 * 
 * The configured tests can be {@link TestSuite}s or {@link TestCase}s, they
 * will be handled accordingly.
 * 
 * @author Frederik Heremans
 */
public class ActivitiServletTestCase extends TestCase {

  public static Test suite() {
    ServletTestSuite suite = new ServletTestSuite();

    // Add all test class-names that are present in a file on the classpath
    InputStream is = null;
    try {
      is = Thread.currentThread().getContextClassLoader().getResourceAsStream("activiti.cactus.tests.txt");
//      is = ActivitiServletTestCase.class.getClassLoader().getResourceAsStream("activiti.cactus.tests.txt");
      if (is == null) {
        throw new RuntimeException("File activiti.cactus.tests.txt is not found on classpath!");
      }

      List<String> testsToRun = readLines(is);
      for (String testName : testsToRun) {
        addTestToSuite(testName, suite);
      }

    } catch (IOException ioe) {
      throw new RuntimeException("Cannot read activiti cactus test configuration", ioe);
    } finally {
      closeSilently(is);
    }
    return suite;
  }

  private static List<String> readLines(InputStream inputStream) throws IOException {
    List<String> lines = new ArrayList<String>();

    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String line = reader.readLine();
    while (line != null) {
      lines.add(line);
      line = reader.readLine();
    }
    return lines;
  }

  private static void closeSilently(InputStream inputStream) {
    if (inputStream != null) {
      try {
        inputStream.close();
      } catch (IOException ioe) {
        // Ignore
      }
    }
  }

  private static Class< ? > forName(String classname) {
    try {
      return Class.forName(classname);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("A test with name '" + classname + "' is configured, but is not found on the classpath", e);
    }
  }

  private static void addTestToSuite(String classname, ServletTestSuite suite) {

    Class< ? > testClass = forName(classname);

    if (TestSuite.class.isAssignableFrom(testClass)) {
      // Add the test-suite
      suite.addTestSuite(testClass);
    } else if (Test.class.isAssignableFrom(testClass)) {
      // Test case, should be wrapped in TestSuite to have all
      // test-methods turned into a single Test
      TestSuite testSuite = new TestSuite(testClass);
      suite.addTest(testSuite);
    } else {
      throw new RuntimeException("Class " + classname + " is not a TestCase nor a TestSuite");
    }

  }
}
