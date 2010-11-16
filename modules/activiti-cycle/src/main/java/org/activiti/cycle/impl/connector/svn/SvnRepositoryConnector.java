package org.activiti.cycle.impl.connector.svn;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
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
 * The {@link RepositoryConnector} for SVN repositories.
 * 
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

	private SVNUrl lockedURL;

	private Map<String, File> dirtyFolders = new HashMap<String, File>();

	private boolean transactionActive = false;

	private boolean autocommit = false;

	private final Object transaction_lock = new Object();

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

		synchronized (transaction_lock) {
			if (!transactionActive) {
				return;
			}
		}

		ISVNClientAdapter clientAdapter = getSvnClientAdapter();

		try {
			clientAdapter.commit(dirtyFolders.values().toArray(new File[0]), comment, true);

			// TODO: unlock?

			synchronized (transaction_lock) {
				transactionActive = false;
			}

		} catch (SVNClientException e) {
			log.log(Level.SEVERE, "Could not commit changes in " + dirtyFolders.values().toArray(new File[0]) + " " + e.getMessage());
			cancelTransaction();
			throw new RepositoryException("Could not commit changes in " + dirtyFolders.values().toArray(new File[0]), e);
		} finally {
			// TODO: do this elsewhere?
			for (File dirtyFolder : dirtyFolders.values()) {
				recDelete(dirtyFolder);
			}
			dirtyFolders.clear();
		}

	}

	public RepositoryArtifact getRepositoryArtifact(String id) throws RepositoryNodeNotFoundException {
		ISVNClientAdapter clientAdapter = getSvnClientAdapter();

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

	public RepositoryArtifact createArtifact(String parentFolderId, String artifactName, String artifactType, Content artifactContent)
			throws RepositoryNodeNotFoundException {
		ISVNClientAdapter clientAdapter = getSvnClientAdapter();
		File temporaryFileStrore = null;
		// acquire lock
		beginTransaction(parentFolderId, "Temporarily locked by Activiti-Cycle for transactional commit on " + new Date() + ".",
				!transactionActive);
		try {
			// checkout parent folder to temporary filestore
			temporaryFileStrore = temporaryCheckout(parentFolderId);

			// create file
			File newFile = new File(temporaryFileStrore + File.separator + artifactName);
			createFile(newFile);
			writeToFile(newFile, artifactContent);

			// add new file
			clientAdapter.addFile(newFile);

			// commit changes
			if (this.autocommit) {
				commitPendingChanges("Activiti-Cycle created file " + artifactName + " in " + parentFolderId);
				// FIXME: can only return artifact if we commit
				return getRepositoryArtifact(buildId(parentFolderId, artifactName));
			}

			return null;

		} catch (Exception e) {
			log.log(Level.WARNING, "Unable to create" + artifactName + " in " + parentFolderId, e);
			cancelTransaction();
			throw new RepositoryException("Unable to create " + artifactName + " in " + parentFolderId + " " + e.getMessage(), e);
		}

	}

	public RepositoryArtifact createArtifactFromContentRepresentation(String parentFolderId, String artifactName, String artifactType,
			String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException {
		return createArtifact(parentFolderId, artifactName, artifactType, artifactContent);

	}

	public RepositoryFolder createFolder(String parentFolderId, String name) throws RepositoryNodeNotFoundException {
		ISVNClientAdapter clientAdapter = getSvnClientAdapter();
		File temporaryFileStrore = null;
		// acquire lock
		beginTransaction(parentFolderId, "Temporarily locked by Activiti-Cycle for transactional commit on " + new Date() + ".",
				!transactionActive);
		try {
			// checkout parent folder to temporary filestore
			temporaryFileStrore = temporaryCheckout(parentFolderId);

			// create folder
			File newDirectory = new File(temporaryFileStrore + File.separator + name);
			createDirectory(newDirectory);

			// add new folder
			clientAdapter.addFile(newDirectory);

			// commit changes
			if (this.autocommit) {
				commitPendingChanges("Activiti-Cycle created folder " + name + " in " + parentFolderId);
				// FIXME: can only return folder if we commit
				return getRepositoryFolder(buildId(parentFolderId, name));
			}

			return null;

		} catch (Exception e) {
			String error = "Error while creating folder" + name + " in " + parentFolderId;
			log.log(Level.WARNING, error, e);
			cancelTransaction();
			throw new RepositoryException(error, e);
		}
	}

	public void updateContent(String artifactId, Content content) throws RepositoryNodeNotFoundException {
		// assure that clientadapter is properly initialized
		getSvnClientAdapter();
		File temporaryFileStrore = null;

		// get parent folder
		String parentFolderId;
		String fileName;
		if (artifactId.contains("/")) {
			parentFolderId = artifactId.substring(0, artifactId.lastIndexOf("/"));
			fileName = artifactId.substring(artifactId.lastIndexOf("/") + 1);
		} else {
			parentFolderId = "";
			fileName = artifactId;
		}

		// acquire lock
		beginTransaction(parentFolderId, "Temporarily locked by Activiti-Cycle for transactional commit on " + new Date() + ".",
				!transactionActive);
		try {
			// checkout parent folder to temporary filestore
			temporaryFileStrore = temporaryCheckout(parentFolderId);

			File file = new File(temporaryFileStrore.getAbsolutePath() + File.separator + fileName);
			writeToFile(file, content);

			// commit changes
			if (this.autocommit) {
				commitPendingChanges("Activiti-Cycle updated file " + fileName + " in " + parentFolderId);
			}

		} catch (Exception e) {
			String error = "Error while updating file " + fileName + " in " + parentFolderId;
			log.log(Level.WARNING, error, e);
			cancelTransaction();
			throw new RepositoryException(error, e);
		}
	}

	public void updateContent(String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException {
		updateContent(artifactId, content);
	}

	public void deleteArtifact(String artifactId) throws RepositoryNodeNotFoundException {

		ISVNClientAdapter clientAdapter = getSvnClientAdapter();

		try {

			SVNUrl url = buildSVNURL(artifactId);

			clientAdapter.remove(new SVNUrl[] { url }, artifactId + "Removed by Activiti Cycle");

		} catch (Exception e) {
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, artifactId, e);
		}

	}

	public void deleteFolder(String folderId) throws RepositoryNodeNotFoundException {
		ISVNClientAdapter clientAdapter = getSvnClientAdapter();

		try {
			SVNUrl url = buildSVNURL(folderId);
			clientAdapter.remove(new SVNUrl[] { url }, folderId + "Removed by Activiti Cycle");
		} catch (Exception e) {
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, folderId, e);
		}
	}

	private String buildId(String... constituents) {
		String result = "";
		for (int i = 0; i < constituents.length; i++) {
			String constituent = constituents[i];
			if (i == 0) {
				result = constituents[i];
				continue;
			}
			if (!result.endsWith("/") && !constituent.startsWith("/") && constituent.length() > 0) {
				result += "/";
			}

			result += constituent;

		}
		return result;
	}

	public SVNUrl buildSVNURL(String id) throws MalformedURLException {
		String repositoryPath = getConfiguration().getRepositoryPath();
		return new SVNUrl(buildId(repositoryPath, id));
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

		// TODO: figure out a better way to do this
		// problem exists with extensions like .bpmn20.xml
		while (extension.contains(".")) {
			extension = extension.substring(path.indexOf(".") + 1);
			try {
				// throws exception if it cannot find an artifact type.
				artifactType = getConfiguration().getArtifactType(extension);
			} catch (Exception e) {
				// let the exception pass
			}
			if (artifactType != null) {
				break;
			}

		}
		if (artifactType == null) {
			return getConfiguration().getDefaultArtifactType();
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

	protected File temporaryCheckout(String folderId) throws RepositoryException {
		ISVNClientAdapter clientAdapter = getSvnClientAdapter();
		try {
			SVNUrl parentFolderUrl = buildSVNURL(folderId);

			// already checked out?
			File temporaryFileStrore = dirtyFolders.get(parentFolderUrl.toString());
			if (temporaryFileStrore != null)
				return temporaryFileStrore;

			temporaryFileStrore = new File(getConfiguration().getTemporaryFileStore() + File.separator + UUID.randomUUID());

			clientAdapter.checkout(parentFolderUrl, temporaryFileStrore, SVNRevision.HEAD, false);
			dirtyFolders.put(parentFolderUrl.toString(), temporaryFileStrore);
			return temporaryFileStrore;
		} catch (Exception e) {
			throw new RepositoryException("Could not checkout " + folderId + ".", e);
		}
	}

	private void writeToFile(File newFile, Content artifactContent) {
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(newFile));
			bos.write(artifactContent.asByteArray());
			bos.flush();

		} catch (IOException ex) {
			throw new RepositoryException("Error while writing content to " + newFile, ex);
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (Exception ex2) {
				}
			}
		}

	}

	private void createFile(File newFile) {
		try {
			newFile.createNewFile();
		} catch (IOException e) {
			log.log(Level.WARNING, "could not create file " + newFile, e);
		} finally {
			if (!newFile.exists())
				throw new RepositoryException("could not create file " + newFile);
		}
	}

	private void createDirectory(File newFolder) {
		try {
			newFolder.mkdir();
		} finally {
			if (!newFolder.exists())
				throw new RepositoryException("could not create folder " + newFolder);
		}
	}

	/**
	 * Recursively deletes a local directory
	 */
	private boolean recDelete(File file) {
		if (!file.exists())
			return false;
		if (file.isFile()) {
			return file.delete();
		}
		boolean result = true;
		File[] children = file.listFiles();
		for (int i = 0; i < children.length; i++) {
			result &= recDelete(children[i]);
		}
		result &= file.delete();
		return result;
	}

	public void cancelTransaction() {
		synchronized (transaction_lock) {
			ISVNClientAdapter clientAdapter = getSvnClientAdapter();

			try {
				clientAdapter.unlock(new SVNUrl[] { lockedURL }, true);
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error while unlocking " + lockedURL, e);
				// TODO: what now?
			}

			for (File dirtyFolder : dirtyFolders.values()) {
				recDelete(dirtyFolder);
			}

			dirtyFolders.clear();

			transactionActive = false;

		}
	}

	public void beginTransaction(String onFolderId, String lockComment, boolean autocommit) throws RepositoryNodeNotFoundException {

		synchronized (transaction_lock) {
			if (transactionActive == true) {
				return;
			}
			this.transactionActive = true;
			this.autocommit = autocommit;
		}

		ISVNClientAdapter clientAdapter = getSvnClientAdapter();

		try {
			SVNUrl url = buildSVNURL(onFolderId);
			// TODO: lock resource ?
			// clientAdapter.lock(new SVNUrl[] { url }, lockComment, false);
			lockedURL = url;

		} catch (Exception e) {
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, onFolderId, e);
		}
	}

	public ISVNClientAdapter getSvnClientAdapter() {
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
			throw new RepositoryException("Could not initialize client adapter. No client library available? " + e.getMessage());
		}
	}
}
