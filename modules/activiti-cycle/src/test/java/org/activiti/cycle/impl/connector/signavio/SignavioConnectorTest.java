package org.activiti.cycle.impl.connector.signavio;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Test;

public class SignavioConnectorTest {

  @Test
  public void test() {
    // TODO: Write :-)
  }
  
  @Test
  public void testTransformation() throws IOException, JSONException {
    // NArf, we need a running signavio for this :-( So skipped for the moment
    
    // InputStream is =
    // this.getClass().getResourceAsStream("/org/activiti/cycle/impl/transform/signavio/engine-pool.json");
    // BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    // StringBuilder sb = new StringBuilder();
    // String line = null;
    // while ((line = reader.readLine()) != null) {
    // sb.append(line + "\n");
    // }
    // is.close();
    //
    // String json = new
    // SignavioConnector(null).transformJsonToBpmn20Xml(sb.toString());
    // String transformedJson = new
    // ExchangeSignavioUuidWithNameTransformation().transform(new
    // JSONObject(json)).toString();
    //    
    // System.out.println(json);
    // System.out.println(transformedJson);
  }
}
