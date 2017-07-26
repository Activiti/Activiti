package org.activiti.emergencycallcenter.delegates;

import org.activiti.emergencycallcenter.util.EmergencyCallCenterProperties;
import org.activiti.emergencycallcenter.util.RestUtil;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.util.json.JSONObject;

public class FireDeptFeedbackDelegate implements JavaDelegate {

  public final static String FIRE_DEPT_TASK_DEFINITION_KEY = "fireDeptReceivingDetailsTask";

  public void execute(DelegateExecution execution) {

    EmergencyCallCenterProperties properties = new EmergencyCallCenterProperties();
    properties.load();

	JSONObject parameters = new JSONObject();
	parameters.put(RestUtil.TASK_DEFINITION_KEY, FireDeptFeedbackDelegate.FIRE_DEPT_TASK_DEFINITION_KEY);
	parameters.put(RestUtil.PROCESS_INSTANCE_ID, execution.getVariable(RestUtil.PROCESS_INSTANCE_ID));

    String taskId = RestUtil.getTaskId (
      properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_BASE_URL) + properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_TASK_QUERY_ENDPOINT), 
      properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_USER),
      properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_PASSWORD),
      parameters);

    parameters = new JSONObject();
	parameters.put(RestUtil.TASK_ID, taskId);
	parameters.put(RestUtil.FIRE_DEPT_NOTE, execution.getVariable(RestUtil.FIRE_DEPT_NOTE));
	parameters.put(RestUtil.FIRE_DEPT_RESOURCES_CONFIRMED, execution.getVariable(RestUtil.FIRE_DEPT_RESOURCES_CONFIRMED));

    RestUtil.sendResourceFeedback (
      properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_BASE_URL) + properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_TASK_ACTION_ENDPOINT), 
      properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_USER),
      properties.getProperty(EmergencyCallCenterProperties.CALL_CENTER_ACTIVITI_PASSWORD),
      parameters);
  }

}