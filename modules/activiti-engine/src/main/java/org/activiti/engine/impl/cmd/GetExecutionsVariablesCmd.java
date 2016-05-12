package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 * @author Daisuke Yoshimoto
 */
public class GetExecutionsVariablesCmd implements Command<List<VariableInstance>>, Serializable{

  private static final long serialVersionUID = 7576838206239649561L;
  protected Set<String> executionIds;
  
  public GetExecutionsVariablesCmd(Set<String> executionIds) {
    this.executionIds = executionIds;
  }
  
  @Override
  public List<VariableInstance> execute(CommandContext commandContext) {
    // Verify existance of executions
    if(executionIds == null) {
      throw new ActivitiIllegalArgumentException("executionIds is null");
    }
    if(executionIds.isEmpty()){
        throw new ActivitiIllegalArgumentException("Set of executionIds is empty");
    }
    
    List<VariableInstance> instances = new ArrayList<VariableInstance>();
    List<VariableInstanceEntity> entities = commandContext.getVariableInstanceEntityManager().findVariableInstancesByExecutionIds(executionIds);
    for(VariableInstanceEntity entity : entities){
        instances.add(entity);
    }
    return instances;
  }

}
