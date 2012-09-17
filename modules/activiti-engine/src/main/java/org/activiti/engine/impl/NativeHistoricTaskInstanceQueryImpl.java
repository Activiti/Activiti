package org.activiti.engine.impl;

import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.NativeHistoricTaskInstanceQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.NativeExecutionQuery;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;


public class NativeHistoricTaskInstanceQueryImpl extends AbstractNativeQuery<NativeHistoricTaskInstanceQuery, HistoricTaskInstance> implements NativeHistoricTaskInstanceQuery {

  private static final long serialVersionUID = 1L;

  public NativeHistoricTaskInstanceQueryImpl() {
    super();
  }

  public NativeHistoricTaskInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeHistoricTaskInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }


 //results ////////////////////////////////////////////////////////////////
  
  public List<HistoricTaskInstance> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return commandContext
      .getHistoricTaskInstanceManager()
      .findHistoricTaskInstancesByNativeQuery(parameterMap, firstResult, maxResults);
  }
  
  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    return commandContext
      .getHistoricTaskInstanceManager()
      .findHistoricTaskInstanceCountByNativeQuery(parameterMap);
  }

}
