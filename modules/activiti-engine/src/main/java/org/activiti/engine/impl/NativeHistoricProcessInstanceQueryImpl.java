package org.activiti.engine.impl;

import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.NativeHistoricProcessInstanceQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.NativeExecutionQuery;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;


public class NativeHistoricProcessInstanceQueryImpl extends AbstractNativeQuery<NativeHistoricProcessInstanceQuery, HistoricProcessInstance> implements NativeHistoricProcessInstanceQuery {

  private static final long serialVersionUID = 1L;

  public NativeHistoricProcessInstanceQueryImpl() {
    super();
  }

  public NativeHistoricProcessInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeHistoricProcessInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }


 //results ////////////////////////////////////////////////////////////////
  
  public List<HistoricProcessInstance> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return commandContext
      .getHistoricProcessInstanceManager()
      .findHistoricProcessInstancesByNativeQuery(parameterMap, firstResult, maxResults);
  }
  
  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    return commandContext
      .getHistoricProcessInstanceManager()
      .findHistoricProcessInstanceCountByNativeQuery(parameterMap);
  }

}
