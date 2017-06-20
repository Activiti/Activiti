package org.activiti.engine.impl.cfg;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.interceptor.CommandInterceptor;

/**
 * Command executor that passes commands to the first interceptor in the chain. If no {@link CommandConfig} is passed, the default configuration will be used.
 * 


 */
public class CommandExecutorImpl implements CommandExecutor {

  protected CommandConfig defaultConfig;
  protected CommandInterceptor first;

  public CommandExecutorImpl(CommandConfig defaultConfig, CommandInterceptor first) {
    this.defaultConfig = defaultConfig;
    this.first = first;
  }

  public CommandInterceptor getFirst() {
    return first;
  }
  
  public void setFirst(CommandInterceptor commandInterceptor) {
    this.first = commandInterceptor;
  }

  @Override
  public CommandConfig getDefaultConfig() {
    return defaultConfig;
  }

  @Override
  public <T> T execute(Command<T> command) {
    return execute(defaultConfig, command);
  }

  @Override
  public <T> T execute(CommandConfig config, Command<T> command) {
    return first.execute(config, command);
  }

}
