package org.activiti.engine.impl;

import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.NativeExecutionQuery;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;


public class NativeExecutionQueryImpl extends AbstractNativeQuery<NativeExecutionQuery, Execution> implements NativeExecutionQuery {

  private static final long serialVersionUID = 1L;

  public NativeExecutionQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeExecutionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }


 //results ////////////////////////////////////////////////////////////////
  
  public List<Execution> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return commandContext
      .getExecutionManager()
      .findExecutionsByNativeQuery(parameterMap, firstResult, maxResults);
  }
  
  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    return commandContext
      .getExecutionManager()
      .findExecutionCountByNativeQuery(parameterMap);
  }

}
