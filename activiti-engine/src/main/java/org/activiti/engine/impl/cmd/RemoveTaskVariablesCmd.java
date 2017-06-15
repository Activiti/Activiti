package org.activiti.engine.impl.cmd;

import java.util.Collection;

import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.util.Activiti5Util;

/**
 * @author roman.smirnov
 * @author Joram Barrez
 */
public class RemoveTaskVariablesCmd extends NeedsActiveTaskCmd<Void> {

  private static final long serialVersionUID = 1L;

  private final Collection<String> variableNames;
  private final boolean isLocal;

  public RemoveTaskVariablesCmd(String taskId, Collection<String> variableNames, boolean isLocal) {
    super(taskId);
    this.variableNames = variableNames;
    this.isLocal = isLocal;
  }

  protected Void execute(CommandContext commandContext, TaskEntity task) {

    if (task.getProcessDefinitionId() != null && Activiti5Util.isActiviti5ProcessDefinitionId(commandContext, task.getProcessDefinitionId())) {
      Activiti5CompatibilityHandler activiti5CompatibilityHandler = Activiti5Util.getActiviti5CompatibilityHandler(); 
      activiti5CompatibilityHandler.removeTaskVariables(taskId, variableNames, isLocal);
      return null;
    }
    
    if (isLocal) {
      task.removeVariablesLocal(variableNames);
    } else {
      task.removeVariables(variableNames);
    }

    return null;
  }

  @Override
  protected String getSuspendedTaskException() {
    return "Cannot remove variables from a suspended task.";
  }

}
