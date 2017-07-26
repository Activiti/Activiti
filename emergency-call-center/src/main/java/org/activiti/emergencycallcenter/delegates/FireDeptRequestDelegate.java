package org.activiti.emergencycallcenter.delegates;

import org.activiti.emergencycallcenter.util.EmergencyCallCenterProperties;
import org.activiti.emergencycallcenter.util.RestUtil;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.util.json.JSONObject;

public class FireDeptRequestDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) {

    EmergencyCallCenterProperties properties = new EmergencyCallCenterProperties();
    properties.load();

    JSONObject parameters = new JSONObject();
    parameters.put(RestUtil.PROCESS_DEFINITION_KEY, properties.getProperty(EmergencyCallCenterProperties.FIRE_DEPT_ACTIVITI_DEFINITION_KEY));
    parameters.put(RestUtil.PROCESS_INSTANCE_ID, execution.getProcessInstanceId());
    parameters.put(RestUtil.CONTACT_NAME, execution.getVariable(RestUtil.CONTACT_NAME));
    parameters.put(RestUtil.CONTACT_DETAILS, execution.getVariable(RestUtil.CONTACT_DETAILS));
    parameters.put(RestUtil.DESCRIPTION, execution.getVariable(RestUtil.DESCRIPTION));
    parameters.put(RestUtil.TYPE, execution.getVariable(RestUtil.TYPE));

	RestUtil.sendResourceRequest(
      properties.getProperty(EmergencyCallCenterProperties.FIRE_DEPT_ACTIVITI_BASE_URL) + properties.getProperty(EmergencyCallCenterProperties.FIRE_DEPT_ACTIVITI_PROCESS_START_ENDPOINT), 
      properties.getProperty(EmergencyCallCenterProperties.FIRE_DEPT_ACTIVITI_USER), 
      properties.getProperty(EmergencyCallCenterProperties.FIRE_DEPT_ACTIVITI_PASSWORD), 
      parameters);

  }

}