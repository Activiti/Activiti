package org.activiti.engine.impl.delegate;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * If the behaviour of an element in a process implements this interface, it has a 'background job' functionality.
 * 
 * The instance will be called at the end of executing the engine operations for each {@link ExecutionEntity} that currently is at the activity AND is inactive.
 * 

 */
@Internal
public interface InactiveActivityBehavior {

  void executeInactive(ExecutionEntity executionEntity);

}
