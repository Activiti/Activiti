package org.activiti.engine.delegate;

import java.util.Date;


public interface DelegateTask {

  /** DB id of the task. */
  String getId();
  
  /** Name or title of the task. */
  String getName();

  /** Free text description of the task. */
  String getDescription();
  
  /** indication of how important/urgent this task is with a number between 
   * 0 and 100 where higher values mean a higher priority and lower values mean 
   * lower priority: [0..19] lowest, [20..39] low, [40..59] normal, [60..79] high 
   * [80..100] highest */
  int getPriority();
  
  /** Refers to a {@link User.getId() user} which is the owner or person responsible for completing this task. */
  String getAssignee();
  
  /** Reference to the process instance or null if it is not related to a process instance. */
  String getProcessInstanceId();
  
  /** Reference to the path of execution or null if it is not related to a process instance. */
  String getExecutionId();
  
  /** Reference to the process definition or null if it is not related to a process. */
  String getProcessDefinitionId();

  /** The date/time when this task was created */
  Date getCreateTime();
  
  /** The id of the activity in the process defining this task or null if this is not related to a process */
  String getTaskDefinitionKey();
  
  /** Adds the given user as a candidate user to this task. */
  void addCandidateUser(String userId);
  
  /** Adds the given group as candidate group to this task */
  void addCandidateGroup(String groupId);

  /** Sets the current assignee of this task to the given user */
  void setAssignee(String assignee);
  
}
