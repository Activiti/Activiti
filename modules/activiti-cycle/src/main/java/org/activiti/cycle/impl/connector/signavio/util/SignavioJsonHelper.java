package org.activiti.cycle.impl.connector.signavio.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Date;

import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.json.JSONException;
import org.json.JSONObject;

public class SignavioJsonHelper {

  /**
   * @deprecated use JSONObject.optString(String key) instead
   */
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

  public static String getEmptypModelTemplate() {
    BufferedReader reader = null;
    try {
      InputStream is = SignavioConnector.class.getResourceAsStream("emptyProcessModelTemplate.json");
      reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      StringWriter resultWriter = new StringWriter();
      String line;
      while ((line = reader.readLine()) != null) {
        resultWriter.append(line);
      }
      reader.close();
      return resultWriter.toString();
    } catch (IOException e) {
      if (reader == null)
        return null;
      try {
        reader.close();
      } catch (IOException ex) {

      }
      return null;
    }
  }
}
