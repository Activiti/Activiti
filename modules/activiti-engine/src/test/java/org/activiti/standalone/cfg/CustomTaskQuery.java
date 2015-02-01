/**
 * 
 */
package org.activiti.standalone.cfg;

import java.util.List;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.task.Task;

/**
 * @author Bassam Al-Sarori
 *
 */
public class CustomTaskQuery extends TaskQueryImpl {

  private static final long serialVersionUID = 1L;
  
  protected boolean unOwned;
  
  public CustomTaskQuery() {
  }
  
  public CustomTaskQuery(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public CustomTaskQuery unOwned(){
    unOwned = true;
    return this;
  }

  public boolean getUnOwned(){
    return unOwned;
  }
  
  @SuppressWarnings("unchecked")
  public List<Task> executeList(CommandContext commandContext, Page page) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext.getDbSqlSession().selectList("selectCustomTaskByQueryCriteria", this);
  }
  
  public long executeCount(CommandContext commandContext) {
    ensureVariablesInitialized();
    checkQueryOk();
    return (Long) commandContext.getDbSqlSession().selectOne("selectCustomTaskCountByQueryCriteria", this);
  }
}
