package org.activiti.cycle.impl.processsolution.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.action.ParameterizedAction;
import org.activiti.cycle.context.CycleRequestContext;
import org.activiti.cycle.impl.RepositoryNodeCollectionImpl;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.impl.processsolution.ProcessSolutionAction;
import org.activiti.cycle.impl.processsolution.representation.ProcessSolutionHomeContentRepresentation;
import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;
import org.activiti.cycle.service.CyclePluginService;
import org.activiti.cycle.service.CycleProcessSolutionService;
import org.activiti.cycle.service.CycleRepositoryService;
import org.activiti.cycle.service.CycleServiceFactory;

/**
 * Virtual {@link RepositoryConnector} for {@link ProcessSolution}s
 * 
 * @author daniel.meyer@camunda.com
 */
public class ProcessSolutionConnector implements RepositoryConnector {

  protected CycleProcessSolutionService processSolutionService = CycleServiceFactory.getProcessSolutionService();

  protected CycleRepositoryService repositoryService = CycleServiceFactory.getRepositoryService();

  public static String PS_HOME_NAME = "/PSHOME";

  protected final String processSolutionId;

  public ProcessSolutionConnector(String id) {
    processSolutionId = id;
  }

  public boolean login(String username, String password) {
    // does not require login
    return true;
  }

  public static String getProcessSolutionId(String nodeId) {
    if (nodeId == null) {
      return null;
    }
    if (!nodeId.contains("/")) {
      return nodeId;
    }
    return nodeId.substring(0, nodeId.indexOf("/"));
  }

  public static String getVirtualFolderId(String nodeId) {
    if (nodeId == null) {
      return null;
    }
    String[] parts = nodeId.split("/");
    if (parts.length >= 2) {
      return parts[1];
    }
    return null;
  }

  public RepositoryNode getRepositoryNode(String id) throws RepositoryNodeNotFoundException {

    String processSolutionId = getProcessSolutionId(id);
    String virtualFolderId = getVirtualFolderId(id);
    VirtualRepositoryFolder virtualFolder = null;
    if (virtualFolderId != null) {
      virtualFolder = processSolutionService.getVirtualRepositoryFolderById(virtualFolderId);
      if (virtualFolder == null) {
        virtualFolderId = null;
      }
    }
    if ("".equals(id) || id == null) {
      throw new RepositoryNodeNotFoundException(id);
    }

    if ("/".equals(id)) {
      processSolutionId = this.processSolutionId;
    }

    if (!processSolutionId.equals(this.processSolutionId)) {
      processSolutionId = null;
    }
    ProcessSolution processSolution;
    RepositoryConnector connector;
    // get the vFolderId from the request:
    String vFolderId = CycleRequestContext.get("vFolderId", String.class);
    if (vFolderId != null && virtualFolderId == null && processSolutionId == null) {
      virtualFolderId = vFolderId;
      virtualFolder = processSolutionService.getVirtualRepositoryFolderById(vFolderId);
      processSolutionId = virtualFolder.getProcessSolutionId();
      connector = CycleComponentFactory.getCycleComponentInstance(RuntimeConnectorList.class, RuntimeConnectorList.class).getConnectorById(
              virtualFolder.getConnectorId());
      processSolution = processSolutionService.getProcessSolutionById(processSolutionId);
      if (virtualFolder.getReferencedNodeId().equals(id)) {
        return new ProcessSolutionFolder(getId(), "/" + processSolutionId + "/" + virtualFolderId, null, processSolution, null);
      }
    } else {

      if (processSolutionId == null) {
        throw new RepositoryNodeNotFoundException(id);
      }

      if (processSolutionId.equals(this.processSolutionId) == false) {
        throw new RepositoryNodeNotFoundException(id);
      }
      // id=id of a processSolution
      processSolution = processSolutionService.getProcessSolutionById(processSolutionId);

      if (id.endsWith(PS_HOME_NAME)) {
        return new ProcessSolutionArtifact(getId(), id, null, processSolution, null);
      }

      if (virtualFolderId == null) {
        return new ProcessSolutionFolder(getId(), id, null, processSolution, null);
      }

      virtualFolder = processSolutionService.getVirtualRepositoryFolderById(virtualFolderId);
      String relativePath = id.replace(processSolutionId + "/" + virtualFolderId, "");
      if (relativePath.length() == 0) {
        // id == processsolution/virtualFolderId
        return new ProcessSolutionFolder(getId(), id, virtualFolder, processSolution, null);
      }
      relativePath = id.replace(processSolutionId + "/" + virtualFolderId + "/", "");

      // id == processsolution/virtualFolderId/...
      connector = CycleComponentFactory.getCycleComponentInstance(RuntimeConnectorList.class, RuntimeConnectorList.class).getConnectorById(
              virtualFolder.getConnectorId());
      id = connector.concatenateNodeId(virtualFolder.getReferencedNodeId(), relativePath);
    }

    try {
      RepositoryFolder folder = connector.getRepositoryFolder(id);
      return new ProcessSolutionFolder(getId(), id, virtualFolder, processSolution, folder);
    } catch (Exception e) {
      RepositoryArtifact artifact = connector.getRepositoryArtifact(id);
      return new ProcessSolutionArtifact(getId(), id, virtualFolder, processSolution, artifact);
    }

  }
  public RepositoryArtifact getRepositoryArtifact(String id) throws RepositoryNodeNotFoundException {
    return (RepositoryArtifact) getRepositoryNode(id);
  }

