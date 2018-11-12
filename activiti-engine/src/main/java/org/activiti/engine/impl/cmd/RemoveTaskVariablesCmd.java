package org.activiti.engine.impl.cmd;

import java.util.Collection;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

/**


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
