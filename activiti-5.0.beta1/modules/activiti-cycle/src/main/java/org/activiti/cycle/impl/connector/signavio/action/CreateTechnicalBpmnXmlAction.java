package org.activiti.cycle.impl.connector.signavio.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.Content;
import org.activiti.cycle.ParametrizedFreemakerTemplateAction;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.activiti.cycle.impl.connector.signavio.provider.JsonProvider;
import org.activiti.cycle.impl.plugin.ActivitiCyclePluginRegistry;
import org.activiti.cycle.impl.transform.JsonTransformation;
import org.activiti.cycle.impl.transform.signavio.AdjustShapeNamesTransformation;
import org.activiti.cycle.impl.transform.signavio.BpmnPoolExtraction;
import org.activiti.cycle.impl.transform.signavio.ExchangeSignavioUuidWithNameTransformation;
import org.activiti.cycle.impl.transform.signavio.RemedyTemporarySignavioIncompatibilityTransformation;
import org.json.JSONObject;

/**
 * This action creates a technical BPMN 2.0 XML for the process engines. It
 * copies the XML from Signavio to a given {@link RepositoryFolder}.
 * 
 * By doing that, registered plugins / transformations are executed. The link
 * between the two {@link RepositoryArtifact}s is remembered (TODO).
 * 
 * @author bernd.ruecker@camunda.com
 */
public class CreateTechnicalBpmnXmlAction extends ParametrizedFreemakerTemplateAction {

  public static final String PARAM_TARGET_FOLDER = "targetFolder";
  public static final String PARAM_TARGET_NAME = "targetName";
  public static final String PARAM_COMMENT = "comment";

  /**
   * Where do we get the transformations from? How are they registered?
   * 
   * How can we extend that project specific?
   */
  public static List<JsonTransformation> registeredTransformations = new ArrayList<JsonTransformation>();
  
  static {
     // TODO: How to register JSON-Transformations

    // example with cutting out just the Engine Pool
    addTransformation(new BpmnPoolExtraction("Process Engine"));
    addTransformation(new ExchangeSignavioUuidWithNameTransformation());
    addTransformation(new AdjustShapeNamesTransformation());
  }

  public static void addTransformation(JsonTransformation transformation) {
    registeredTransformations.add(transformation);
  }

  @Override
  public String getFormResourceName() {
    return getDefaultFormName();
  }  

  @Override
  public void execute(Map<String, Object> parameters) throws Exception {
    // TODO: Check with Nils that we get the object instead of the string in
    // here!
    RepositoryFolder targetFolder = (RepositoryFolder) getParameter(parameters, PARAM_TARGET_FOLDER, true, null, RepositoryFolder.class);
    String targetName = (String) getParameter(parameters, PARAM_TARGET_NAME, false, getProcessName(), String.class);
    String comment = (String) getParameter(parameters, PARAM_COMMENT, false, null, String.class);
    
    String sourceJson = getBpmn20Json();
    String transformedJson = applyJsonTransformations(sourceJson);
    String bpmnXml = transformToBpmn20(transformedJson);
    createTargetArtifact(targetFolder, targetName + ".bpmn20.xml", bpmnXml, SignavioConnector.BPMN_2_0_XML);

    // TODO: Think about that more, does it make sense like this?
    targetFolder.getConnector().commitPendingChanges(comment);
  }

  protected String getBpmn20Json() {
    return getArtifact().getConnector().getContent(getArtifact().getId(), JsonProvider.NAME).asString();
  }

  protected String applyJsonTransformations(String sourceJson) {
    try {
      JSONObject jsonObject = new JSONObject(sourceJson);

      for (JsonTransformation trafo : registeredTransformations) {
        jsonObject = trafo.transform(jsonObject);
      }

      return jsonObject.toString();
    } catch (Exception e) {
      throw new RepositoryException("Exception occured while transformation of BPMN model", e);
    }
  }

  protected String transformToBpmn20(String transformedJson) {
    String bpmnXml = getSignavioConnector().transformJsonToBpmn20Xml(transformedJson);
    bpmnXml = new RemedyTemporarySignavioIncompatibilityTransformation().transformBpmn20Xml(bpmnXml);
    return bpmnXml;
  }

  public void createTargetArtifact(RepositoryFolder targetFolder, String artifactId, String bpmnXml, String artifactTypeIdentifier) {
    RepositoryArtifact targetArtifact = new RepositoryArtifact(targetFolder.getConnector());
    
    // TODO: Check naming policy
    targetArtifact.setId(artifactId);
    // TODO: Check artifac types / registry
    targetArtifact.setArtifactType(ActivitiCyclePluginRegistry.getArtifactTypeByIdentifier(artifactTypeIdentifier));

    Content content = new Content();
    content.setValue(bpmnXml);

    targetFolder.getConnector().createNewArtifact(targetFolder.getId(), targetArtifact, content);
  }
  
  public String getProcessName() {
    return getArtifact().getMetadata().getName();
  }

  public SignavioConnector getSignavioConnector() {
    return (SignavioConnector) getArtifact().getOriginalConnector();
  }
  
  @Override
  public String getLabel() {
    return "create technical model";
  }

}
