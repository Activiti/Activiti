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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.Page;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * superclass for all query types that want to offer the list(), paginatedList() and count() operations.
 * 
 * Note that the {@link AbstractSingleResultQuery} is extended, which means that also the 
 * singleResult() operation is offered.
 * 
 * @author Joram Barrez
 */
public abstract class AbstractListQuery<T> extends AbstractSingleResultQuery<T> {
  
  public AbstractListQuery(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  protected T executeSingleResultQuery(CommandContext commandContext) {
    List<T> results = list();
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
     throw new ActivitiException("Multiple query results found when calling singleResult");
    } 
    return null;
  }
  
  public List<T> list() {
    return commandExecutor.execute(new Command<List<T>>() {
      
      public List<T> execute(CommandContext commandContext) {
        return executeList(commandContext, null);
      };
    
    });
  }
  
  public List<T> paginatedList(final int start, final int size) {
    return commandExecutor.execute(new Command<List<T>>() {
      
      public List<T> execute(CommandContext commandContext) {
        return executeList(commandContext, new Page(start, size));
      };
    
    });
  }
  
  public long count() {
    return commandExecutor.execute(new Command<Long>() {
      
      public Long execute(CommandContext commandContext) {
        return executeCount(commandContext);
      };
    
    });
  }

  protected abstract long executeCount(CommandContext commandContext);
  
  /**
   * Executes the actual query to retrieve the list of results.
   * @param page used if the results must be paged. If null, no paging will be applied. 
   */
  protected abstract List<T> executeList(CommandContext commandContext, Page page);

}
