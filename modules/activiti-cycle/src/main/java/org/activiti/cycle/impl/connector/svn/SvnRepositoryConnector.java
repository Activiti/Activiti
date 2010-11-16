package org.activiti.cycle.impl.connector.svn;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory;

/**
 * The {@link RepositoryConnector} for SVN repositories. Use this Connector in a
 * stateless way for browsing and atomic commits. Call
 * {@link #beginTransaction(String, String, boolean)} to start longer
 * transactions (with batch-commits)
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

	private Map<String, File> dirtyFolders = new HashMap<String, File>();
	private Map<String, File> createdFolders = new HashMap<String, File>();

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
			throw new RuntimeException("Cannot initialize SvnRepositoryConnector: repositoryPath not set.");
		}
		String tempFilePath = getConfiguration().getTemporaryFileStore();
		if (tempFilePath == null) {
			throw new RuntimeException("Cannot initialize SvnRepositoryConnector: temporaryFileStore not set.");
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
			createdFolders.clear();
		}

	}

	public RepositoryArtifact getRepositoryArtifact(String id) throws RepositoryNodeNotFoundException {
		ISVNClientAdapter clientAdapter = getSvnClientAdapter();

		ISVNInfo entry = null;

		try {
			File dirtyFile = isDirty(id);
			if (dirtyFile != null) {
				entry = clientAdapter.getInfo(dirtyFile);
			} else {
				entry = clientAdapter.getInfo(buildSVNURL(id));
			}
			return (RepositoryArtifact) initRepositoryNode(entry, id);

		} catch (Exception e) {
			log.log(Level.WARNING, "cannot get Artifact " + id, e);
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, id, e);
		}

	}

	public RepositoryFolder getRepositoryFolder(String id) throws RepositoryNodeNotFoundException {
		ISVNClientAdapter clientAdapter = getSvnClientAdapter();

		ISVNInfo entry = null;

		try {
			File dirtyFolder = isDirty(id);
			if (dirtyFolder != null) {
				entry = clientAdapter.getInfo(dirtyFolder);
			} else {
				entry = clientAdapter.getInfo(buildSVNURL(id));
			}
			return (RepositoryFolder) initRepositoryNode(entry, id);
		} catch (Exception e) {
			log.log(Level.WARNING, "cannot get Folder " + id, e);
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id, e);
		}

	}

	public RepositoryNodeCollection getChildren(String id) throws RepositoryNodeNotFoundException {
		ISVNClientAdapter clientAdapter = getSvnClientAdapter();

		List<RepositoryNode> nodeList = new ArrayList<RepositoryNode>();

		try {
			File dirtyFolder = isDirty(id);
			if (dirtyFolder != null) {
				// get local list using traditional Java-file-API:
				File[] list = dirtyFolder.listFiles();
				for (File file : list) {
					if (file.isHidden()) {
						continue;
					}
					ISVNInfo info = clientAdapter.getInfo(file);
					String nodeId = buildId(id, info.getFile().getName());
					if (info != null) {
						RepositoryNode node = initRepositoryNode(info, nodeId);
						if (node != null) {
							nodeList.add(node);
						}
					}
				}
			} else {
				// get list from remote repository
				ISVNDirEntry[] dirEntries = clientAdapter.getList(buildSVNURL(id), SVNRevision.HEAD, false);
				for (ISVNDirEntry isvnDirEntry : dirEntries) {
					String nodeId = buildId(id, isvnDirEntry.getPath());
					// FIXME: this is expensive:
					ISVNInfo info = clientAdapter.getInfo(buildSVNURL(nodeId));
					RepositoryNode node = initRepositoryNode(info, nodeId);
					if (node != null) {
						nodeList.add(node);
					}
				}
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "cannot get children of " + id, e);
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id, e);
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
			}

			// FIXME: can only return artifact if we commit
			return getRepositoryArtifact(buildId(parentFolderId, artifactName));

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

			String newFolderId = buildId(parentFolderId, name);
			createdFolders.put(newFolderId, newDirectory);

			// commit changes
			if (this.autocommit) {
				commitPendingChanges("Activiti-Cycle created folder " + name + " in " + parentFolderId);
			}

			// FIXME: can only return folder if we commit
			return getRepositoryFolder(newFolderId);

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
			while (constituent.endsWith("//")) {
				constituent = constituent.substring(0, constituent.length() - 1);
			}
			while (constituent.startsWith("//")) {
				constituent = constituent.substring(1);
			}
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

	/**
	 * creates and initializes a new {@link RepositoryNode} for the given
	 * {@link ISVNDirEntry} and the given id. The id is the node-id of the node
	 * as returned by {@link RepositoryNode#getNodeId()}
	 * 
	 * @param info
	 * @param the
	 *            repositoryNodeId
	 * @return an initialized {@link RepositoryNode}
	 */
	protected RepositoryNode initRepositoryNode(ISVNInfo info, String id) {

		if (info.getNodeKind().equals(SVNNodeKind.DIR)) {
			return createFolderNode(info, id);
		}
		if (info.getNodeKind().equals(SVNNodeKind.FILE)) {
			return createArtifactNode(info, id);
		}
		return null;
		// TODO: other types ?
	}

	/**
	 * Check whether the {@link RepositoryNode} with the given url is dirty and
	 * if true returns the corresponding dirty {@link File}
	 * 
	 * @param id
	 *            the id of the {@link RepositoryNode}
	 * @return the {@link File} corresponding to the file in the local
	 *         repository
	 */
	protected File isDirty(String id) {
		// a node is dirty, if the parent folder is in the dirtyFolders-list or
		// if the
		// parent folder was created in this transaction (is in the
		// createdFolders list)
		try {
			String parentFolderId = id;
			if (parentFolderId.contains(("/"))) {
				parentFolderId = parentFolderId.substring(0, id.lastIndexOf("/"));
			}
			String filename = id;
			if (filename.contains(("/"))) {
				filename = filename.substring(id.lastIndexOf("/") + 1);
			}

			File parentFolder = createdFolders.get(parentFolderId);
			if (parentFolder != null)
				return new File(parentFolder.getAbsolutePath() + File.separator + filename);

			String url = buildSVNURL(parentFolderId).toString();
			for (String dirtyUrl : dirtyFolders.keySet()) {
				if (!url.equals(dirtyUrl)) {
					continue;
				}
				File dirtyFolder = dirtyFolders.get(dirtyUrl);
				return new File(dirtyFolder.getAbsolutePath() + File.separator + filename);

			}
			return null;
		} catch (Exception e) {
			return null;
		}

	}

	protected RepositoryNode createArtifactNode(ISVNInfo info, String id) {
		String filename = info.getFile().getName();

		// TODO: ignore hidden files?
		if (filename.startsWith(".")) {
			return null;
		}

		// get mimetype
		ArtifactType artifactType = getMimeType(filename);

		RepositoryArtifactImpl artifact = new RepositoryArtifactImpl(getConfiguration().getId(), id, artifactType, this);
		artifact.getMetadata().setName(filename);
		artifact.getMetadata().setLastAuthor(info.getLastCommitAuthor());
		if (info.getLastChangedRevision() != null) {
			artifact.getMetadata().setVersion(info.getLastChangedRevision().toString());
		}

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

	protected RepositoryNode createFolderNode(ISVNInfo info, String nodeId) {
		String filename = info.getFile().getName();

		RepositoryFolderImpl folder = new RepositoryFolderImpl(getConfiguration().getId(), nodeId);

		folder.getMetadata().setName(filename);
		folder.getMetadata().setLastAuthor(info.getLastCommitAuthor());
		if (info.getLastChangedRevision() != null) {
			folder.getMetadata().setVersion(info.getLastChangedRevision().toString());
		}
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
			temporaryFileStrore = createdFolders.get(folderId);
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

			// try {
			// clientAdapter.unlock(new SVNUrl[] { lockedURL }, true);
			// } catch (Exception e) {
			// log.log(Level.SEVERE, "Error while unlocking " + lockedURL, e);
			// // TODO: what now?
			// }

			for (File dirtyFolder : dirtyFolders.values()) {
				recDelete(dirtyFolder);
			}

			dirtyFolders.clear();
			createdFolders.clear();

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

		// try {
		// SVNUrl url = buildSVNURL(onFolderId);
		// // TODO: lock resource ?
		// clientAdapter.lock(new SVNUrl[] { url }, lockComment, false);
		// lockedURL = url;
		//
		// } catch (Exception e) {
		// throw new
		// RepositoryNodeNotFoundException(getConfiguration().getName(),
		// RepositoryFolder.class, onFolderId, e);
		// }
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
