package org.activiti.engine.impl.runtime;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandExecutor;

/**
 * For each API call (and thus {@link Command}) being executed, a new agenda instance is created.
 * On this agenda, operations are put, which the {@link CommandExecutor} will keep executing until
 * all are executed.
 *
 * The agenda also gives easy access to methods to plan new operations when writing
 * {@link ActivityBehavior} implementations.
 *
 * During a {@link Command} execution, the agenda can always be fetched using {@link Context#getAgenda()}.
 */
public interface Agenda {

  boolean isEmpty();

  Runnable getNextOperation();

  /**
   * Generic method to plan a {@link Runnable}.
   */
  void planOperation(Runnable operation);

}
