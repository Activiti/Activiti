package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.InstanceLocks;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

/**
 * @author Saeid Mirzaei
 */

public class WaitforFinishCmd implements Command<Object>, Serializable {
  private static final long serialVersionUID = 1L;
  String processInstanceId = null;
  
  

  public WaitforFinishCmd(String processInstanceId) {
    this.processInstanceId = processInstanceId; 
  }
  
  public Object execute(CommandContext commandContext) {
    
    ConcurrentHashMap<String, String> instanceLocks = InstanceLocks.getLocks();
    try {

      synchronized (instanceLocks) {

        while (instanceLocks.get(processInstanceId) == null) {
          instanceLocks.wait();
        }
        instanceLocks.remove(processInstanceId);
       
      }
     
    } catch (InterruptedException e) {      
      throw new ActivitiException("error for process instance to end. ProcessInstanceId=" + processInstanceId);
    }
    return null;
   
  }
}
