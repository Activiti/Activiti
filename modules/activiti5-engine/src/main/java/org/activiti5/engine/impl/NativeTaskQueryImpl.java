package org.activiti5.engine.impl;

import java.util.List;
import java.util.Map;

import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.interceptor.CommandExecutor;
import org.activiti5.engine.task.NativeTaskQuery;
import org.activiti5.engine.task.Task;


public class NativeTaskQueryImpl extends AbstractNativeQuery<NativeTaskQuery, Task> implements NativeTaskQuery {

  private static final long serialVersionUID = 1L;
  
  public NativeTaskQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeTaskQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }


 //results ////////////////////////////////////////////////////////////////
  
  public List<Task> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return commandContext
      .getTaskEntityManager()
      .findTasksByNativeQuery(parameterMap, firstResult, maxResults);
  }
  
  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    return commandContext
      .getTaskEntityManager()
      .findTaskCountByNativeQuery(parameterMap);
  }

}
