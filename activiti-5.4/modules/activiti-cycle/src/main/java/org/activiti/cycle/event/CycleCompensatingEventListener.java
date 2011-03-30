package org.activiti.cycle.event;

/**
 * 
 * @author daniel.meyer@camunda.com
 */
public interface CycleCompensatingEventListener<T> extends CycleEventListener<T> {

  public void compensateEvent(T event);

}
