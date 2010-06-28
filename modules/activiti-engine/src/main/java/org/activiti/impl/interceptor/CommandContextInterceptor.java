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
package org.activiti.impl.interceptor;

import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.impl.cfg.ProcessEngineConfiguration;



/**
 * @author Tom Baeyens
 */
public class CommandContextInterceptor extends Interceptor {
  
  private static Logger log = Logger.getLogger(CommandContextInterceptor.class.getName());
  
  private final ProcessEngineConfiguration processEngineConfiguration;

  public CommandContextInterceptor(ProcessEngineConfiguration processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }

  public <T> T execute(Command<T> command) {
    log.fine("");
    log.fine("=== starting command "+command+" ===========================================");
    CommandContext commandContext = processEngineConfiguration.getCommandContextFactory().createCommandContext(command);
    try {
      T result = next.execute(command);
      return result;
      
    } catch (Throwable exception) {
      commandContext.exception(exception);
      
      if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      } else if (exception instanceof Error) {
        throw (Error) exception;
      }
      throw new ActivitiException(exception.getMessage(), exception);
      
    } finally {
      commandContext.close();        
      log.fine("=== command "+command+" finished ===========================================");
      log.fine("");
    }
  }
}
