package org.activiti.engine.dynamic;


import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.runtime.ChangeActivityStateBuilderImpl;


public interface DynamicStateManager {

    void moveExecutionState(ChangeActivityStateBuilderImpl changeActivityStateBuilder, CommandContext commandContext);
}
