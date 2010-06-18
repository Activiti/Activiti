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
package org.activiti.impl.db;

import org.activiti.Configuration;
import org.activiti.impl.CmdExecutor;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.cmd.GetNextDbidBlockCmd;
import org.activiti.impl.tx.TransactionContext;


/**
 * generates {@link DbidBlock}s that are used to assign ids to new objects.
 * 
 * The scope of an instance of this class is process engine,
 * which means that there is only one instance in one process engine instance.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DbidGenerator {

  protected long nextDbid = 0;
  protected long lastDbid = -1;
  
  public synchronized long getNextDbid() {
    if (lastDbid<nextDbid) {
      getNewBlock();
    }
    return nextDbid++;
  }

  protected void getNewBlock() {
    // TODO extend configuration capabilities so that process engine en cmd executor can be injected
    
    ProcessEngineImpl processEngine = TransactionContext
      .getCurrent()
      .getProcessEngine();
    
    CmdExecutor cmdExecutor = processEngine
      .getConfigurationObject(Configuration.NAME_COMMANDEXECUTOR, CmdExecutor.class);
    
    DbidBlock dbidBlock = cmdExecutor.execute(new GetNextDbidBlockCmd(), processEngine);
    this.nextDbid = dbidBlock.getNextDbid();
    this.lastDbid = dbidBlock.getLastDbid();
  }
}
