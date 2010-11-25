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



/**
 * @author Tom Baeyens
 */
public class CommandContextInterceptor extends CommandInterceptor {

  protected CommandContextFactory commandContextFactory;

    public CommandContextInterceptor() {
    }

    public CommandContextInterceptor(CommandContextFactory commandContextFactory) {
        this.commandContextFactory = commandContextFactory;
    }

    public <T> T execute(Command<T> command) {
    CommandContext context = commandContextFactory.createCommandContext(command);

    try {
      CommandContext.setCurrentCommandContext(context);
      return next.execute(command);
      
    } catch (Exception e) {
      context.exception(e);
      
    } finally {
      try {
        context.close();
      } finally {
        CommandContext.removeCurrentCommandContext();
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
}
