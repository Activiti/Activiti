package org.activiti.emergencycallcenter.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class EmergencyCallCenterProperties extends Properties {

  private static final long serialVersionUID = 1L;

  private static final String FILE_NAME = "emergencyCallCenter.properties";

  public static final String FIRE_DEPT_BASE_URL = "fireDept.baseurl";
  public static final String FIRE_DEPT_REQUEST_RESOURCES_ENDPOINT_PREFIX = "fireDept.requestResources.endpoint.prefix";

  public static final String POLICE_DEPT_BASE_URL = "policeDept.baseurl";
  public static final String POLICE = "policeDept.requestResources.endpoint.prefix";

  public static final String HOSPITAL_BASE_URL = "hospital.baseurl";
  public static final String HOSPITAL = "hospital.requestResources.endpoint.prefix";

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
