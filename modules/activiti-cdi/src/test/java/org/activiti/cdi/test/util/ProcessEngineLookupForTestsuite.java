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
package org.activiti.cdi.test.util;

import org.activiti.cdi.spi.ProcessEngineLookup;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;

/**
 * @author Daniel Meyer
 */
public class ProcessEngineLookupForTestsuite implements ProcessEngineLookup {

  public static ProcessEngine processEngine;
  
  @Override
  public int getPrecedence() {
    return 100;
  }

  @Override
  public ProcessEngine getProcessEngine() {
    if(processEngine == null) {
      processEngine = ProcessEngines.getDefaultProcessEngine();
    }
    return processEngine;
  }
  
  @Override
  public void ungetProcessEngine() {
    // do nothing
  }

}
