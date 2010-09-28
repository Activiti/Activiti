package org.activiti.cycle.impl.connector.demo.action;

import java.util.Map;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.ParameterizedHtmlFormTemplateAction;

/**
 * Demo action which just copies the artifact (maybe multiple times) to
 * demonstrate actions
 * 
 * @author ruecker
 */
public class CopyArtifactAction extends ParameterizedHtmlFormTemplateAction {

  private static final long serialVersionUID = 1L;

  @Override
  public String getFormResourceName() {
    return "/org/activiti/cycle/impl/connector/demo/action/CopyArtifactAction.html";
  }

  public void execute(RepositoryConnector connector, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception {
    int count = (Integer) getParameter(parameters, "copyCount", true, null, Integer.class);
    String targetName = (String) getParameter(parameters, "targetName", true, null, String.class);

    if (count==1) {
      copyArtifact(connector, artifact, targetName);
    }
    for (int i = 0; i < count; i++) {
      copyArtifact(connector, artifact, targetName + i);
    }
  }
  
  private void copyArtifact(RepositoryConnector connector, RepositoryArtifact artifact, String targetName) {    
    String path = artifact.getId().substring(0, artifact.getId().lastIndexOf("/"));

    // if (targetName.startsWith("/")) {
    // targetName = artifact.getMetadata().setParentFolderId() + targetName;
    // } else {
    // targetName = artifact.getMetadata().setParentFolderId() + "/" +
    // targetName;
    // }
    
    Content content = connector.getContent(artifact.getId(), artifact.getArtifactType().getDefaultContentRepresentation().getId());
    connector.createArtifact(path, targetName, artifact.getArtifactType().getId(), content);
  }
}
