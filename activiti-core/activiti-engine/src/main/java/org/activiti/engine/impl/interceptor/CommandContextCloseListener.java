package org.activiti.engine.impl.interceptor;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.cfg.TransactionContext;

/**
 * A listener that can be used to be notified of lifecycle events of the {@link CommandContext}.
 * 

 */
@Internal
public interface CommandContextCloseListener {

  /**
   * Called when the {@link CommandContext} is being closed, but no 'close logic' has been executed.
   * 
   * At this point, the {@link TransactionContext} (if applicable) has not yet been committed/rolledback 
   * and none of the {@link Session} instances have been flushed.
   * 
   * If an exception happens and it is not caught in this method:
   * - The {@link Session} instances will *not* be flushed
   * - The {@link TransactionContext} will be rolled back (if applicable) 
   */
  void closing(CommandContext commandContext);
  
  /**
   * Called when the {@link Session} have been successfully flushed.
   * When an exception happened during the flushing of the sessions, this method will not be called.
   * 
   * If an exception happens and it is not caught in this method:
   * - The {@link Session} instances will *not* be flushed
   * - The {@link TransactionContext} will be rolled back (if applicable) 
   */
  void afterSessionsFlush(CommandContext commandContext);

  /**
   * Called when the {@link CommandContext} is successfully closed.
   * 
   * At this point, the {@link TransactionContext} (if applicable) has been successfully committed
   * and no rollback has happened. All {@link Session} instances have been closed.
   * 
   * Note that throwing an exception here does *not* affect the transaction. 
   * The {@link CommandContext} will log the exception though.
   */
  void closed(CommandContext commandContext);
  
  /**
   * Called when the {@link CommandContext} has not been successully closed due to an exception that happened.
   * 
   * Note that throwing an exception here does *not* affect the transaction. 
   * The {@link CommandContext} will log the exception though.
   */
  void closeFailure(CommandContext commandContext);

}
