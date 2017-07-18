package org.activiti.engine.impl;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.NativeProcessDefinitionQuery;
import org.activiti.engine.repository.ProcessDefinition;

import java.util.List;
import java.util.Map;

public class NativeProcessDefinitionQueryImpl extends AbstractNativeQuery<NativeProcessDefinitionQuery, ProcessDefinition> implements NativeProcessDefinitionQuery {

  private static final long serialVersionUID = 1L;

  public NativeProcessDefinitionQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeProcessDefinitionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  // results ////////////////////////////////////////////////////////////////

  public List<ProcessDefinition> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return commandContext.getProcessDefinitionEntityManager().findProcessDefinitionsByNativeQuery(parameterMap, firstResult, maxResults);
  }

  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    return commandContext.getProcessDefinitionEntityManager().findProcessDefinitionCountByNativeQuery(parameterMap);
  }

}
