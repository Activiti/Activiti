package org.activiti.cycle.impl.action;

import java.util.Map;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.ParameterizedHtmlFormTemplateAction;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;
import org.activiti.cycle.service.CycleRepositoryService;
import org.activiti.cycle.service.CycleServiceFactory;

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
  public static final String PARAM_COMMENT_NAME = "comment";
  public static final String CREATE_LINK_NAME = "createLink";

  public AbstractCopyBaseAction(String actionId) {
    super(actionId);
  }

  public void execute(RepositoryConnector connector, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception {
    String targetFolderId = (String) getParameter(parameters, PARAM_TARGET_FOLDER, true, null, String.class);
    String targetName = (String) getParameter(parameters, PARAM_TARGET_NAME, false, artifact.getMetadata().getName(), String.class);
    String comment = (String) getParameter(parameters, PARAM_COMMENT_NAME, false, null, String.class);
    RepositoryConnector targetConnector = (RepositoryConnector) getParameter(parameters, PARAM_TARGET_CONNECTOR, true, null, RepositoryConnector.class);

    boolean createLink = (Boolean) getParameter(parameters, CREATE_LINK_NAME, true, Boolean.TRUE, Boolean.class);

    Content content = getContent(artifact, connector);
    targetName = getName(targetName);

    // no transaction required: atomic
    RepositoryArtifact targetArtifact = targetConnector.createArtifact(targetFolderId, targetName, artifact.getArtifactType().getName(), content);

    if (createLink) {
      RepositoryArtifactLink link = new RepositoryArtifactLinkEntity();
      link.setSourceArtifact(artifact);
      link.setTargetArtifact(targetArtifact);
      link.setComment(comment);
      link.setLinkType(RepositoryArtifactLinkEntity.TYPE_COPY);
      CycleRepositoryService repositoryService = CycleServiceFactory.getRepositoryService();
      repositoryService.addArtifactLink(link);
    }
  }

  protected abstract Content getContent(RepositoryArtifact artifact, RepositoryConnector connector);
  
  protected String getName(String paramName) {
    return paramName;    
  }

  public String getFormResourceName() {
    return getDefaultFormName();
  }

}
