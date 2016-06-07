package org.activiti.dmn.engine.impl.interceptor;

/**
 * A listener that can be used to be notified of lifecycle events of the command context.
 * 
 * @author Joram Barrez
 */
public interface CommandContextCloseListener {

  void closing(CommandContext commandContext);

  void closed(CommandContext commandContext);

}