  public Content getRepositoryArtifactPreview(String artifactId) throws RepositoryNodeNotFoundException {
    return null;
  }

  public RepositoryFolder getRepositoryFolder(String id) throws RepositoryNodeNotFoundException {
    return (RepositoryFolder) getRepositoryNode(id);
  }

  public RepositoryNodeCollection getChildren(String id) throws RepositoryNodeNotFoundException {
    List<RepositoryNode> resultList = new ArrayList<RepositoryNode>();
    ProcessSolutionFolder processSolutionFolder = (ProcessSolutionFolder) getRepositoryNode(id);
    VirtualRepositoryFolder virtualFolder = processSolutionFolder.getVirtualRepositoryFolder();
    RepositoryFolder wrappedFolder = (RepositoryFolder) processSolutionFolder.getWrappedNode();
    RepositoryNodeCollection childNodes = null;
    if (wrappedFolder != null) {
      // get child nodes of wrapped folder
      childNodes = repositoryService.getChildren(wrappedFolder.getConnectorId(), wrappedFolder.getNodeId());
    } else if (virtualFolder != null) {
      // get child nodes of virtual folder
      childNodes = repositoryService.getChildren(virtualFolder.getConnectorId(), virtualFolder.getReferencedNodeId());
    }
    if (childNodes != null) {
      for (RepositoryNode childNode : childNodes.asList()) {
        String childNodeId = childNode.getNodeId();
        childNodeId = childNodeId.replace(processSolutionFolder.getVirtualRepositoryFolder().getReferencedNodeId(), "");
        childNodeId = processSolutionId + "/" + processSolutionFolder.getVirtualRepositoryFolder().getId() + "/" + childNodeId;
        if (childNode instanceof RepositoryArtifact) {
          resultList.add(new ProcessSolutionArtifact(getId(), childNodeId, processSolutionFolder.getVirtualRepositoryFolder(), processSolutionFolder
                  .getProcessSolution(), (RepositoryArtifact) childNode));
        } else {
          resultList.add(new ProcessSolutionFolder(getId(), childNodeId, processSolutionFolder.getVirtualRepositoryFolder(), processSolutionFolder
                  .getProcessSolution(), (RepositoryFolder) childNode));
        }
      }
    } else {
      // add Home folder:
//      resultList.add(new ProcessSolutionArtifact(getId(), processSolutionId + PS_HOME_NAME, null, processSolutionFolder.processSolution, null));

      // get children of process solution:
      for (VirtualRepositoryFolder virtualChildfolder : processSolutionService.getFoldersForProcessSolution(processSolutionId)) {
        String childNodeId = processSolutionId + "/" + virtualChildfolder.getId();
        resultList.add(new ProcessSolutionFolder(getId(), childNodeId, virtualChildfolder, processSolutionFolder.getProcessSolution(), null));
      }
    }
    return new RepositoryNodeCollectionImpl(resultList);
  }

