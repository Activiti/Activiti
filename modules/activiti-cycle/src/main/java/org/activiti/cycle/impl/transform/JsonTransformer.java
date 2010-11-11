package org.activiti.cycle.impl.transform;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonTransformer {

  List<JsonTransformation> transformations = new ArrayList<JsonTransformation>();
  
  public void addJsonTransformation(JsonTransformation transformation) {
    transformations.add(transformation);
  }

  public JSONObject transform(JSONObject json) throws JSONException {
    for (JsonTransformation transformation : transformations) {
      json = transformation.transform(json);
    }
    return json;
  }

  public JSONObject transform(String json) throws JSONException {
    return transform(new JSONObject(json));
  }

}
