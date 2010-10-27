package org.activiti.cycle.impl.connector.signavio.action;

import java.util.Map;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.ParameterizedHtmlFormTemplateAction;

/**
 * This action copies any artifact to another location. Extend it to specify the
 * used content representation id
 * 
 * TODO: Move to a folder for generic base classes
 * 
 * @author bernd.ruecker@camunda.com
 */
public abstract class AbstractCopyBaseAction extends ParameterizedHtmlFormTemplateAction {

  private static final long serialVersionUID = 1L;
  
  public static final String PARAM_TARGET_FOLDER = "targetFolderId";
  public static final String PARAM_TARGET_CONNECTOR = "targetConnectorId";
  public static final String PARAM_TARGET_NAME = "targetName";
  public static final String PARAM_COMMENT = "comment";

  public AbstractCopyBaseAction(String actionId) {
    super(actionId);
  }

  public void execute(RepositoryConnector connector, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception {
    String targetFolderId = (String) getParameter(parameters, PARAM_TARGET_FOLDER, true, null, String.class);
    String targetName = (String) getParameter(parameters, PARAM_TARGET_NAME, false, artifact.getMetadata().getName(), String.class);
    String comment = (String) getParameter(parameters, PARAM_COMMENT, false, null, String.class);
    RepositoryConnector targetConnector = (RepositoryConnector) getParameter(parameters, PARAM_TARGET_CONNECTOR, true, null, RepositoryConnector.class);
    
    String contentAsString = connector.getContent(artifact.getNodeId(), getContentRepresentationIdToUse()).asString();
    Content content = new Content();
    content.setValue(contentAsString);
    targetConnector.createArtifact(targetFolderId, targetName, artifact.getArtifactType().getId(), content);

    // TODO: Think about that more, does it make sense like this?
    targetConnector.commitPendingChanges(comment);
  }

  public abstract String getContentRepresentationIdToUse();

  @Override
  public String getFormResourceName() {
    return getDefaultFormName();
  }
  
}