  public RepositoryArtifact createArtifact(String parentFolderId, String artifactName, String artifactType, Content artifactContent)
          throws RepositoryNodeNotFoundException {
    ProcessSolutionFolder folder = (ProcessSolutionFolder) getRepositoryNode(parentFolderId);
    if (folder.getVirtualRepositoryFolder() == null) {
      throw new RepositoryException("Cannot create artifact in the top-level folder. ");
    }
    RepositoryArtifact newArtifact = null;
    if (folder.getWrappedNode() == null) {
      newArtifact = repositoryService.createArtifact(folder.getVirtualRepositoryFolder().getConnectorId(), folder.getVirtualRepositoryFolder()
              .getReferencedNodeId(), artifactName, artifactType, artifactContent);
    } else {
      newArtifact = repositoryService.createArtifact(folder.getWrappedNode().getConnectorId(), folder.getWrappedNode().getNodeId(), artifactName, artifactType,
              artifactContent);
    }
    String relativePath = newArtifact.getNodeId().replace(folder.getVirtualRepositoryFolder().getReferencedNodeId(), "");
    String virtualPath = folder.getNodeId() + "/" + relativePath;
    return new ProcessSolutionArtifact(getId(), virtualPath, folder.getVirtualRepositoryFolder(), folder.getProcessSolution(), newArtifact);
  }

