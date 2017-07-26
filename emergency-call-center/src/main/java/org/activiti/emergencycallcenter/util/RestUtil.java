package org.activiti.emergencycallcenter.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.activiti.engine.impl.util.json.JSONObject;

public final class RestUtil {

  public final static String PROCESS_DEFINITION_KEY = "processDefinitionKey";
  public final static String PROCESS_INSTANCE_ID = "processInstanceId";
  public final static String TASK_DEFINITION_KEY = "taskDefinitionKey";
  public final static String TASK_ID = "taskId";
  public final static String CONTACT_NAME = "contactName";
  public final static String CONTACT_DETAILS = "contactDetails";
  public final static String DESCRIPTION = "description";
  public final static String TYPE = "type";
  public final static String FIRE_DEPT_NOTE = "fireDeptNote";
  public final static String FIRE_DEPT_RESOURCES_CONFIRMED = "fireDeptResourceConfirmed";
  public final static String POLICE_DEPT_NOTE = "policeDeptNote";
  public final static String POLICE_DEPT_RESOURCES_CONFIRMED = "policeDeptResourceConfirmed";
  public final static String HOSPITAL_NOTE = "hospitalNote";
  public final static String HOSPITAL_RESOURCES_CONFIRMED = "hospitalResourceConfirmed";

  /**
   * 
   * Send the request of resources to an Organization, starting an Activiti process.
   * 
   * @param endpoint
   * @param user
   * @param password
   * @param parameters
   */
  public final static void sendResourceRequest (
    String endpoint,
    String user,
    String password,
    JSONObject parameters) {

    try {

      URL url = new URL(endpoint);
      String authentication = Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));

      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Authorization", "Basic " + authentication);
      conn.setRequestProperty("Content-Type", "application/json");

      String input = "{\"" + RestUtil.PROCESS_DEFINITION_KEY + "\":\"" + parameters.getString(RestUtil.PROCESS_DEFINITION_KEY) + "\","; 
      parameters.remove(RestUtil.PROCESS_DEFINITION_KEY);
      input += "\"variables\": [" + getVariables(parameters) + "]}";

      OutputStream os = conn.getOutputStream();
      os.write(input.getBytes());
      os.flush();

      if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
        throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
      }

      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      JSONObject result = new JSONObject(br.readLine());

      if (!result.has("id")) {
        throw new RuntimeException("Failed : Cannot generate the '" + parameters.getString("processDefinitionKey") + "' process.");
      }

      conn.disconnect();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 
   * Get the taskId of an existing process.
   * 
   * @param endpoint
   * @param user
   * @param password
   * @param parameters
   * @return
   */
  public final static String getTaskId (
    String endpoint,
    String user,
    String password,
    JSONObject parameters) {

  String taskId = "";

  try {

      String queryString = endpoint + "?" +
        RestUtil.TASK_DEFINITION_KEY + "=" + parameters.getString(RestUtil.TASK_DEFINITION_KEY) + "&" +
        RestUtil.PROCESS_INSTANCE_ID + "=" + parameters.getString(RestUtil.PROCESS_INSTANCE_ID);

      URL url = new URL(queryString);
      String authentication = Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));

      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Authorization", "Basic " + authentication);

      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
      }

      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      JSONObject result = new JSONObject(br.readLine());

      if (!result.has("data")) {
        throw new RuntimeException("Failed : Cannot retrieve the taskId for process instance '" + parameters.getString(RestUtil.PROCESS_INSTANCE_ID) + "' on the taskDefinition '" + parameters.getString(RestUtil.TASK_DEFINITION_KEY) + "'.");
      }
      if (result.getJSONArray("data").length() == 0) {
        throw new RuntimeException("Failed : No tasks are active for process instance '" + parameters.getString(RestUtil.PROCESS_INSTANCE_ID) + "' on the taskDefinition '" + parameters.getString(RestUtil.TASK_DEFINITION_KEY) + "'.");
      }
      if (result.getJSONArray("data").length() > 1) {
        throw new RuntimeException("Failed : Multiple tasks are active for process instance '" + parameters.getString(RestUtil.PROCESS_INSTANCE_ID) + "' on the taskDefinition '" + parameters.getString(RestUtil.TASK_DEFINITION_KEY) + "'.");
      }
      JSONObject data = result.getJSONArray("data").getJSONObject(0);
      if (!data.has("id")) {
        throw new RuntimeException("Failed : Cannot retrieve the taskId for process instance '" + parameters.getString(RestUtil.PROCESS_INSTANCE_ID) + "' on the taskDefinition '" + parameters.getString(RestUtil.TASK_DEFINITION_KEY) + "'.");
      }

      taskId = data.getString("id");

      conn.disconnect();

    } catch (Exception e) {
      e.printStackTrace();
    }

  return taskId;
  }

  /**
   * 
   * Send a feedback to the Call Center's process.
   * 
   * @param endpoint
   * @param user
   * @param password
   * @param parameters
   */
  public final static void sendResourceFeedback (
    String endpointPrefix,
    String user,
    String password,
    JSONObject parameters) {

    try {

      String enpdoint = endpointPrefix + parameters.getString(RestUtil.TASK_ID);

      URL url = new URL(enpdoint);
      String authentication = Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));

      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Authorization", "Basic " + authentication);
      conn.setRequestProperty("Content-Type", "application/json");

      parameters.remove(RestUtil.TASK_ID);
      String input = "{" + 
        "\"action\":\"complete\"," + 
        "\"variables\": [" + getVariables(parameters) + "]" + 
      "}";

      OutputStream os = conn.getOutputStream();
      os.write(input.getBytes());
      os.flush();

      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
      }

      conn.disconnect();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 
   * Compose a string with all the parameters for the POST calls.
   * 
   * @param parameters
   * @return
   */
  private final static String getVariables(JSONObject parameters) {

    String result ="";
    for (int i=0; i < parameters.length(); ++i) {
      result += "{\"name\":\"" + parameters.names().get(i) + "\",\"value\":\"" + parameters.getString((String) parameters.names().get(i)) + "\"}";
      if (i < (parameters.length() - 1)) {
        result += ",";
      }
    }

    return result;
  }
}
