package org.activiti.emergencycallcenter.util;

import org.activiti.engine.impl.util.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public final class RestUtil {

  public final static String RESOURCES_CONFIRMED = "resourceConfirmed";

  public final static Boolean sendResourceRequest (
    String endpoint,
    String processInstanceId) {

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(endpoint + processInstanceId, String.class);

    JSONObject result = new JSONObject(response.getBody());
    if (!result.has(RestUtil.RESOURCES_CONFIRMED)) {
      throw new RuntimeException("Failed : Cannot retrieve the '" + RestUtil.RESOURCES_CONFIRMED + "' for process instance '" + processInstanceId + "'.");
    }

    return result.getBoolean(RestUtil.RESOURCES_CONFIRMED);

  }

}
