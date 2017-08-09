package org.activiti.emergencycallcenter.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.activiti.engine.impl.util.json.JSONObject;

public final class RestUtil {

  public final static String RESOURCES_CONFIRMED = "resourceConfirmed";

  /**
   * 
   * Send the request of resources to an Department and elaborate the answer.
   * 
   * @param endpoint
   * @param user
   * @param password
   * @param parameters
   */
  public final static Boolean sendResourceRequest (
    String endpoint,
    String processInstanceId) {

    Boolean resourceConfirmed = false;

    try {

      String queryString = endpoint + processInstanceId;

      URL url = new URL(queryString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("GET");

      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
      }

      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      JSONObject result = new JSONObject(br.readLine());

      if (!result.has(RestUtil.RESOURCES_CONFIRMED)) {
        throw new RuntimeException("Failed : Cannot retrieve the '" + RestUtil.RESOURCES_CONFIRMED + "' for process instance '" + processInstanceId + "'.");
      }

      resourceConfirmed = result.getBoolean(RestUtil.RESOURCES_CONFIRMED);

      conn.disconnect();

    } catch (Exception e) {
      e.printStackTrace();
    }

    return resourceConfirmed;
  }

}
