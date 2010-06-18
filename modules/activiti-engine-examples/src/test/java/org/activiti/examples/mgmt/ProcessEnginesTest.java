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
package org.activiti.examples.mgmt;

import java.util.List;

import junit.framework.TestCase;

import org.activiti.ProcessEngine;
import org.activiti.ProcessEngineInfo;
import org.activiti.ProcessEngines;
import org.activiti.test.ProcessEngineTestCase;


/**
 * @author Tom Baeyens
 */
public class ProcessEnginesTest extends TestCase {
  
  protected void setUp() throws Exception {
    // in case previous ProcessEngineTestCase s (or ActivitiTestCase s)
    // already have inialized and cached a process engine, it needs to 
    // be closed first.
    ProcessEngineTestCase.closeProcessEngine();
  }

  public void testProcessEngineInfo() {
    ProcessEngines.init();
    
    List<ProcessEngineInfo> processEngineInfos = ProcessEngines.getProcessEngineInfos();
    assertEquals(1, processEngineInfos.size());
    
    ProcessEngineInfo processEngineInfo = processEngineInfos.get(0);
    assertNull(processEngineInfo.getException()); 
    assertNotNull(processEngineInfo.getName()); 
    assertNotNull(processEngineInfo.getResourceUrl()); 
    
    ProcessEngine processEngine = ProcessEngines.getProcessEngine(ProcessEngines.NAME_DEFAULT);
    assertNotNull(processEngine);
    
    ProcessEngines.destroy();
  }
}
