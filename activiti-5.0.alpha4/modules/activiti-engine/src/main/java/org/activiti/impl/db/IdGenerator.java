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

import org.activiti.impl.cmd.GetNextDbidBlockCmd;
import org.activiti.impl.interceptor.CommandExecutor;


/**
 * generates {@link DbidBlock}s that are used to assign ids to new objects.
 * 
 * The scope of an instance of this class is process engine,
 * which means that there is only one instance in one process engine instance.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class IdGenerator {

  protected long nextDbid = 0;
  protected long lastDbid = -1;
  
  protected final CommandExecutor commandExecutor;
  
  public IdGenerator(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public synchronized long getNextDbid() {
    if (lastDbid<nextDbid) {
      getNewBlock();
    }
    return nextDbid++;
  }

  protected synchronized void getNewBlock() {
    DbidBlock dbidBlock = commandExecutor.execute(new GetNextDbidBlockCmd());
    this.nextDbid = dbidBlock.getNextDbid();
    this.lastDbid = dbidBlock.getLastDbid();
  }

}
