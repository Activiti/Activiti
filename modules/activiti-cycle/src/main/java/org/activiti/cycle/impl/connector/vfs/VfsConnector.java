package org.activiti.cycle.impl.connector.vfs;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.impl.RepositoryArtifactImpl;
import org.activiti.cycle.impl.RepositoryFolderImpl;
import org.activiti.cycle.impl.RepositoryNodeCollectionImpl;
import org.activiti.cycle.impl.connector.AbstractRepositoryConnector;
import org.activiti.cycle.impl.connector.util.ConnectorPathUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;

public abstract class VfsConnector extends AbstractRepositoryConnector<VfsConnectorConfiguration> {

	private static Logger log = Logger.getLogger(VfsConnector.class.getName());

	protected FileSystemManager fileSystemManager;

	protected String connectionString;

	protected FileSystemOptions fileSystemOptions;

	public VfsConnector(VfsConnectorConfiguration configuration) {
		super(configuration);
		validateConfiguration();
	}

	public void commitPendingChanges(String comment) {
		// do nothing, this connector is not transactional
	}

	public RepositoryArtifact getRepositoryArtifact(String id) throws RepositoryNodeNotFoundException {
		String filename = buildFilename(id);
		FileObject fileObject = null;
		RepositoryArtifact artifact = null;

		try {
			fileObject = fileSystemManager.resolveFile(filename);
			artifact = initRepositoryArtifact(fileObject, id);
			if (artifact == null)
				throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, id);
		} catch (FileSystemException e) {
			log.log(Level.WARNING, "cannot get artifact with id  " + id, e);
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, id, e);
		} finally {
			close(fileObject);
		}

		return artifact;
	}

	public RepositoryFolder getRepositoryFolder(String id) throws RepositoryNodeNotFoundException {
		String filename = buildFilename(id);
		FileObject fileObject = null;
		RepositoryFolder artifact = null;

		try {
			fileObject = fileSystemManager.resolveFile(filename);
			artifact = initRepositoryFolder(fileObject, id);
			if (artifact == null)
				throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id);
		} catch (FileSystemException e) {
			log.log(Level.WARNING, "cannot get artifact with id  " + id, e);
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id, e);
		} finally {
			close(fileObject);
		}

		return artifact;
	}

	public RepositoryNodeCollection getChildren(String id) throws RepositoryNodeNotFoundException {
		checkRepository();

		String filename = buildFilename(id);
		FileObject fileObject = null;
		FileObject[] children;
		List<RepositoryNode> result = new ArrayList<RepositoryNode>();

		try {
			fileObject = fileSystemManager.resolveFile(filename);
			children = fileObject.getChildren();
		} catch (FileSystemException e) {
			log.log(Level.WARNING, "cannot get Children of  " + id, e);
			throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id, e);
		} finally {
			close(fileObject);
		}

		for (FileObject child : children) {
			String newId = ConnectorPathUtils.buildId(id, child.getName().getBaseName());
			RepositoryNode node = initRepositoryNode(child, newId);
			if (node == null)
				continue;
			result.add(node);
			close(child);
		}

		return new RepositoryNodeCollectionImpl(result);
	}

	public RepositoryArtifact createArtifact(String parentFolderId, String artifactName, String artifactType, Content artifactContent)
			throws RepositoryNodeNotFoundException {
		throw new UnsupportedOperationException("Create artifact not yet implemented");
	}

	public RepositoryArtifact createArtifactFromContentRepresentation(String parentFolderId, String artifactName, String artifactType,
			String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException {
		throw new UnsupportedOperationException("Create artifact not yet implemented");
	}

	public RepositoryFolder createFolder(String parentFolderId, String name) throws RepositoryNodeNotFoundException {
		throw new UnsupportedOperationException("Create folder not yet implemented");
	}

	public void updateContent(String artifactId, Content content) throws RepositoryNodeNotFoundException {
		throw new UnsupportedOperationException("Update content not yet implemented");

	}

	public void updateContent(String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException {
		throw new UnsupportedOperationException("Update content not yet implemented");
	}

	public void deleteArtifact(String artifactId) throws RepositoryNodeNotFoundException {
		throw new UnsupportedOperationException("Delete artifact not yet implemented");
	}

	public void deleteFolder(String folderId) throws RepositoryNodeNotFoundException {
		throw new UnsupportedOperationException("deleteFolder not yet implemented");
	}

	protected RepositoryNode initRepositoryNode(FileObject child, String id) {
		FileType type = null;
		try {
			type = child.getType();
		} catch (FileSystemException e) {
			log.log(Level.WARNING, "Error while determining type of " + child.getName(), e);
			return null;
		}

		if (FileType.FOLDER.equals(type)) {
			return initRepositoryFolder(child, id);
		} else if (FileType.FILE.equals(type)) {
			return initRepositoryArtifact(child, id);
		}

		// TODO what about other types?
		log.info("could not determine whether " + child.getName() + " is a File or a directory.");
		return null;

	}

	protected RepositoryArtifact initRepositoryArtifact(FileObject child, String id) {
		String name = child.getName().getBaseName();
		if (name.equals(".") || name.equals(".."))
			return null;

		ArtifactType type = ConnectorPathUtils.getMimeType(child.getName().getBaseName(), getConfiguration());
		if (type == null)
			return null;

		RepositoryArtifact newArtifact = new RepositoryArtifactImpl(getConfiguration().getId(), id, type, this);
		newArtifact.getMetadata().setName(name);
		return newArtifact;
	}

	protected RepositoryFolder initRepositoryFolder(FileObject child, String id) {
		String name = child.getName().getBaseName();
		if (name.equals(".") || name.equals(".."))
			return null;
		RepositoryFolderImpl newFolder = new RepositoryFolderImpl(getConfiguration().getId(), id);
		newFolder.getMetadata().setName(name);
		return newFolder;
	}

	public String buildFilename(String id) {
		return ConnectorPathUtils.buildId(connectionString, id);
	}

	private void checkRepository() {
		getFileSystemManager();
		if (connectionString == null)
			throw new RepositoryException("You need to login first.");

	}

	protected void close(FileObject fileObject) {
		try {
			if (fileObject != null)
				fileObject.close();
		} catch (FileSystemException e) {
			log.log(Level.WARNING, "cannot close  " + fileObject.getName(), e);
		}
	}

	protected abstract void validateConfiguration();

	public abstract FileSystemManager getFileSystemManager();
}
