package org.activiti.engine.impl.bpmn.helper;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Elias Ricken de Medeiros
 */
public class SubProcessVariableSnapshotter {

    public void setVariablesSnapshots(ExecutionEntity sourceExecution, ExecutionEntity snapshotHolder) {
        snapshotHolder.setVariablesLocal(sourceExecution.getVariablesLocal());

        ExecutionEntity parentExecution = sourceExecution.getParent();
        if (parentExecution != null && parentExecution.isMultiInstanceRoot()) {
            snapshotHolder.setVariablesLocal(parentExecution.getVariablesLocal());
        }

    }

}
