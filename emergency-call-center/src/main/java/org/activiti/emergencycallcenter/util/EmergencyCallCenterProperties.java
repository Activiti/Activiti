package org.activiti.emergencycallcenter.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class EmergencyCallCenterProperties extends Properties {

  private static final long serialVersionUID = 1L;

  private static final String FILE_NAME = "emergencyCallCenter.properties";

  public static final String CALL_CENTER_ACTIVITI_BASE_URL = "callCenter.activiti.baseurl";
  public static final String CALL_CENTER_ACTIVITI_TASK_QUERY_ENDPOINT = "callCenter.activiti.task.query.endpoint";
  public static final String CALL_CENTER_ACTIVITI_TASK_ACTION_ENDPOINT = "callCenter.activiti.task.action.endpoint";
  public static final String CALL_CENTER_ACTIVITI_USER = "callCenter.activiti.user";
  public static final String CALL_CENTER_ACTIVITI_PASSWORD = "callCenter.activiti.password";

  public static final String FIRE_DEPT_ACTIVITI_BASE_URL = "fireDept.activiti.baseurl";
  public static final String FIRE_DEPT_ACTIVITI_PROCESS_START_ENDPOINT = "fireDept.activiti.process.start.endpoint";
  public static final String FIRE_DEPT_ACTIVITI_USER = "fireDept.activiti.user";
  public static final String FIRE_DEPT_ACTIVITI_PASSWORD = "fireDept.activiti.password";
  public static final String FIRE_DEPT_ACTIVITI_DEFINITION_KEY = "fireDept.activiti.definition.key";

  public static final String POLICE_DEPT_ACTIVITI_BASE_URL = "policeDept.activiti.baseurl";
  public static final String POLICE_DEPT_ACTIVITI_PROCESS_START_ENDPOINT = "policeDept.activiti.process.start.endpoint";
  public static final String POLICE_DEPT_ACTIVITI_USER = "policeDept.activiti.user";
  public static final String POLICE_DEPT_ACTIVITI_PASSWORD = "policeDept.activiti.password";
  public static final String POLICE_DEPT_ACTIVITI_DEFINITION_KEY = "policeDept.activiti.definition.key";

  public static final String HOSPITAL_ACTIVITI_BASE_URL = "hospital.activiti.baseurl";
  public static final String HOSPITAL_ACTIVITI_PROCESS_START_ENDPOINT = "hospital.activiti.process.start.endpoint";
  public static final String HOSPITAL_ACTIVITI_USER = "hospital.activiti.user";
  public static final String HOSPITAL_ACTIVITI_PASSWORD = "hospital.activiti.password";
  public static final String HOSPITAL_ACTIVITI_DEFINITION_KEY = "hospital.activiti.definition.key";

  public final void load() {

    InputStream input = getClass().getClassLoader().getResourceAsStream(EmergencyCallCenterProperties.FILE_NAME);

    if (input != null) {

	  try {

        load(input);

	  } catch (IOException e) {
		e.printStackTrace();
      } finally {
        if (input != null) {
          try {
            input.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }    
  }
}
