package org.activiti.emergencycallcenter.delegates;

import org.activiti.emergencycallcenter.util.EmergencyCallCenterProperties;
import org.activiti.emergencycallcenter.util.RestUtil;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.util.json.JSONObject;

public class HospitalFeedbackDelegate implements JavaDelegate {

  public final static String HOSPITAL_TASK_DEFINITION_KEY = "hospitalReceivingDetailsTask";

  public void execute(DelegateExecution execution) {

    EmergencyCallCenterProperties properties = new EmergencyCallCenterProperties();
    properties.load();

	JSONObject parameters = new JSONObject();
	parameters.put(RestUtil.TASK_DEFINITION_KEY, HospitalFeedbackDelegate.HOSPITAL_TASK_DEFINITION_KEY);
	parameters.put(RestUtil.PROCESS_INSTANCE_ID, execution.getVariable(RestUtil.PROCESS_INSTANCE_ID));

    String taskId = RestUtil.getTaskId (
      properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_BASE_URL) + properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_TASK_QUERY_ENDPOINT), 
      properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_USER),
      properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_PASSWORD),
      parameters);

    parameters = new JSONObject();
	parameters.put(RestUtil.TASK_ID, taskId);
	parameters.put(RestUtil.HOSPITAL_NOTE, execution.getVariable(RestUtil.HOSPITAL_NOTE));
	parameters.put(RestUtil.HOSPITAL_RESOURCES_CONFIRMED, execution.getVariable(RestUtil.HOSPITAL_RESOURCES_CONFIRMED));

    RestUtil.sendResourceFeedback (
      properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_BASE_URL) + properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_TASK_ACTION_ENDPOINT), 
      properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_USER),
      properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_PASSWORD),
      parameters);
  }

}