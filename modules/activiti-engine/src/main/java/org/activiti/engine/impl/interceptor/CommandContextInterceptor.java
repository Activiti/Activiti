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

package org.activiti.engine.impl.interceptor;


import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.context.ProcessEngineContext;

/**
 * @author Tom Baeyens
 */
public class CommandContextInterceptor extends CommandInterceptor {

  protected CommandContextFactory commandContextFactory;
  protected ProcessEngineContext processEngineContext;

  public CommandContextInterceptor() {
  }

  public CommandContextInterceptor(CommandContextFactory commandContextFactory, ProcessEngineContext processEngineContext) {
    this.commandContextFactory = commandContextFactory;
    this.processEngineContext = processEngineContext;
  }

  public <T> T execute(Command<T> command) {
    CommandContext context = commandContextFactory.createCommandContext(command);

    try {
      CommandContext.setCurrentCommandContext(context);
      Context.setCommandContext(context);
      Context.setProcessEngineContext(processEngineContext);
      return next.execute(command);
      
    } catch (Exception e) {
      context.exception(e);
      
    } finally {
      try {
        context.close();
      } finally {
        CommandContext.removeCurrentCommandContext();
        Context.removeCommandContext();
        Context.removeProcessEngineContext();
      }
    }
    
    return null;
  }
  
  public CommandContextFactory getCommandContextFactory() {
    return commandContextFactory;
  }
  
  public void setCommandContextFactory(CommandContextFactory commandContextFactory) {
    this.commandContextFactory = commandContextFactory;
  }

  public ProcessEngineContext getProcessEngineContext() {
    return processEngineContext;
  }

  public void setProcessEngineContext(ProcessEngineContext processEngineContext) {
    this.processEngineContext = processEngineContext;
  }
}
