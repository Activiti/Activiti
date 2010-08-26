package org.activiti.cycle.impl.connector.signavio.util;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class SignavioJsonHelper {

  public static String getValueIfExists(JSONObject json, String name) throws JSONException {
    if (json.has(name)) {
      return json.getString("name");
    } else {
      return null;
    }
  }

  public static Date getDateValueIfExists(JSONObject json, String name) throws JSONException {
    if (json.has(name)) {
      // TODO: IMplement Date conversion, Signavio has: 2010-06-21 17:36:23
      // +0200
      return null;
      // return json.getString("name");
    } else {
      return null;
    }
  }
}
