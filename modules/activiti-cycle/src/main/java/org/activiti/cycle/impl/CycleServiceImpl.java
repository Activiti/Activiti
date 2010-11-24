package org.activiti.cycle.impl;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.Content;
import org.activiti.cycle.CycleService;
import org.activiti.cycle.CycleTagContent;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.RepositoryAuthenticationException;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.RepositoryNodeTag;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.PasswordEnabledRepositoryConnectorConfiguration;
import org.activiti.cycle.impl.connector.demo.DemoConnectorConfiguration;
import org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration;
import org.activiti.cycle.impl.connector.util.TransactionalConnectorUtils;
import org.activiti.cycle.impl.connector.view.TagConnectorConfiguration;
import org.activiti.cycle.impl.db.CycleConfigurationService;
import org.activiti.cycle.impl.db.CycleDAO;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;
import org.activiti.cycle.impl.db.entity.RepositoryNodeTagEntity;
import org.activiti.cycle.impl.db.impl.CycleConfigurationServiceImpl;
import org.activiti.cycle.impl.db.impl.CycleDaoMyBatisImpl;
import org.activiti.cycle.impl.plugin.PluginFinder;

/**
 * Connector to represent customized view for a user of cycle to hide all the
 * internal configuration and {@link RepositoryConnector} stuff from the client
 * (e.g. the webapp)
 * 
 * @author bernd.ruecker@camunda.com
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class CycleServiceImpl implements CycleService {

  protected static class ConnectorList implements Serializable {
    private static final long serialVersionUID = 1L;
    // the transient field keeps the servlet container from serializing the connectors in the session
    // TODO: needs testing: When do servlet containers serialize/deserialize? Tomcat seems to do it 
    // between shutdowns / startups. At the moment I would qualify this as a 'hack' - Daniel Meyer
    private transient List<RepositoryConnector> connectors;           
  }
  
	private CycleDAO cycleDAO;

	private List<RepositoryConnector> repositoryConnectors;
	
	

	// private static ThreadLocal<CycleService> currentCycleService = new
	// ThreadLocal<CycleService>();

	public CycleServiceImpl(List<RepositoryConnector> repositoryConnectors) {

		PluginFinder.checkPluginInitialization();
		this.cycleDAO = new CycleDaoMyBatisImpl();

		this.repositoryConnectors = repositoryConnectors;

		for (RepositoryConnector repositoryConnector : repositoryConnectors) {
			repositoryConnector.getConfiguration().setCycleService(this);
		}

		// If we get here we can assume that all the required logins are
		// available
		// and we can now perform the login for those connectors that require it

		for (RepositoryConnector connector : this.repositoryConnectors) {
			if (PasswordEnabledRepositoryConnectorConfiguration.class.isInstance(connector.getConfiguration())) {
				PasswordEnabledRepositoryConnectorConfiguration conf = (PasswordEnabledRepositoryConnectorConfiguration) connector
						.getConfiguration();
				String username = conf.getUser();
				String password = conf.getPassword();
				try {
					this.login(username, password, conf.getId());
				} catch (RepositoryException e) {
					Map<String, String> connectorMap = new HashMap<String, String>();
					connectorMap.put(conf.getId(), conf.getName());
					throw new RepositoryAuthenticationException("Repository authentication error: couldn't login to " + conf.getName(),
							connectorMap, e);
				}
			}
		}

		// add tag connector hard coded for the moment (at the first node in the
		// tree)
		this.repositoryConnectors.add(0, new TagConnectorConfiguration(this).createConnector());
	}

	// bootstrapping for cycle

	/**
	 * Provides a static factory method for CycleService instances. Checks
	 * whether the HttpSession contains an instance for the specified user name
	 * and creates a new instance if none is found.
	 * 
	 * @param currentUserId
	 *            the user id of the currently logged in user
	 * @param session
	 *            the HttpSession object from the currently logged in user
	 * @return the CycleService instance for the currently logged in user
	 */
	public static CycleService getCycleService(String currentUserId, HttpSession session, List<RepositoryConnector> connectors) {
		String key = currentUserId + "_cycleService";
		CycleService cycleService = (CycleService) session.getAttribute(key);
		if (cycleService == null) {
			cycleService = new CycleServiceImpl(connectors);
			session.setAttribute(key, cycleService);
		}
		return cycleService;
	}

	/**
	 * Provides access to the list of configured repository connectors for the
	 * current user. If the list is not yet present as a session attribute, it
	 * will be loaded from the database and persisted on the session.
	 * 
	 * @param currentUserId
	 *            the user id of the currently logged in user
	 * @param session
	 *            the HttpSession object from the currently logged in user
	 * @return list of configured repository connectors for the current user
	 */
	public static List<RepositoryConnector> getConfiguredRepositoryConnectors(String currentUserId, HttpSession session) {
		String key = currentUserId + "_cycleConfiguredRepositoryConnectors";
		
		ConnectorList connectorList = (ConnectorList) session.getAttribute(key);		
		List<RepositoryConnector> connectors =null;
		if(connectorList != null) {
		  connectors = connectorList.connectors;
		}
		if (connectors == null) {
			PluginFinder.registerServletContext(session.getServletContext());
			ConfigurationContainer container = loadUserConfiguration(currentUserId);			
			connectors = container.getConnectorList();
			connectorList = new ConnectorList();
			connectorList.connectors = connectors;
			session.setAttribute(key, connectorList);
		}
		return connectors;
	}

	/**
	 * Loads the configuration for this user. If no configuration exists, a demo
	 * configuration is created and stored in the database.
	 * 
	 * @param currentUserId
	 *            the id of the currently logged in user
	 */
	private static ConfigurationContainer loadUserConfiguration(String currentUserId) {
		PluginFinder.checkPluginInitialization();
		CycleConfigurationService configService = new CycleConfigurationServiceImpl(null);
		ConfigurationContainer configuration;
		try {
			configuration = configService.getConfiguration(currentUserId);
		} catch (RepositoryException e) {
			configuration = createDefaultDemoConfiguration(currentUserId);
			configService.saveConfiguration(configuration);
		}
		return configuration;
	}

	private static ConfigurationContainer createDefaultDemoConfiguration(String currentUserId) {
		ConfigurationContainer configuration = new ConfigurationContainer(currentUserId);
		configuration.addRepositoryConnectorConfiguration(new DemoConnectorConfiguration("demo"));
		configuration.addRepositoryConnectorConfiguration(new SignavioConnectorConfiguration("signavio",
				"http://localhost:8080/activiti-modeler/"));
		configuration.addRepositoryConnectorConfiguration(new FileSystemConnectorConfiguration("files", File.listRoots()[0]));
		return configuration;
	}

	// implementation of CycleService methods

	public boolean login(String username, String password, String connectorId) {
		RepositoryConnector conn = getRepositoryConnector(connectorId);
		if (conn != null) {
			conn.login(username, password);
			return true;
		}
		return false;
	}

	/**
	 * commit pending changes in all repository connectors configured
	 */
	public void commitPendingChanges(String comment) {
		for (RepositoryConnector connector : this.repositoryConnectors) {
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
		for (RepositoryConnector connector : this.repositoryConnectors) {

			RepositoryFolderImpl folder = new RepositoryFolderImpl(connector.getConfiguration().getId(), "/");
			folder.getMetadata().setName(connector.getConfiguration().getName());
			folder.getMetadata().setParentFolderId("/");
			nodes.add(folder);

		}
		return new RepositoryNodeCollectionImpl(nodes);
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

	public RepositoryArtifact createArtifact(String connectorId, String parentFolderId, String artifactName, String artifactType,
			Content artifactContent) throws RepositoryNodeNotFoundException {
		return getRepositoryConnector(connectorId).createArtifact(parentFolderId, artifactName, artifactType, artifactContent);
	}

	public RepositoryArtifact createArtifactFromContentRepresentation(String connectorId, String parentFolderId, String artifactName,
			String artifactType, String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException {
		return getRepositoryConnector(connectorId).createArtifactFromContentRepresentation(parentFolderId, artifactName, artifactType,
				contentRepresentationName, artifactContent);
	}

	public void updateContent(String connectorId, String artifactId, Content content) throws RepositoryNodeNotFoundException {
		RepositoryConnector connector = getRepositoryConnector(connectorId);
		connector.updateContent(artifactId, content);
	}

	public void updateContent(String connectorId, String artifactId, String contentRepresentationName, Content content)
			throws RepositoryNodeNotFoundException {
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

	public Content getContent(String connectorId, String artifactId, String representationName) throws RepositoryNodeNotFoundException {
		return getRepositoryConnector(connectorId).getContent(artifactId, representationName);
	}

	public void executeParameterizedAction(String connectorId, String artifactId, String actionId, Map<String, Object> parameters)
			throws Exception {
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

	public List<ArtifactType> getSupportedArtifactTypes(String connectorId, String folderId) {
		if (folderId == null || folderId.length() <= 1) {
			// "virtual" root folder doesn't support any artifact types
			return new ArrayList<ArtifactType>();
		}
		return getRepositoryConnector(connectorId).getSupportedArtifactTypes(folderId);
	}

	// RepositoryArtifactLink specific methods

	public void addArtifactLink(RepositoryArtifactLink repositoryArtifactLink) {
		if (repositoryArtifactLink instanceof RepositoryArtifactLinkEntity) {
			cycleDAO.insertArtifactLink((RepositoryArtifactLinkEntity) repositoryArtifactLink);
		} else {
			RepositoryArtifactLinkEntity cycleLink = new RepositoryArtifactLinkEntity();

			cycleLink.setId(repositoryArtifactLink.getId());

			// set source artifact attributes
			cycleLink.setSourceConnectorId(repositoryArtifactLink.getSourceArtifact().getConnectorId());
			cycleLink.setSourceArtifactId(repositoryArtifactLink.getSourceArtifact().getNodeId());
			cycleLink.setSourceElementId(repositoryArtifactLink.getSourceElementId());
			cycleLink.setSourceElementName(repositoryArtifactLink.getSourceElementName());
			cycleLink.setSourceRevision(repositoryArtifactLink.getSourceArtifact().getArtifactType().getRevision());

			// set target artifact attributes
			cycleLink.setTargetConnectorId(repositoryArtifactLink.getTargetArtifact().getConnectorId());
			cycleLink.setTargetArtifactId(repositoryArtifactLink.getTargetArtifact().getNodeId());
			cycleLink.setTargetElementId(repositoryArtifactLink.getTargetElementId());
			cycleLink.setTargetElementName(repositoryArtifactLink.getTargetElementName());
			cycleLink.setTargetRevision(repositoryArtifactLink.getTargetArtifact().getArtifactType().getRevision());

			cycleLink.setLinkType(repositoryArtifactLink.getLinkType());
			cycleLink.setComment(repositoryArtifactLink.getComment());
			cycleLink.setLinkedBothWays(false);

			cycleDAO.insertArtifactLink(cycleLink);
		}
	}

	public List<RepositoryArtifactLink> getArtifactLinks(String sourceConnectorId, String sourceArtifactId) {
		List<RepositoryArtifactLink> artifactLinks = new ArrayList<RepositoryArtifactLink>();

		List<RepositoryArtifactLinkEntity> linkResultList = cycleDAO.getOutgoingArtifactLinks(sourceConnectorId, sourceArtifactId);
		for (RepositoryArtifactLinkEntity entity : linkResultList) {
			entity.resolveArtifacts(this);
			artifactLinks.add(entity);
		}

		return artifactLinks;
	}

	public List<RepositoryArtifactLink> getIncomingArtifactLinks(String targetConnectorId, String targetArtifactId) {
	  List<RepositoryArtifactLink> artifactLinks = new ArrayList<RepositoryArtifactLink>();
    List<RepositoryArtifactLinkEntity> linkResultList = this.cycleDAO.getIncomingArtifactLinks(targetConnectorId, targetArtifactId);
    for (RepositoryArtifactLinkEntity entity : linkResultList) {
      entity.resolveArtifacts(this);
      artifactLinks.add(entity);
    }
    return artifactLinks;
	}

	public void deleteLink(String linkId) {
		cycleDAO.deleteArtifactLink(linkId);
	}


  public void addTag(String connectorId, String artifactId, String tagName, String alias) {
    RepositoryNodeTagEntity tagEntity = new RepositoryNodeTagEntity(tagName, connectorId, artifactId);
    tagEntity.setAlias(alias);
    cycleDAO.insertTag(tagEntity);
  }

  public void setTags(String connectorId, String nodeId, List<String> tags) {
    // TODO: Improve method to really just update changes!
    List<RepositoryNodeTagEntity> tagsForNode = cycleDAO.getTagsForNode(connectorId, nodeId);
    for (RepositoryNodeTagEntity tagEntity : tagsForNode) {
      cycleDAO.deleteTag(connectorId, nodeId, tagEntity.getName());
    }

    HashSet<String> alreadyAddedTags = new HashSet<String>();
    for (String tag : tags) {
      // TODO: Add Alias to setTags method as soon as we have a UI concept for
      // it
      if (tag != null) {
        tag = tag.trim();
      }
      if (!alreadyAddedTags.contains(tag) && tag.length() > 0) {
        addTag(connectorId, nodeId, tag, null);
        alreadyAddedTags.add(tag);
      }
    }
  }

  public List<RepositoryNodeTag> getRepositoryNodeTags(String connectorId, String nodeId) {
    ArrayList<RepositoryNodeTag> list = new ArrayList<RepositoryNodeTag>();
    list.addAll(cycleDAO.getTagsForNode(connectorId, nodeId));
    return list;
  }

  public List<String> getTags(String connectorId, String nodeId) {
    ArrayList<String> result = new ArrayList<String>();
    List<RepositoryNodeTagEntity> tagsForNode = cycleDAO.getTagsForNode(connectorId, nodeId);
    for (RepositoryNodeTagEntity tagEntity : tagsForNode) {
      result.add(tagEntity.getName());
    }
    return result;
  }

  public List<String> getSimiliarTagNames(String tagNamePattern) {
    return cycleDAO.getSimiliarTagNames(tagNamePattern);
  }

  public void deleteTag(String connectorId, String artifactId, String tagName) {
    cycleDAO.deleteTag(connectorId, artifactId, tagName);
  }

  public List<CycleTagContent> getRootTags() {
    ArrayList<CycleTagContent> result = new ArrayList<CycleTagContent>();
    result.addAll(cycleDAO.getTagsGroupedByName());
    return result;
  }

  public CycleTagContent getTagContent(String name) {
    CycleTagContentImpl tagContent = cycleDAO.getTagContent(name);
    tagContent.resolveRepositoryArtifacts(this);
    return tagContent;
  }

	private RepositoryConnector getRepositoryConnector(String connectorId) {
		for (RepositoryConnector connector : this.repositoryConnectors) {
			if (connector.getConfiguration().getId().equals(connectorId)) {
				return connector;
			}
		}
		throw new RepositoryException("Couldn't find Repository Connector with id '" + connectorId + "'");
	}


}