  public RepositoryArtifact createArtifactFromContentRepresentation(String parentFolderId, String artifactName, String artifactType,
          String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException {
    return createArtifact(parentFolderId, artifactName, artifactType, artifactContent);
  }

  public RepositoryArtifact createEmptyArtifact(String parentFolderId, String artifactName, String artifactType) throws RepositoryNodeNotFoundException {
    ProcessSolutionFolder folder = (ProcessSolutionFolder) getRepositoryNode(parentFolderId);
    if (folder.getVirtualRepositoryFolder() == null) {
      throw new RepositoryException("Cannot create artifact in the top-level folder. ");
    }
    RepositoryArtifact newArtifact = null;
    if (folder.getWrappedNode() == null) {
      newArtifact = repositoryService.createEmptyArtifact(folder.getVirtualRepositoryFolder().getConnectorId(), folder.getVirtualRepositoryFolder()
              .getReferencedNodeId(), artifactName, artifactType);
    } else {
      newArtifact = repositoryService.createEmptyArtifact(folder.getWrappedNode().getConnectorId(), folder.getWrappedNode().getNodeId(), artifactName,
              artifactType);
    }
    String relativePath = newArtifact.getNodeId().replace(folder.getVirtualRepositoryFolder().getReferencedNodeId(), "");
    String virtualPath = folder.getNodeId() + "/" + relativePath;
    return new ProcessSolutionArtifact(getId(), virtualPath, folder.getVirtualRepositoryFolder(), folder.getProcessSolution(), newArtifact);
  }

  public RepositoryFolder createFolder(String parentFolderId, String name) throws RepositoryNodeNotFoundException {
    ProcessSolutionFolder folder = (ProcessSolutionFolder) getRepositoryNode(parentFolderId);
    if (folder.getVirtualRepositoryFolder() == null) {
      throw new RepositoryException("Cannot create artifact in the top-level folder. ");
    }
    if (folder.getWrappedNode() == null) {
      return repositoryService.createFolder(folder.getVirtualRepositoryFolder().getConnectorId(), folder.getVirtualRepositoryFolder().getReferencedNodeId(),
              name);
    }
    return repositoryService.createFolder(folder.getWrappedNode().getConnectorId(), folder.getWrappedNode().getNodeId(), name);
  }

  public Content getContent(String artifactId) throws RepositoryNodeNotFoundException {
    ProcessSolutionArtifact artifact = (ProcessSolutionArtifact) getRepositoryNode(artifactId);
    if (artifactId.endsWith(PS_HOME_NAME)) {
      return CycleComponentFactory.getCycleComponentInstance(ProcessSolutionHomeContentRepresentation.class, ProcessSolutionHomeContentRepresentation.class)
              .getContent(artifact);
    }
    return repositoryService.getContent(artifact.wrappedNode.getConnectorId(), artifact.wrappedNode.getNodeId());
  }

  public void updateContent(String artifactId, Content content) throws RepositoryNodeNotFoundException {
    ProcessSolutionArtifact artifact = (ProcessSolutionArtifact) getRepositoryNode(artifactId);
    repositoryService.updateContent(artifact.wrappedNode.getConnectorId(), artifact.wrappedNode.getNodeId(), content);
  }

  public void updateContent(String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException {
    updateContent(artifactId, content);
  }

  public void deleteArtifact(String artifactId) throws RepositoryNodeNotFoundException {
    ProcessSolutionArtifact artifact = (ProcessSolutionArtifact) getRepositoryNode(artifactId);
    repositoryService.deleteArtifact(artifact.wrappedNode.getConnectorId(), artifact.wrappedNode.getNodeId());
  }

  public void deleteFolder(String folderId) throws RepositoryNodeNotFoundException {
    ProcessSolutionFolder folder = (ProcessSolutionFolder) getRepositoryNode(folderId);
    repositoryService.deleteFolder(folder.wrappedNode.getConnectorId(), folder.wrappedNode.getNodeId());
  }

  public void executeParameterizedAction(String artifactId, String actionId, Map<String, Object> parameters) throws Exception {
    ProcessSolutionArtifact artifact = (ProcessSolutionArtifact) getRepositoryNode(artifactId);
    CyclePluginService pluginService = CycleServiceFactory.getCyclePluginService();
    ParameterizedAction action = pluginService.getParameterizedActionById(actionId);
    if (action instanceof ProcessSolutionAction) {
      // execute ProcessSolutionActions using the ProcessSolutionArtifact and
      // the ProcessSolutionConnector
      action.execute(this, artifact, parameters);
    } else {
      // execute non-ProcessSolutionActions using the underlying connector
      repositoryService.executeParameterizedAction(artifact.wrappedNode.getConnectorId(), artifact.wrappedNode.getNodeId(), actionId, parameters);
    }
  }

  public boolean isLoggedIn() {
    return false;
  }

  public ContentRepresentation getDefaultContentRepresentation(String artifactId) throws RepositoryNodeNotFoundException {
    ProcessSolutionArtifact artifact = (ProcessSolutionArtifact) getRepositoryNode(artifactId);
    RuntimeConnectorList connectorList = CycleComponentFactory.getCycleComponentInstance(RuntimeConnectorList.class, RuntimeConnectorList.class);
    RepositoryConnector connector = connectorList.getConnectorById(artifact.connectorId);
    return connector.getDefaultContentRepresentation(artifact.wrappedNode.getNodeId());
  }

  public void startConfiguration() {
  }

  public void addConfiguration(Map<String, Object> configurationValues) {
  }

  public void addConfigurationEntry(String key, Object value) {
  }

  public void configurationFinished() {
  }

  public String[] getConfigurationKeys() {
    return null;
  }

  public void setId(String connectorId) {
  }

  public String getId() {
    return "ps-" + processSolutionId;
  }

  public String getName() {
    try {
      return processSolutionService.getProcessSolutionById(processSolutionId).getLabel();
    } catch (Exception e) {
      return "Deleted processSolution - " + processSolutionId;
    }
  }

  public void setName(String name) {
  }

  public String concatenateNodeId(String prefix, String suffix) {
    return null;
  }

}
