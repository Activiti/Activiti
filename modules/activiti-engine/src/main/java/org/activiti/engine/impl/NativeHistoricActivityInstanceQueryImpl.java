package org.activiti.engine.impl;

import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.NativeHistoricActivityInstanceQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.NativeExecutionQuery;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;


public class NativeHistoricActivityInstanceQueryImpl extends AbstractNativeQuery<NativeHistoricActivityInstanceQuery, HistoricActivityInstance> implements NativeHistoricActivityInstanceQuery {

  private static final long serialVersionUID = 1L;

  public NativeHistoricActivityInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeHistoricActivityInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }


 //results ////////////////////////////////////////////////////////////////
  
  public List<HistoricActivityInstance> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return commandContext
      .getHistoricActivityInstanceManager()
      .findHistoricActivityInstancesByNativeQuery(parameterMap, firstResult, maxResults);
  }
  
  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    return commandContext
      .getHistoricActivityInstanceManager()
      .findHistoricActivityInstanceCountByNativeQuery(parameterMap);
  }

}
