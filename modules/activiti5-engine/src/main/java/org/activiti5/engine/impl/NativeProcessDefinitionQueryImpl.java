package org.activiti5.engine.impl;

import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.interceptor.CommandExecutor;
import org.activiti5.engine.repository.NativeProcessDefinitionQuery;

public class NativeProcessDefinitionQueryImpl extends AbstractNativeQuery<NativeProcessDefinitionQuery, ProcessDefinition> implements NativeProcessDefinitionQuery {

  private static final long serialVersionUID = 1L;
  
  public NativeProcessDefinitionQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeProcessDefinitionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }


 //results ////////////////////////////////////////////////////////////////
  
  public List<ProcessDefinition> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return commandContext
      .getProcessDefinitionEntityManager()
      .findProcessDefinitionsByNativeQuery(parameterMap, firstResult, maxResults);
  }
  
  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    return commandContext
      .getProcessDefinitionEntityManager()
      .findProcessDefinitionCountByNativeQuery(parameterMap);
  }

}
