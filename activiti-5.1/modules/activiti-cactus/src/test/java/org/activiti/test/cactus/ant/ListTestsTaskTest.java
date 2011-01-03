package org.activiti.test.cactus.ant;
import java.util.Arrays;
import java.util.List;

import org.activiti.test.cactus.ant.ListTestsTask;

import junit.framework.Assert;
import junit.framework.TestCase;


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
 * @author Frederik Heremans
 */
public class ListTestsTaskTest extends TestCase {

  
  public void testGetClassNameFromFileParts() throws Exception {
    
    ListTestsTask task= new ListTestsTask();
    
    // Matching class
    List<String> parts = Arrays.asList("root", "folder","org", "activiti", "test", "MyTestClass.class");
    Assert.assertEquals("org.activiti.test.MyTestClass", task.getClassNameFromFileParts(parts));
    
    // Matching class on root
    parts = Arrays.asList("org", "activiti", "test", "MyTestClass.class");
    Assert.assertEquals("org.activiti.test.MyTestClass", task.getClassNameFromFileParts(parts));
    
    // Non-matching class
    parts = Arrays.asList("org", "bugus", "test", "MyTestClass.class");
    Assert.assertNull(task.getClassNameFromFileParts(parts));
  }
}
