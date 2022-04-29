/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.logging;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.MDC;

/**
 * Constants and functions for MDC (Mapped Diagnostic Context) logging
 *

 */

public class LogMDC {

  public static final String LOG_MDC_PROCESSDEFINITION_ID = "mdcProcessDefinitionID";
  public static final String LOG_MDC_EXECUTION_ID = "mdcExecutionId";
  public static final String LOG_MDC_PROCESSINSTANCE_ID = "mdcProcessInstanceID";
  public static final String LOG_MDC_BUSINESS_KEY = "mdcBusinessKey";
  public static final String LOG_MDC_TASK_ID = "mdcTaskId";

  static boolean enabled;

  public static boolean isMDCEnabled() {
    return enabled;
  }

  public static void setMDCEnabled(boolean b) {
    enabled = b;
  }

  public static void putMDCExecution(ExecutionEntity e) {
    if (e.getId() != null)
      MDC.put(LOG_MDC_EXECUTION_ID, e.getId());
    if (e.getProcessDefinitionId() != null)
      MDC.put(LOG_MDC_PROCESSDEFINITION_ID, e.getProcessDefinitionId());
    if (e.getProcessInstanceId() != null)
      MDC.put(LOG_MDC_PROCESSINSTANCE_ID, e.getProcessInstanceId());
    if (e.getProcessInstanceBusinessKey() != null)
      MDC.put(LOG_MDC_BUSINESS_KEY, e.getProcessInstanceBusinessKey());

  }

  public static void clear() {
    MDC.clear();
  }
}
