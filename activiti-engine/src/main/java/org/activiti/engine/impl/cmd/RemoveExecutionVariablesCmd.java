package org.activiti.engine.impl.cmd;

import java.util.Collection;

import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.Activiti5Util;

/**
 * @author roman.smirnov
 * @author Joram Barrez
 */
public class RemoveExecutionVariablesCmd extends NeedsActiveExecutionCmd<Void> {

  private static final long serialVersionUID = 1L;

  private Collection<String> variableNames;
  private boolean isLocal;

  public RemoveExecutionVariablesCmd(String executionId, Collection<String> variableNames, boolean isLocal) {
    super(executionId);
    this.variableNames = variableNames;
    this.isLocal = isLocal;
  }

  protected Void execute(CommandContext commandContext, ExecutionEntity execution) {
    if (Activiti5Util.isActiviti5ProcessDefinitionId(commandContext, execution.getProcessDefinitionId())) {
      Activiti5CompatibilityHandler activiti5CompatibilityHandler = Activiti5Util.getActiviti5CompatibilityHandler(); 
      activiti5CompatibilityHandler.removeExecutionVariables(executionId, variableNames, isLocal);
      return null;
    }
    
    if (isLocal) {
      execution.removeVariablesLocal(variableNames);
    } else {
      execution.removeVariables(variableNames);
    }

    return null;
  }

  @Override
  protected String getSuspendedExceptionMessage() {
    return "Cannot remove variables because execution '" + executionId + "' is suspended";
  }

}
