package org.activiti.cycle.impl.connector.signavio.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.impl.ParameterizedHtmlFormTemplateAction;
import org.activiti.cycle.impl.connector.fs.FileSystemPluginDefinition;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.activiti.cycle.impl.connector.signavio.SignavioPluginDefinition;
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
public class CreateTechnicalBpmnXmlAction extends ParameterizedHtmlFormTemplateAction {

  public static final String PARAM_TARGET_FOLDER = "targetFolder";
  public static final String PARAM_TARGET_CONNECTOR = "targetFolderConnector";
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

  public void execute(RepositoryConnector connector, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception {
    // TODO: Check with Nils that we get the object instead of the string in
    // here!
    String targetFolderId = (String) getParameter(parameters, PARAM_TARGET_FOLDER, true, null, String.class);
    String targetName = (String) getParameter(parameters, PARAM_TARGET_NAME, false, getProcessName(artifact), String.class);
    String comment = (String) getParameter(parameters, PARAM_COMMENT, false, null, String.class);
    RepositoryConnector targetConnector = (RepositoryConnector) getParameter(parameters, PARAM_TARGET_CONNECTOR, true, null, RepositoryConnector.class);
    
    String sourceJson = getBpmn20Json((SignavioConnector) connector, artifact);
    String transformedJson = applyJsonTransformations(sourceJson);
    String bpmnXml = transformToBpmn20((SignavioConnector) connector, transformedJson);
    createTargetArtifact(targetConnector, targetFolderId, targetName + ".bpmn20.xml", bpmnXml, FileSystemPluginDefinition.ARTIFACT_TYPE_BPMN_20_XML);

    // TODO: Think about that more, does it make sense like this?
    targetConnector.commitPendingChanges(comment);
  }

  protected String getBpmn20Json(RepositoryConnector connector, RepositoryArtifact artifact) {
    return connector.getContent(artifact.getId(), SignavioPluginDefinition.CONTENT_REPRESENTATION_ID_JSON).asString();
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

  protected String transformToBpmn20(SignavioConnector connector, String transformedJson) {
    String bpmnXml = connector.transformJsonToBpmn20Xml(transformedJson);
    bpmnXml = new RemedyTemporarySignavioIncompatibilityTransformation().transformBpmn20Xml(bpmnXml);
    return bpmnXml;
  }

  public void createTargetArtifact(RepositoryConnector targetConnector, String targetFolderId, String artifactId, String bpmnXml, String artifactTypeId) {
    Content content = new Content();
    content.setValue(bpmnXml);
    targetConnector.createArtifact(targetFolderId, artifactId, artifactTypeId, content);
  }
  
  public String getProcessName(RepositoryArtifact artifact) {
    return artifact.getMetadata().getName();
  }

  @Override
  public String getFormResourceName() {
    return getDefaultFormName();
  }
  
}
