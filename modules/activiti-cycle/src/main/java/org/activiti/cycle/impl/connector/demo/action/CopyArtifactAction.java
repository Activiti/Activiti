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

    RepositoryConnector targetFolderConnector = (RepositoryConnector) getParameter(parameters, "targetConnectorId", true, null, RepositoryConnector.class);
    String targetFolderId = (String) getParameter(parameters, "targetFolderId", true, null, String.class);

    // retrieve the platform independent file separator
    // String fileSeperator = System.getProperty("file.separator");\
    // Combine targetFolder and targetName to put together the targetPath
    // String targetPath = targetFolder +
    // ((!targetFolder.endsWith(fileSeperator) &&
    // !targetName.startsWith(fileSeperator)) ? fileSeperator : "") +
    // targetName;

    if (count == 1) {
      copyArtifact(connector, targetFolderConnector, artifact, targetFolderId, targetName);
    } else {
      for (int i = 0; i < count; i++) {
        copyArtifact(connector, targetFolderConnector, artifact, targetFolderId, targetName + i);
      }
    }

  }
  private void copyArtifact(RepositoryConnector sourceConnector, RepositoryConnector targetConnector, RepositoryArtifact artifact, String targetFolder,
          String targetName) {
    // String path = artifact.getId().substring(0,
    // artifact.getId().lastIndexOf("/"));

    // if (targetName.startsWith("/")) {
    // targetName = artifact.getMetadata().setParentFolderId() + targetName;
    // } else {
    // targetName = artifact.getMetadata().setParentFolderId() + "/" +
    // targetName;
    // }

    Content content = sourceConnector.getContent(artifact.getNodeId(), artifact.getArtifactType().getDefaultContentRepresentation().getId());
    targetConnector.createArtifact(targetFolder, targetName, artifact.getArtifactType().getId(), content);
  }
}
