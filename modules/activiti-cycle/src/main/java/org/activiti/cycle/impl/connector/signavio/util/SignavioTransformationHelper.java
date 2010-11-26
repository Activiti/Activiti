package org.activiti.cycle.impl.connector.signavio.util;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.connector.signavio.transform.JsonTransformation;
import org.activiti.cycle.impl.connector.signavio.transform.signavio.AdjustShapeNamesTransformation;
import org.activiti.cycle.impl.connector.signavio.transform.signavio.BpmnPoolExtraction;
import org.activiti.cycle.impl.connector.signavio.transform.signavio.ExchangeSignavioUuidWithNameTransformation;
import org.json.JSONObject;


public class SignavioTransformationHelper {

  static {
    registeredTransformations = new ArrayList<JsonTransformation>();
    // TODO: How to register JSON-Transformations

    // example with cutting out just the Engine Pool
    addTransformation(new BpmnPoolExtraction("Process Engine"));
    addTransformation(new ExchangeSignavioUuidWithNameTransformation());
    addTransformation(new AdjustShapeNamesTransformation());
  }
  
  /**
   * Where do we get the transformations from? How are they registered?
   * 
   * How can we extend that project specific?
   */
  public static List<JsonTransformation> registeredTransformations;

  public static void addTransformation(JsonTransformation transformation) {
    registeredTransformations.add(transformation);
  }

  
  public static List<JsonTransformation> getRegisteredTransformations() {
    return registeredTransformations;
  }
  
  public static String applyJsonTransformations(String sourceJson) {
    try {
      JSONObject jsonObject = new JSONObject(sourceJson);

      for (JsonTransformation trafo : getRegisteredTransformations()) {
        jsonObject = trafo.transform(jsonObject);
      }

      return jsonObject.toString();
    } catch (Exception e) {
      throw new RepositoryException("Exception occured while transformation of BPMN model", e);
    }
  }

}
