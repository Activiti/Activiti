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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationAware;
import org.activiti.engine.impl.util.ClassNameUtil;

/**
 * @author Tom Baeyens
 * @author Dave Syer
 */
public class DefaultCommandExecutor implements CommandExecutor, ProcessEngineConfigurationAware {

  private static Logger log = Logger.getLogger(DefaultCommandExecutor.class.getName());
  
  protected List<ContextAwareCommandInterceptor> contextAwareInterceptors = new ArrayList<ContextAwareCommandInterceptor>();
  protected List<CommandInterceptor> interceptors = new ArrayList<CommandInterceptor>();
  protected CommandContextFactory commandContextFactory;

  public void configurationCompleted(ProcessEngineConfiguration processEngineConfiguration) {
    this.commandContextFactory = processEngineConfiguration.getCommandContextFactory();
  }

  public DefaultCommandExecutor addContextAwareCommandInterceptor(ContextAwareCommandInterceptor interceptor) {
    contextAwareInterceptors.add(interceptor);
    return this;
  }

  public DefaultCommandExecutor addCommandInterceptor(CommandInterceptor interceptor) {
    interceptors.add(interceptor);
    return this;
  }

  public <T> T execute(Command<T> command) {
    log.fine("                                                                                                    ");
    log.fine("--- starting command " + ClassNameUtil.getClassNameWithoutPackage(command) + " --------------------------------------------------------");
    try {

      return new InternalCommandInterceptorChain(interceptors, contextAwareInterceptors, commandContextFactory).execute(command);

    } catch (Throwable exception) {

      if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      } else if (exception instanceof Error) {
        throw (Error) exception;
      }
      throw new ActivitiException("Command failed with unknown exception.", exception);

    } finally {
      log.fine("--- command " + ClassNameUtil.getClassNameWithoutPackage(command) + " finished --------------------------------------------------------");
      log.fine("                                                                                                    ");
    }
  }

  private static class InternalCommandInterceptorChain implements CommandExecutor {

    private final Iterator<ContextAwareCommandInterceptor> contextAwareInterceptors;
    private final Iterator<CommandInterceptor> interceptors;
    private final InternalCommandContextCreator contextCreator;

    public InternalCommandInterceptorChain(List<CommandInterceptor> interceptors, List<ContextAwareCommandInterceptor> contextAwareInterceptors,
            CommandContextFactory commandContextFactory) {

      ArrayList<CommandInterceptor> unawares = new ArrayList<CommandInterceptor>(interceptors);
      contextCreator = new InternalCommandContextCreator(commandContextFactory);
      unawares.add(contextCreator);

      this.interceptors = unawares.iterator();
      this.contextAwareInterceptors = Collections.unmodifiableList(contextAwareInterceptors).iterator();

    }

    public <T> T execute(Command<T> command) {

      if (interceptors.hasNext()) {
        return interceptors.next().invoke(this, command);
      }

      if (contextAwareInterceptors.hasNext()) {
        return contextAwareInterceptors.next().invoke(this, command, contextCreator.getCommmandContext());
      }

      return command.execute(contextCreator.getCommmandContext());

    }

  }

  private static class InternalCommandContextCreator implements CommandInterceptor {

    private final CommandContextFactory commandContextFactory;
    private CommandContext context;

    public InternalCommandContextCreator(CommandContextFactory commandContextFactory) {
      this.commandContextFactory = commandContextFactory;
    }

    public CommandContext getCommmandContext() {
      return context;
    }

    public <T> T invoke(CommandExecutor next, Command<T> command) {

      context = commandContextFactory.createCommandContext(command);

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
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public CommandContextFactory getCommandContextFactory() {
    return commandContextFactory;
  }

  public void setCommandContextFactory(CommandContextFactory commandContextFactory) {
    this.commandContextFactory = commandContextFactory;
  }
}
