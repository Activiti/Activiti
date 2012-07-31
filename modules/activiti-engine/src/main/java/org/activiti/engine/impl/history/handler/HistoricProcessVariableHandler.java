package org.activiti.engine.impl.history.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.HistoricProcessVariableEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricProcessVariableHandler implements ExecutionListener {

  public void notify(DelegateExecution execution) throws Exception {
    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    String processInstanceId = execution.getProcessInstanceId();
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_VARIABLE &&
        processInstanceId != null &&
        execution.getId().equals(processInstanceId)) {
      
      Map<String, VariableInstanceEntity> historicVariablesByName = new HashMap<String, VariableInstanceEntity>();
      
      // check runtime vars in db
      CommandContext commandContext = Context.getCommandContext();
      List<VariableInstanceEntity> variableInstances = commandContext
        .getVariableInstanceManager()
        .findVariableInstancesByExecutionId(processInstanceId);
      
      for (VariableInstanceEntity variableInstanceEntity : variableInstances) {
        historicVariablesByName.put(variableInstanceEntity.getName(), variableInstanceEntity);
      }
      
      // check transient vars in db session cache and overwrite old values in map
      List<VariableInstanceEntity> variableInstancesInCache = commandContext
        .getDbSqlSession()
        .findInCache(VariableInstanceEntity.class);

      for (VariableInstanceEntity variableInstanceEntity : variableInstancesInCache) {
        if (variableInstanceEntity.getProcessInstanceId().equals(processInstanceId)) {
          historicVariablesByName.put(variableInstanceEntity.getName(), variableInstanceEntity);
        }
      }
      
      for (VariableInstanceEntity variableInstanceEntity : historicVariablesByName.values()) {
        HistoricProcessVariableEntity historicProcessVariable = new HistoricProcessVariableEntity(variableInstanceEntity);
        Context.getCommandContext()
               .getDbSqlSession()
               .insert(historicProcessVariable);
      }
      
    }
  }

}
