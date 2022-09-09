package org.activiti.engine.impl.cmd;


import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.dynamic.DynamicStateManager;
import org.activiti.engine.impl.dynamic.DefaultDynamicStateManager;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.runtime.ChangeActivityStateBuilderImpl;

/**
 * @author LoveMyOrange
 */
public class ChangeActivityStateCmd implements Command<Void> {

    protected ChangeActivityStateBuilderImpl changeActivityStateBuilder;

    public ChangeActivityStateCmd(ChangeActivityStateBuilderImpl changeActivityStateBuilder) {
        this.changeActivityStateBuilder = changeActivityStateBuilder;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        if (changeActivityStateBuilder.getMoveExecutionIdList().size() == 0 && changeActivityStateBuilder.getMoveActivityIdList().size() == 0) {
            throw new ActivitiIllegalArgumentException("No move execution or activity ids provided");

        } else if (changeActivityStateBuilder.getMoveActivityIdList().size() > 0 && changeActivityStateBuilder.getProcessInstanceId() == null) {
            throw new ActivitiIllegalArgumentException("Process instance id is required");
        }

        DynamicStateManager dynamicStateManager = new DefaultDynamicStateManager();
        dynamicStateManager.moveExecutionState(changeActivityStateBuilder, commandContext);

        return null;
    }
}
