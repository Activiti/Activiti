package org.activiti.cycle.impl.connector.svn;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.impl.RepositoryArtifactImpl;
import org.activiti.cycle.impl.RepositoryFolderImpl;
import org.activiti.cycle.impl.RepositoryNodeCollectionImpl;
import org.activiti.cycle.impl.connector.AbstractRepositoryConnector;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory;

/**
 * The {@link RepositoryConnector} for SVN repositories
 * 
 * @author daniel.meyer@camunda.com
 * 
 */
/*
 * IMPLEMENTATION NOTE: we perform lazy-initialization of the actual client
 * adapter. Make sure to always call getSvnClientAdapter().
 */
public class SvnRepositoryConnector extends AbstractRepositoryConnector<SvnConnectorConfiguration> {

	private static Logger log = Logger.getLogger(SvnRepositoryConnector.class.getName());

	private ISVNClientAdapter svnClientAdapter;

	static {
		setupFactories();
	}

	public SvnRepositoryConnector(SvnConnectorConfiguration configuration) {
		super(configuration);
		validateConfig();
	}

	protected void validateConfig() {
		String repositoryPath = getConfiguration().getRepositoryPath();
		if (repositoryPath == null) {
			throw new RuntimeException("Cannot initialize SVNRepositoryConnector: repositoryPath not set.");
		}
	}

	protected static void setupFactories() {
		try {
			SvnKitClientAdapterFactory.setup();
		} catch (SVNClientException e) {
			log.log(Level.FINEST, "cannot initialize the SvnKitClientAdapterFactory.");
		}

		try {
			CmdLineClientAdapterFactory.setup();
		} catch (SVNClientException e) {
			log.log(Level.FINEST, "cannot initialize the CmdLineClientAdapterFactory.");
		}

		// try {
		// JhlClientAdapterFactory.setup();
		// } catch (SVNClientException e) {
		// log.log(Level.FINEST,
		// "cannot initialize the JhlClientAdapterFactory.");
		// }

	}

	public boolean login(String username, String password) {
		ISVNClientAdapter clientAdapter = getSvnClientAdapter();
		if (clientAdapter == null)
			return false;
		clientAdapter.setUsername(username);
		clientAdapter.setPassword(password);
		try {
			clientAdapter.getList(new SVNUrl(getConfiguration().getRepositoryPath()), SVNRevision.HEAD, false);
			return true;
		} catch (Exception e) {
			log.log(Level.WARNING, "cannot log into repository", e);
			return false;
		}
	}

	public void commitPendingChanges(String comment) {
		// TODO Auto-generated method stub

	}

	public RepositoryArtifact getRepositoryArtifact(String id) throws RepositoryNodeNotFoundException {
		ISVNClientAdapter clientAdapter = getSvnClientAdapter();
		if (clientAdapter == null)
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, id);

		ISVNDirEntry entry = null;

