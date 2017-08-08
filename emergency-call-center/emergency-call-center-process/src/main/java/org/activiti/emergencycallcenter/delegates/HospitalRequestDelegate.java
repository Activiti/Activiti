package org.activiti.emergencycallcenter.delegates;

import org.activiti.emergencycallcenter.util.EmergencyCallCenterProperties;
import org.activiti.emergencycallcenter.util.RestUtil;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class HospitalRequestDelegate implements JavaDelegate {

  private final static String RESOURCES_CONFIRMED = "hospitalResourceConfirmed";

  public void execute(DelegateExecution execution) {

    EmergencyCallCenterProperties properties = new EmergencyCallCenterProperties();
    properties.load();

	Boolean resourceConfirmed = RestUtil.sendResourceRequest(
      properties.getProperty(EmergencyCallCenterProperties.FIRE_DEPT_BASE_URL) + properties.getProperty(EmergencyCallCenterProperties.FIRE_DEPT_REQUEST_RESOURCES_ENDPOINT_PREFIX), 
      execution.getProcessInstanceId());

	execution.setVariable(RESOURCES_CONFIRMED, (resourceConfirmed ? "Yes" : "No"));

  }

}