package org.activiti.cycle.impl.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.components.RuntimeConnectorList;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.impl.RepositoryFolderImpl;
import org.activiti.cycle.impl.RepositoryNodeCollectionImpl;
import org.activiti.cycle.impl.conf.PasswordEnabledRepositoryConnectorConfiguration;
import org.activiti.cycle.impl.connector.util.TransactionalConnectorUtils;
import org.activiti.cycle.impl.db.CycleLinkDao;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;
import org.activiti.cycle.service.CycleRepositoryService;

/**
 * @author bernd.ruecker@camunda.com
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class CycleRepositoryServiceImpl implements CycleRepositoryService {

  private CycleServiceConfiguration cycleServiceConfiguration;

  private CycleLinkDao linkDao;

  public CycleRepositoryServiceImpl() {
  }

  /**
   * perform initialization after dependencies are set.
   */
  public void initialize() {
    // perform initialization
  }

  public void setCycleServiceConfiguration(CycleServiceConfiguration cycleServiceConfiguration) {
    this.cycleServiceConfiguration = cycleServiceConfiguration;
  }

  public void setLinkDao(CycleLinkDao linkDao) {
    this.linkDao = linkDao;
  }

  // implementation of CycleService methods

  public boolean login(String username, String password, String connectorId) {
    RepositoryConnector conn = getRepositoryConnector(connectorId);
    if (conn != null) {

      if (conn.getConfiguration() instanceof PasswordEnabledRepositoryConnectorConfiguration) {
        PasswordEnabledRepositoryConnectorConfiguration configuration = (PasswordEnabledRepositoryConnectorConfiguration) conn.getConfiguration();
        configuration.setUser(username);
        configuration.setPassword(password);
      }

      conn.login(username, password);
      return true;
    }
    return false;
  }

  protected List<RepositoryConnector> getRuntimeRepositoryConnectors() {
    RuntimeConnectorList connectorList = CycleSessionContext.get(RuntimeConnectorList.class);
    return connectorList.getConnectors();
  }

  /**
   * commit pending changes in all repository connectors configured
   */
  public void commitPendingChanges(String comment) {
    for (RepositoryConnector connector : getRuntimeRepositoryConnectors()) {
      TransactionalConnectorUtils.commitTransaction(connector, comment);
    }
  }

  public RepositoryNodeCollection getChildren(String connectorId, String nodeId) {
    // special handling for root
    if ("/".equals(connectorId)) {
      return getRepoRootFolders();
    }

    RepositoryConnector connector = getRepositoryConnector(connectorId);
    return connector.getChildren(nodeId);
  }

  public RepositoryNodeCollection getRepoRootFolders() {
    ArrayList<RepositoryNode> nodes = new ArrayList<RepositoryNode>();
    for (RepositoryConnector connector : getRuntimeRepositoryConnectors()) {

      RepositoryFolderImpl folder = new RepositoryFolderImpl(connector.getConfiguration().getId(), "/");
      folder.getMetadata().setName(connector.getConfiguration().getName());
      folder.getMetadata().setParentFolderId("/");
      nodes.add(folder);

    }
    return new RepositoryNodeCollectionImpl(nodes);
  }
  
  public RepositoryNode getRepositoryNode(String connectorId, String nodeId) {
    RepositoryConnector connector = getRepositoryConnector(connectorId);
    RepositoryNode repositoryNode = connector.getRepositoryNode(nodeId);
    return repositoryNode;
  }

  public RepositoryArtifact getRepositoryArtifact(String connectorId, String artifactId) {
    RepositoryConnector connector = getRepositoryConnector(connectorId);
    RepositoryArtifact repositoryArtifact = connector.getRepositoryArtifact(artifactId);
    return repositoryArtifact;
  }

  public Content getRepositoryArtifactPreview(String connectorId, String artifactId) throws RepositoryNodeNotFoundException {
    RepositoryConnector connector = getRepositoryConnector(connectorId);
    return connector.getRepositoryArtifactPreview(artifactId);
  }

  public RepositoryFolder getRepositoryFolder(String connectorId, String artifactId) {
    RepositoryConnector connector = getRepositoryConnector(connectorId);
    RepositoryFolder repositoryFolder = connector.getRepositoryFolder(artifactId);
    return repositoryFolder;
  }

  public RepositoryArtifact createArtifact(String connectorId, String parentFolderId, String artifactName, String artifactType, Content artifactContent)
          throws RepositoryNodeNotFoundException {
    return getRepositoryConnector(connectorId).createArtifact(parentFolderId, artifactName, artifactType, artifactContent);
  }

  public RepositoryArtifact createArtifactFromContentRepresentation(String connectorId, String parentFolderId, String artifactName, String artifactType,
          String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException {
    return getRepositoryConnector(connectorId).createArtifactFromContentRepresentation(parentFolderId, artifactName, artifactType, contentRepresentationName,
            artifactContent);
  }

  public void updateContent(String connectorId, String artifactId, Content content) throws RepositoryNodeNotFoundException {
    RepositoryConnector connector = getRepositoryConnector(connectorId);
    connector.updateContent(artifactId, content);
  }

  public void updateContent(String connectorId, String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException {
    RepositoryConnector connector = getRepositoryConnector(artifactId);
    connector.updateContent(artifactId, contentRepresentationName, content);
  }

  public RepositoryFolder createFolder(String connectorId, String parentFolderId, String name) throws RepositoryNodeNotFoundException {
    return getRepositoryConnector(connectorId).createFolder(parentFolderId, name);
  }

  public void deleteArtifact(String connectorId, String artifactId) {
    getRepositoryConnector(connectorId).deleteArtifact(artifactId);
  }

  public void deleteFolder(String connectorId, String folderId) {
    getRepositoryConnector(connectorId).deleteFolder(folderId);
  }

  public Content getContent(String connectorId, String artifactId) throws RepositoryNodeNotFoundException {
    return getRepositoryConnector(connectorId).getContent(artifactId);
  }

  public void executeParameterizedAction(String connectorId, String artifactId, String actionId, Map<String, Object> parameters) throws Exception {
    RepositoryConnector connector = getRepositoryConnector(connectorId);

    // TODO: (Nils Preusker, 20.10.2010), find a better way to solve this!
    for (String key : parameters.keySet()) {
      if (key.equals("targetConnectorId")) {
        RepositoryConnector targetConnector = getRepositoryConnector((String) parameters.get(key));
        parameters.put(key, targetConnector);
      }
    }

    connector.executeParameterizedAction(artifactId, actionId, parameters);
  }

//  public List<ArtifactType> getSupportedArtifactTypes(String connectorId, String folderId) {
//    if (folderId == null || folderId.length() <= 1) {
//      // "virtual" root folder doesn't support any artifact types
//      return new ArrayList<ArtifactType>();
//    }
//    return getRepositoryConnector(connectorId).getSupportedArtifactTypes(folderId);
//  }

  // RepositoryArtifactLink specific methods

  public void addArtifactLink(RepositoryArtifactLink repositoryArtifactLink) {
    if (repositoryArtifactLink instanceof RepositoryArtifactLinkEntity) {
      linkDao.insertArtifactLink((RepositoryArtifactLinkEntity) repositoryArtifactLink);
    } else {
      RepositoryArtifactLinkEntity cycleLink = new RepositoryArtifactLinkEntity();

      cycleLink.setId(repositoryArtifactLink.getId());

      // set source artifact attributes
      cycleLink.setSourceConnectorId(repositoryArtifactLink.getSourceArtifact().getConnectorId());
      cycleLink.setSourceArtifactId(repositoryArtifactLink.getSourceArtifact().getNodeId());
      cycleLink.setSourceElementId(repositoryArtifactLink.getSourceElementId());
      cycleLink.setSourceElementName(repositoryArtifactLink.getSourceElementName());
      // cycleLink.setSourceRevision(repositoryArtifactLink.getSourceArtifact().getArtifactType().getRevision());

      // set target artifact attributes
      cycleLink.setTargetConnectorId(repositoryArtifactLink.getTargetArtifact().getConnectorId());
      cycleLink.setTargetArtifactId(repositoryArtifactLink.getTargetArtifact().getNodeId());
      cycleLink.setTargetElementId(repositoryArtifactLink.getTargetElementId());
      cycleLink.setTargetElementName(repositoryArtifactLink.getTargetElementName());
      // cycleLink.setTargetRevision(repositoryArtifactLink.getTargetArtifact().getArtifactType().getRevision());

      cycleLink.setLinkType(repositoryArtifactLink.getLinkType());
      cycleLink.setComment(repositoryArtifactLink.getComment());
      cycleLink.setLinkedBothWays(false);

      linkDao.insertArtifactLink(cycleLink);
    }
  }

  public List<RepositoryArtifactLink> getArtifactLinks(String sourceConnectorId, String sourceArtifactId) {
    List<RepositoryArtifactLink> artifactLinks = new ArrayList<RepositoryArtifactLink>();

    List<RepositoryArtifactLinkEntity> linkResultList = linkDao.getOutgoingArtifactLinks(sourceConnectorId, sourceArtifactId);
    for (RepositoryArtifactLinkEntity entity : linkResultList) {
      entity.resolveArtifacts(this);
      artifactLinks.add(entity);
    }

    return artifactLinks;
  }

  public List<RepositoryArtifactLink> getIncomingArtifactLinks(String targetConnectorId, String targetArtifactId) {
    List<RepositoryArtifactLink> artifactLinks = new ArrayList<RepositoryArtifactLink>();
    List<RepositoryArtifactLinkEntity> linkResultList = linkDao.getIncomingArtifactLinks(targetConnectorId, targetArtifactId);
    for (RepositoryArtifactLinkEntity entity : linkResultList) {
      entity.resolveArtifacts(this);
      artifactLinks.add(entity);
    }
    return artifactLinks;
  }

  public void deleteLink(String linkId) {
    linkDao.deleteArtifactLink(linkId);
  }

  private RepositoryConnector getRepositoryConnector(String connectorId) {
    for (RepositoryConnector connector : getRuntimeRepositoryConnectors()) {
      if (connector.getConfiguration().getId().equals(connectorId)) {
        return connector;
      }
    }
    throw new RepositoryException("Couldn't find Repository Connector with id '" + connectorId + "'");
  }

}
