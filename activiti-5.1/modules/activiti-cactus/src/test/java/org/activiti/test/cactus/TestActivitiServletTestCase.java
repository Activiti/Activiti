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

package org.activiti.test.cactus;

import org.apache.cactus.ServletTestSuite;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestResult;


/**
 * @author 'Frederik Heremans'
 */
public class TestActivitiServletTestCase extends TestCase {
  
  public void testActivitiServletTestCase() {
    
    // Call static method "suite" to get the suite
    ServletTestSuite resultingTest = (ServletTestSuite) ActivitiServletTestCase.suite();
    
    // Check the test that is returned
    Assert.assertNotNull(resultingTest);
    Assert.assertEquals(3, resultingTest.countTestCases());
    
    // Execute the test-suite. One test should have finished successfully
    TestResult theResult = new TestResult();
    resultingTest.run(theResult);
    
    Assert.assertEquals(3, theResult.runCount());
    Assert.assertEquals(0, theResult.failureCount());
    Assert.assertEquals(0, theResult.errorCount());
  }
}
