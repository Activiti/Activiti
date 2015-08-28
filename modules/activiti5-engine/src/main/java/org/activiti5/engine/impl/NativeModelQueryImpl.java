package org.activiti5.engine.impl;

import java.util.List;
import java.util.Map;

import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.interceptor.CommandExecutor;
import org.activiti5.engine.repository.Model;
import org.activiti5.engine.repository.NativeModelQuery;

public class NativeModelQueryImpl extends AbstractNativeQuery<NativeModelQuery, Model> implements NativeModelQuery {

  private static final long serialVersionUID = 1L;
  
  public NativeModelQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeModelQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

 //results ////////////////////////////////////////////////////////////////
  
  public List<Model> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return commandContext
      .getModelEntityManager()
      .findModelsByNativeQuery(parameterMap, firstResult, maxResults);
  }
  
  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    return commandContext
      .getModelEntityManager()
      .findModelCountByNativeQuery(parameterMap);
  }

}
