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

package org.activiti.engine.impl.db;

import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationAware;
import org.activiti.engine.impl.cmd.GetNextIdBlockCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * @author Tom Baeyens
 */
public class DbIdGenerator implements IdGenerator, ProcessEngineConfigurationAware  {

  protected long nextId = 0;
  protected long lastId = -1;
  
  protected CommandExecutor commandExecutor;
  
  public void configurationCompleted(ProcessEngineConfiguration processEngineConfiguration) {
    this.commandExecutor = processEngineConfiguration.getCommandExecutor();
  }

  public synchronized long getNextId() {
    if (lastId<nextId) {
      getNewBlock();
    }
    return nextId++;
  }

  protected synchronized void getNewBlock() {
    // TODO http://jira.codehaus.org/browse/ACT-45 use a separate 'requiresNew' command executor
    IdBlock idBlock = commandExecutor.execute(new GetNextIdBlockCmd());
    this.nextId = idBlock.getNextId();
    this.lastId = idBlock.getLastId();
  }
}
