package org.activiti.engine.logging;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.MDC;

/**
 * Constants and functions for MDC (Mapped Diagnostic Context) logging
 * 
 * @author Saeid Mirzaei
 */

public class LogMDC {

  public static final String LOG_MDC_PROCESSDEFINITION_ID = "mdcProcessDefinitionID";
  public static final String LOG_MDC_EXECUTION_ID = "mdcExecutionId";
  public static final String LOG_MDC_PROCESSINSTANCE_ID = "mdcProcessInstanceID";
  public static final String LOG_MDC_BUSINESS_KEY = "mdcBusinessKey";
  public static final String LOG_MDC_TASK_ID = "mdcTaskId";

  static boolean enabled = false;

  public static boolean isMDCEnabled() {
    return enabled;
  }

  public static void setMDCEnabled(boolean b) {
    enabled = b;
  }

  public static void putMDCExecution(ActivityExecution e) {
    if (e.getId() != null)
      MDC.put(LOG_MDC_EXECUTION_ID, e.getId());
    if (e.getProcessDefinitionId() != null)
      MDC.put(LOG_MDC_PROCESSDEFINITION_ID, e.getProcessDefinitionId());
    if (e.getProcessInstanceId() != null)
      MDC.put(LOG_MDC_PROCESSINSTANCE_ID, e.getProcessInstanceId());
    if (e.getProcessBusinessKey() != null)
      MDC.put(LOG_MDC_BUSINESS_KEY, e.getProcessBusinessKey());
    

  }

  public static void clear() {
    MDC.clear();
  }
}
