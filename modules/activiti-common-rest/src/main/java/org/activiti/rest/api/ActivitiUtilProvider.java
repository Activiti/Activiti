package org.activiti.rest.api;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineInfo;

/**
 *
 * @author fers
 */
public interface ActivitiUtilProvider {
    public ProcessEngine getProcessEngine();
    public ProcessEngineInfo getProcessEngineInfo();
}