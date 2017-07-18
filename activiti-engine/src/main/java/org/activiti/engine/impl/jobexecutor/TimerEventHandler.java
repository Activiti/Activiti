package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.impl.util.json.JSONException;
import org.activiti.engine.impl.util.json.JSONObject;

public class TimerEventHandler {

  public static final String PROPERTYNAME_TIMER_ACTIVITY_ID = "activityId";
  public static final String PROPERTYNAME_END_DATE_EXPRESSION = "timerEndDate";
  public static final String PROPERTYNAME_CALENDAR_NAME_EXPRESSION = "calendarName";
  
  public static String createConfiguration(String id, String endDate, String calendarName) {
    JSONObject cfgJson = new JSONObject();
    cfgJson.put(PROPERTYNAME_TIMER_ACTIVITY_ID, id);
    if (endDate != null) {
      cfgJson.put(PROPERTYNAME_END_DATE_EXPRESSION, endDate);
    }
    if (calendarName != null) {
      cfgJson.put(PROPERTYNAME_CALENDAR_NAME_EXPRESSION, calendarName);
    }
    return cfgJson.toString();
  }

  public static String setActivityIdToConfiguration(String jobHandlerConfiguration, String activityId) {
    try {
      JSONObject cfgJson = new JSONObject(jobHandlerConfiguration);
      cfgJson.put(PROPERTYNAME_TIMER_ACTIVITY_ID, activityId);
      return cfgJson.toString();
    } catch (JSONException ex) {
      return jobHandlerConfiguration;
    }
  }

  public static String getActivityIdFromConfiguration(String jobHandlerConfiguration) {
    try {
      JSONObject cfgJson = new JSONObject(jobHandlerConfiguration);
      return cfgJson.get(PROPERTYNAME_TIMER_ACTIVITY_ID).toString();
    } catch (JSONException ex) {
      return jobHandlerConfiguration;
    }
  }
  
  public static String geCalendarNameFromConfiguration(String jobHandlerConfiguration) {
    try {
      JSONObject cfgJson = new JSONObject(jobHandlerConfiguration);
      return cfgJson.get(PROPERTYNAME_CALENDAR_NAME_EXPRESSION).toString();
    } catch (JSONException ex) {
      // calendar name is not specified
      return "";
    }
  }

  public static String setEndDateToConfiguration(String jobHandlerConfiguration, String endDate) {
    JSONObject cfgJson = null;
    try {
      cfgJson = new JSONObject(jobHandlerConfiguration);
    } catch (JSONException ex) {
      // create the json config
      cfgJson = new JSONObject();
      cfgJson.put(PROPERTYNAME_TIMER_ACTIVITY_ID, jobHandlerConfiguration);
    }
    if (endDate != null) {
      cfgJson.put(PROPERTYNAME_END_DATE_EXPRESSION, endDate);
    }

    return cfgJson.toString();
  }

  public static String getEndDateFromConfiguration(String jobHandlerConfiguration) {
    try {
      JSONObject cfgJson = new JSONObject(jobHandlerConfiguration);
      return cfgJson.get(PROPERTYNAME_END_DATE_EXPRESSION).toString();
    } catch (JSONException ex) {
      return null;
    }
  }

}
