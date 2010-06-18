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
package org.activiti.impl.query;

import java.util.List;

import org.activiti.ActivitiException;
import org.activiti.Configuration;
import org.activiti.Page;
import org.activiti.impl.Cmd;
import org.activiti.impl.CmdExecutor;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.tx.TransactionContext;


/**
 * Abstract superclass for all query types.
 * 
 * @author Joram Barrez
 */
public abstract class AbstractQuery<T> {
  
  protected ProcessEngineImpl processEngine;
  
  protected CmdExecutor cmdExecutor;
 
  public AbstractQuery(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
    this.cmdExecutor = processEngine.getConfigurationObject(
           Configuration.NAME_COMMANDEXECUTOR, CmdExecutor.class);
  }
  
  public long count() {
    return cmdExecutor.execute(new Cmd<Long>() {
      
      public Long execute(TransactionContext transactionContext) {
        return executeCount(transactionContext);
      };
    
    }, processEngine);
  }
  
  public List<T> list() {
    return cmdExecutor.execute(new Cmd<List<T>>() {
      
      public List<T> execute(TransactionContext transactionContext) {
        return executeList(transactionContext, null);
      };
    
    }, processEngine);
  }
  
  public List<T> pagedList(final int offset, final int maxResults) {
    return cmdExecutor.execute(new Cmd<List<T>>() {
      
      public List<T> execute(TransactionContext transactionContext) {
        return executeList(transactionContext, new Page(offset, maxResults));
      };
    
    }, processEngine);
  }
  
  public T singleResult() {
    List<T> results = list();
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
     throw new ActivitiException("Multiple query results found when calling singleResult");
    } 
    return null;
  }
  
  protected abstract long executeCount(TransactionContext transactionContext);
  
  /**
   * Executes the actual query to retrieve the list of results.
   * @param page used if the results must be paged. If null, no paging will be applied. 
   */
  protected abstract List<T> executeList(TransactionContext transactionContext, Page page);

}
