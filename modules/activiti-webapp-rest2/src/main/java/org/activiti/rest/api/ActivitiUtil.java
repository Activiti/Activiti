package org.activiti.rest.api;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineInfo;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;

public class ActivitiUtil {
  
  private static final String DEFAULT_ENGINE = "default";
  
  /**
   * Returns the process engine info.
   *
   * @return The process engine info
   */
  public static ProcessEngineInfo getProcessEngineInfo() {
    return ProcessEngines.getProcessEngineInfo(DEFAULT_ENGINE);
  }

  /**
   * Returns the process engine.
   *
   * @return The process engine
   */
  public static ProcessEngine getProcessEngine() {
    return ProcessEngines.getDefaultProcessEngine();
  }

  /**
   * Returns the identity service.
   *
   * @return The identity service
   */
  public static IdentityService getIdentityService() {
    return getProcessEngine().getIdentityService();
  }

  /**
   * Returns the management service.
   *
   * @return The management service.
   */
  public static ManagementService getManagementService() {
    return getProcessEngine().getManagementService();
  }

  /**
   * Returns The process service.
   *
   * @return The process service
   */
  public static RuntimeService getRuntimeService() {
    return getProcessEngine().getRuntimeService();
  }

  /**
   * Returns The history service.
   *
   * @return The history service
   */
  public static HistoryService getHistoryService() {
    return getProcessEngine().getHistoryService();
  }

  /**
   * Returns The repository service.
   *
   * @return The repository service
   */
  public static RepositoryService getRepositoryService() {
    return getProcessEngine().getRepositoryService();
  }

  /**
   * Returns the task service.
   *
   * @return The task service
   */
  public static TaskService getTaskService() {
    return getProcessEngine().getTaskService();
  }
  
  /**
   * Returns the form service.
   *
   * @return The form service
   */
  public static FormService getFormService() {
    return getProcessEngine().getFormService();
  }
}
