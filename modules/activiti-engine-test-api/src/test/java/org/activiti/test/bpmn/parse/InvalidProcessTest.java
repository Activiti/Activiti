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
package org.activiti.test.bpmn.parse;

import org.activiti.ActivitiException;
import org.activiti.test.ActivitiTestCase;


/**
 * Test case for verifying if the parser throws validation exceptions
 * when a process definition is given that is not conform the BPMN 2.0 schemas.
 * 
 * @author Joram Barrez
 */
public class InvalidProcessTest extends ActivitiTestCase {
  
  public void testInvalidProcessDefinition() {
    try {
      deployProcessForThisTestMethod();
    } catch (ActivitiException e) {
      assertTextPresent("Attribute 'invalidAttribute' is not allowed to appear in element 'process'",
                        e.getMessage());
    }
  }

}
