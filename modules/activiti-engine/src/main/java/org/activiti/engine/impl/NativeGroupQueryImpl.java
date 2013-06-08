package org.activiti.engine.impl;

import org.activiti.engine.identity.NativeGroupQuery;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;

import java.util.List;
import java.util.Map;

public class NativeGroupQueryImpl extends AbstractNativeQuery<NativeGroupQuery, Group> implements NativeGroupQuery {

  private static final long serialVersionUID = 1L;
  
  public NativeGroupQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public NativeGroupQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }


 //results ////////////////////////////////////////////////////////////////
  
  public List<Group> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return commandContext
      .getGroupIdentityManager()
      .findGroupsByNativeQuery(parameterMap, firstResult, maxResults);
  }
  
  public long executeCount(CommandContext commandContext, Map<String, Object> parameterMap) {
    return commandContext
      .getGroupIdentityManager()
      .findGroupCountByNativeQuery(parameterMap);
  }

}