		try {
			entry = clientAdapter.getDirEntry(buildSVNURL(id), SVNRevision.HEAD);
			return (RepositoryArtifact) createRepositoryNode(entry, id);
		} catch (Exception e) {
			log.log(Level.WARNING, "cannot get Artifact " + id, e);
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, id, e);
		}

	}

	public RepositoryFolder getRepositoryFolder(String id) throws RepositoryNodeNotFoundException {
		ISVNClientAdapter clientAdapter = getSvnClientAdapter();
		if (clientAdapter == null)
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id);

		ISVNDirEntry entry = null;

		try {
			entry = clientAdapter.getDirEntry(buildSVNURL(id), SVNRevision.HEAD);
			return (RepositoryFolder) createRepositoryNode(entry, id);
		} catch (Exception e) {
			log.log(Level.WARNING, "cannot get Folder " + id, e);
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id, e);
		}

	}

	public RepositoryNodeCollection getChildren(String id) throws RepositoryNodeNotFoundException {
		ISVNClientAdapter clientAdapter = getSvnClientAdapter();
		if (clientAdapter == null)
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id);

		ISVNDirEntry[] dirEntries;

		try {
			dirEntries = clientAdapter.getList(buildSVNURL(id), SVNRevision.HEAD, false);
		} catch (Exception e) {
			log.log(Level.WARNING, "cannot get children of " + id, e);
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id, e);
		}

		List<RepositoryNode> nodeList = new ArrayList<RepositoryNode>();
		for (ISVNDirEntry isvnDirEntry : dirEntries) {
			RepositoryNode node = createRepositoryNode(isvnDirEntry, id);
			if (node != null) {
				nodeList.add(node);
			}
		}

		RepositoryNodeCollection result = new RepositoryNodeCollectionImpl(nodeList);
		return result;
	}

	public SVNUrl buildSVNURL(String id) throws MalformedURLException {
		String urlString = getConfiguration().getRepositoryPath();
		if (!urlString.endsWith("/")) {
			urlString += "/";
		}
		urlString += id;
		return new SVNUrl(urlString);
	}

	protected RepositoryNode createRepositoryNode(ISVNDirEntry isvnDirEntry, String id) {
		String nodeId = id;
		if (!id.endsWith(isvnDirEntry.getPath())) {
			nodeId += "/" + isvnDirEntry.getPath();
		}

		// TODO: other types ?

		if (isvnDirEntry.getNodeKind().equals(SVNNodeKind.DIR)) {
			return createFolderNode(isvnDirEntry, nodeId);
		}
		if (isvnDirEntry.getNodeKind().equals(SVNNodeKind.FILE)) {
			return createArtifactNode(isvnDirEntry, nodeId);
		}
		return null;
	}

	protected RepositoryNode createArtifactNode(ISVNDirEntry isvnDirEntry, String id) {
		String path = isvnDirEntry.getPath();

		// TODO: ignore hidden files:
		if (path.startsWith(".")) {
			return null;
		}

		// get mimetype
		ArtifactType artifactType = getMimeType(path);

		// find filename
		String filename = new File(path).getName();

		RepositoryArtifactImpl artifact = new RepositoryArtifactImpl(getConfiguration().getId(), id, artifactType, this);
		artifact.getMetadata().setName(filename);
		artifact.getMetadata().setLastAuthor(isvnDirEntry.getLastCommitAuthor());
		artifact.getMetadata().setVersion(isvnDirEntry.getLastChangedRevision().toString());

		return artifact;
	}

	protected ArtifactType getMimeType(String path) {
		// try to find artifact type
		ArtifactType artifactType = null;
		String extension = path;
		if (!extension.contains(".")) {
			return getConfiguration().getDefaultArtifactType();
		}
		while (artifactType == null || extension.contains(".")) {
			extension = path.substring(path.indexOf(".") + 1);
			artifactType = getConfiguration().getArtifactType(extension);
		}
		return artifactType;

	}

	protected RepositoryNode createFolderNode(ISVNDirEntry isvnDirEntry, String nodeId) {
		String path = isvnDirEntry.getPath();

		RepositoryFolderImpl folder = new RepositoryFolderImpl(getConfiguration().getId(), nodeId);

		// find filename
		String filename = new File(path).getName();

		folder.getMetadata().setName(filename);
		folder.getMetadata().setLastAuthor(isvnDirEntry.getLastCommitAuthor());
		folder.getMetadata().setVersion(isvnDirEntry.getLastChangedRevision().toString());
		return folder;
	}

	public RepositoryArtifact createArtifact(String parentFolderId, String artifactName, String artifactType, Content artifactContent)
			throws RepositoryNodeNotFoundException {
		throw new UnsupportedOperationException("createArtifact");
	}

	public RepositoryArtifact createArtifactFromContentRepresentation(String parentFolderId, String artifactName, String artifactType,
			String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException {
		throw new UnsupportedOperationException("createArtifactFromContentRepresentation");
	}

	public RepositoryFolder createFolder(String parentFolderId, String name) throws RepositoryNodeNotFoundException {
		throw new UnsupportedOperationException("createFolder");
	}

	public void updateContent(String artifactId, Content content) throws RepositoryNodeNotFoundException {
		throw new UnsupportedOperationException("updateContent");

	}

	public void updateContent(String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException {
		throw new UnsupportedOperationException("updateContent");

	}

	public void deleteArtifact(String artifactId) throws RepositoryNodeNotFoundException {
		throw new UnsupportedOperationException("deleteArtifact");

	}

	public void deleteFolder(String folderId) throws RepositoryNodeNotFoundException {
		throw new UnsupportedOperationException("deleteFolder");

	}

	public ISVNClientAdapter getSvnClientAdapter() {
		// TODO: do we have to worry about thread-safety?
		if (svnClientAdapter == null) {
			initClientAdapter();
		}
		return svnClientAdapter;
	}

	private void initClientAdapter() {
		try {
			String bestClientType = SVNClientAdapterFactory.getPreferredSVNClientType();
			log.fine("Using " + bestClientType + " factory");
			svnClientAdapter = SVNClientAdapterFactory.createSVNClient(bestClientType);
		} catch (SVNClientException e) {
			log.log(Level.SEVERE, "Cannot create an SVN Client. No client library installed? This activiti-cycle connector is not usable.",
					e);
			return;
		}
	}
}
