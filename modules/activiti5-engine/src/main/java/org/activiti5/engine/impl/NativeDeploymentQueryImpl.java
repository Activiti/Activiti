package org.activiti5.engine.impl;

import java.util.List;
import java.util.Map;

import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.interceptor.CommandExecutor;
import org.activiti5.engine.repository.Deployment;
import org.activiti5.engine.repository.NativeDeploymentQuery;

public class NativeDeploymentQueryImpl extends AbstractNativeQuery<NativeDeploymentQuery, Deployment> implements NativeDeploymentQuery {

  private static final long serialVersionUID = 1L;
  
  public NativeDeploymentQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeDeploymentQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }


 //results ////////////////////////////////////////////////////////////////
  
  public List<Deployment> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return commandContext
      .getDeploymentEntityManager()
      .findDeploymentsByNativeQuery(parameterMap, firstResult, maxResults);
  }
  
  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    return commandContext
      .getDeploymentEntityManager()
      .findDeploymentCountByNativeQuery(parameterMap);
  }

}
