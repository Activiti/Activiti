package org.activiti.engine.impl.jobexecutor;
/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.util.json.JSONException;
import org.activiti.engine.impl.util.json.JSONObject;

public class TimerEventHandler {

  public static final String PROPERTYNAME_TIMER_ACTIVITY_ID = "activityId";
  public static final String PROPERTYNAME_END_DATE_EXPRESSION = "timerEndDate";
  public static final String PROPERTYNAME_PROCESS_DEFINITION_KEY = "processDefinitionKey";
  public static final String PROPERTYNAME_CALENDAR_NAME_EXPRESSION = "calendarName";

  public static String createConfiguration(String id, Expression endDate, Expression calendarName) {
    JSONObject cfgJson = new JSONObject();
    cfgJson.put(PROPERTYNAME_TIMER_ACTIVITY_ID, id);
    if (endDate!=null) {
      cfgJson.put(PROPERTYNAME_END_DATE_EXPRESSION, endDate.getExpressionText());
    }
    if (calendarName != null) {
      cfgJson.put(PROPERTYNAME_CALENDAR_NAME_EXPRESSION, calendarName.getExpressionText());
    }
    return cfgJson.toString();
  }

  public String setActivityIdToConfiguration(String jobHandlerConfiguration, String activityId) {
    try {
      JSONObject cfgJson = new JSONObject(jobHandlerConfiguration);
      cfgJson.put(PROPERTYNAME_TIMER_ACTIVITY_ID, activityId);
      return cfgJson.toString();
    } catch (JSONException ex){
      return jobHandlerConfiguration;
    }
  }

  public static String getActivityIdFromConfiguration(String jobHandlerConfiguration) {
   try {
     JSONObject cfgJson = new JSONObject(jobHandlerConfiguration);
     return  cfgJson.get(PROPERTYNAME_TIMER_ACTIVITY_ID).toString();
   } catch (JSONException ex){
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

  public String setEndDateToConfiguration(String jobHandlerConfiguration, String endDate) {
    JSONObject cfgJson =null;
    try {
      cfgJson = new JSONObject(jobHandlerConfiguration);
    } catch (JSONException ex){
      //create the json config
      cfgJson = new JSONObject();
      cfgJson.put(PROPERTYNAME_TIMER_ACTIVITY_ID, jobHandlerConfiguration);
    }
    if (endDate!=null) {
      cfgJson.put(PROPERTYNAME_END_DATE_EXPRESSION, endDate);
    }

    return cfgJson.toString();
  }

  public static String getEndDateFromConfiguration(String jobHandlerConfiguration) {
    try{
      JSONObject cfgJson = new JSONObject(jobHandlerConfiguration);
      return  cfgJson.get(PROPERTYNAME_END_DATE_EXPRESSION).toString();
    } catch (JSONException ex){
      return null;
    }
  }
  
  public String setProcessDefinitionKeyToConfiguration(String jobHandlerConfiguration, String activityId) {
    try{
      JSONObject cfgJson = new JSONObject(jobHandlerConfiguration);
      cfgJson.put(PROPERTYNAME_PROCESS_DEFINITION_KEY, activityId);
      return cfgJson.toString();
    } catch (JSONException ex){
      return jobHandlerConfiguration;
    }
  }
  
  public String getProcessDefinitionKeyFromConfiguration(String jobHandlerConfiguration) {
    try{
      JSONObject cfgJson = new JSONObject(jobHandlerConfiguration);
      return  cfgJson.get(PROPERTYNAME_PROCESS_DEFINITION_KEY).toString();
    } catch (JSONException ex){
      return null;
    }
  }

  /**
   * Before Activiti 5.21, the jobHandlerConfiguration would have as activityId the process definition key
   * (as only one timer start event was supported). In >= 5.21, this changed and in >= 5.21 the activityId 
   * is the REAL activity id. It can be recognized by having the 'processDefinitionKey' in the configuration.
   * A < 5.21 job would not have that.
   */
  public static boolean hasRealActivityId(String jobHandlerConfiguration) {
    try{
      JSONObject cfgJson = new JSONObject(jobHandlerConfiguration);
      Object processDefinitionKey = cfgJson.get(PROPERTYNAME_PROCESS_DEFINITION_KEY);
      if (processDefinitionKey != null) {
        return processDefinitionKey.toString().length() > 0;
      }
    }catch (JSONException ex){
      return false;
    }
    return false;
  }

}
