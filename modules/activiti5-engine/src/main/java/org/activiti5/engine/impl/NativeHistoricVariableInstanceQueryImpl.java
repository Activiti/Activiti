package org.activiti5.engine.impl;

import java.util.List;
import java.util.Map;

import org.activiti5.engine.history.HistoricVariableInstance;
import org.activiti5.engine.history.NativeHistoricVariableInstanceQuery;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.interceptor.CommandExecutor;

public class NativeHistoricVariableInstanceQueryImpl extends AbstractNativeQuery<NativeHistoricVariableInstanceQuery, HistoricVariableInstance> implements NativeHistoricVariableInstanceQuery {

  private static final long serialVersionUID = 1L;
  
  public NativeHistoricVariableInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeHistoricVariableInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

 //results ////////////////////////////////////////////////////////////////
  
  public List<HistoricVariableInstance> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return commandContext
      .getHistoricVariableInstanceEntityManager()
      .findHistoricVariableInstancesByNativeQuery(parameterMap, firstResult, maxResults);
  }
  
  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    return commandContext
        .getHistoricVariableInstanceEntityManager()
      .findHistoricVariableInstanceCountByNativeQuery(parameterMap);
  }

}