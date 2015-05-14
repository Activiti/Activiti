/**
 * 
 */
package org.activiti.standalone.cfg;

import java.util.List;

import org.activiti.engine.ManagementService;
import org.activiti.engine.impl.AbstractQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.TaskQueryProperty;
import org.activiti.engine.impl.interceptor.CommandContext;

/**
 * @author Bassam Al-Sarori
 *
 */
public class CustomTaskQuery extends AbstractQuery<CustomTaskQuery, CustomTask> {

  private static final long serialVersionUID = 1L;
  
  protected boolean unOwned;
  protected String taskId;
  protected String owner;
  
  public CustomTaskQuery(ManagementService managementService) {
    super(managementService);
  }
  
  public CustomTaskQuery taskId(String taskId){
    this.taskId = taskId;
    return this;
  }
  
  public CustomTaskQuery taskOwner(String owner){
    this.owner = owner;
    return this;
  }
  
  public CustomTaskQuery orderByTaskPriority(){
    return orderBy(TaskQueryProperty.PRIORITY);
  }
  
  public CustomTaskQuery unOwned(){
    unOwned = true;
    return this;
  }

  public boolean getUnOwned(){
    return unOwned;
  }
  
  @SuppressWarnings("unchecked")
  public List<CustomTask> executeList(CommandContext commandContext, Page page) {
    return commandContext.getDbSqlSession().selectList("selectCustomTaskByQueryCriteria", this);
  }
  
  public long executeCount(CommandContext commandContext) {
    return (Long) commandContext.getDbSqlSession().selectOne("selectCustomTaskCountByQueryCriteria", this);
  }
}
