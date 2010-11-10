package org.activiti.cycle.impl.connector.svn.provider;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.ContentProviderImpl;
import org.activiti.cycle.impl.connector.svn.SvnRepositoryConnector;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * 
 * @author daniel.meyer@camunda.com
 * 
 */
public class SvnBinaryContentProvider extends ContentProviderImpl {
	public void addValueToContent(Content content, RepositoryConnector connector, RepositoryArtifact artifact) {

		SvnRepositoryConnector svnConnector = ((SvnRepositoryConnector) connector);
		ISVNClientAdapter adapter = svnConnector.getSvnClientAdapter();

		if (adapter == null) {
			throw new RepositoryException("Errror while retreiving content of " + artifact.getNodeId());
		}

		SVNUrl url;
		try {
			url = svnConnector.buildSVNURL(artifact.getNodeId());
		} catch (MalformedURLException e) {
			throw new RepositoryException("Error while retreiving content of " + artifact.getNodeId() + " malformed URL.", e);
		}

		InputStream is;
		try {
			is = adapter.getContent(url, SVNRevision.HEAD);
		} catch (SVNClientException e) {
			throw new RepositoryException("Error while retreiving content of " + artifact.getNodeId(), e);
		}

		// wrap in BufferedInputStream
		is = new BufferedInputStream(is);

		content.setValue(is);

	};
}
