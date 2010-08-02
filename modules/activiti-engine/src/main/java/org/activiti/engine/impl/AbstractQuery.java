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
package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.Page;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * Abstract superclass for all query types.
 *  
 * @author Joram Barrez
 */
public abstract class AbstractQuery<T> {
    
  protected CommandExecutor commandExecutor;
 
  protected AbstractQuery() {
  }

  protected AbstractQuery(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public T singleResult() {
    return commandExecutor.execute(new Command<T>() {
      
      public T execute(CommandContext commandContext) {
        return executeSingleResult(commandContext);
      };
    
    });
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

  public abstract long executeCount(CommandContext commandContext);
  
  /**
   * Executes the actual query to retrieve the list of results.
   * @param page used if the results must be paged. If null, no paging will be applied. 
   */
  public abstract List<T> executeList(CommandContext commandContext, Page page);
  
  public T executeSingleResult(CommandContext commandContext) {
    List<T> results = list();
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
     throw new ActivitiException("Query return "+results.size()+" results instead of max 1");
    } 
    return null;
  }
}
