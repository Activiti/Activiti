package org.activiti.engine.history;

import java.util.Date;
import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * A trail of data for a given process instance.
 * 
 * @author Joram Barrez
 */
public interface ProcessInstanceHistoryLog {
	
	 /** The process instance id (== as the id for the runtime {@link ProcessInstance process instance}). */
  String getId();
  
  /** The user provided unique reference to this process instance. */
  String getBusinessKey();

  /** The process definition reference. */
  String getProcessDefinitionId();

  /** The time the process was started. */
  Date getStartTime();

  /** The time the process was ended. */
  Date getEndTime();

  /** The difference between {@link #getEndTime()} and {@link #getStartTime()} . */
  Long getDurationInMillis();

  /** The authenticated user that started this process instance. 
   * @see IdentityService#setAuthenticatedUserId(String) */
  String getStartUserId();
  
  /** The start activity. */
  String getStartActivityId();

  /** Obtains the reason for the process instance's deletion. */
  String getDeleteReason();
  
  /**
   * The process instance id of a potential super process instance or null if no super process instance exists
   */
  String getSuperProcessInstanceId();
  
  /**
   * The tenant identifier for the process instance.
   */
  String getTenantId();
  
  /**
   * The trail of data, ordered by date (ascending).
   * Gives a replay of the process instance.
   */
  List<HistoricData> getHistoricData();

}
