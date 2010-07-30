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

package org.activiti.engine.test;

import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.util.LogUtil.ThreadLogMode;


/** JUnit 3 style base class that also exposes selected implementation 
 * aspects of the engine like the CommandExecutor.
 *  
 * @author Tom Baeyens
 */
public class ProcessEngineImplTestCase extends ProcessEngineTestCase {

  protected ProcessEngineConfiguration processEngineConfiguration;
  
  public ProcessEngineImplTestCase() {
    super();
  }

  public ProcessEngineImplTestCase(String configurationResource, ThreadLogMode threadRenderingMode) {
    super(configurationResource, threadRenderingMode);
  }

  public ProcessEngineImplTestCase(String configurationResource) {
    super(configurationResource);
  }

  public ProcessEngineImplTestCase(ThreadLogMode threadRenderingMode) {
    super(threadRenderingMode);
  }

  @Override
  void initializeServices() {
    super.initializeServices();
    processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
  }
}
