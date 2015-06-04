package org.activiti5.engine.impl;

import java.util.List;
import java.util.Map;

import org.activiti5.engine.history.HistoricDetail;
import org.activiti5.engine.history.NativeHistoricDetailQuery;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.interceptor.CommandExecutor;

public class NativeHistoricDetailQueryImpl extends AbstractNativeQuery<NativeHistoricDetailQuery, HistoricDetail> implements NativeHistoricDetailQuery {

  private static final long serialVersionUID = 1L;
  
  public NativeHistoricDetailQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeHistoricDetailQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

 //results ////////////////////////////////////////////////////////////////
  
  public List<HistoricDetail> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return commandContext
      .getHistoricDetailEntityManager()
      .findHistoricDetailsByNativeQuery(parameterMap, firstResult, maxResults);
  }
  
  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    return commandContext
        .getHistoricDetailEntityManager()
      .findHistoricDetailCountByNativeQuery(parameterMap);
  }

}