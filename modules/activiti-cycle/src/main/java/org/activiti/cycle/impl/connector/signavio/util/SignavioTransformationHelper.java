package org.activiti.cycle.impl.connector.signavio.util;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.connector.signavio.transform.JsonTransformation;
import org.activiti.cycle.impl.connector.signavio.transform.JsonTransformer;
import org.activiti.cycle.impl.connector.signavio.transform.pattern.BpmnPoolExtraction;
import org.activiti.cycle.impl.connector.signavio.transform.pattern.ExchangeSignavioUuidWithName;
import org.activiti.cycle.impl.connector.signavio.transform.pattern.MakeNamesUnique;
import org.activiti.cycle.impl.connector.signavio.transform.pattern.ReplaceEmptyShapeNamesWithTypes;
import org.json.JSONObject;

/**
 * @deprecated use {@link JsonTransformer} instead. 
 */
public class SignavioTransformationHelper {

  static {
    registeredTransformations = new ArrayList<JsonTransformation>();
    // TODO: How to register JSON-Transformations

    // example with cutting out just the Engine Pool
    addTransformation(new BpmnPoolExtraction("Process Engine"));
    addTransformation(new ReplaceEmptyShapeNamesWithTypes());
    addTransformation(new ExchangeSignavioUuidWithName());
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
