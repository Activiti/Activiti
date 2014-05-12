package org.activiti.crystalball.simulator.delegate.event;

/**
 *
 * @param <S> function's input
 * @param <D> function's output
 */
public interface Function<S, D> {
  D apply(S sourceObject);
}
