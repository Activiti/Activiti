package org.activiti.cycle.impl.connector.svn;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.plugin.PluginFinder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

/**
 * Assert that we can login to the activiti public repository
 * 
 * @author daniel.meyer@camunda.com
 * 
 */
public class TestSVNRepositoryConnector {

	public final static String REPO_LOCATION = "http://svn.codehaus.org/activiti/";

	private static ConfigurationContainer userConfiguration;

	private static RepositoryConnector connector;

	@BeforeClass
	public static void createConnector() {
		userConfiguration = new ConfigurationContainer("daniel");
		userConfiguration.addRepositoryConnectorConfiguration(new SvnConnectorConfiguration("svn", REPO_LOCATION));
		connector = userConfiguration.getConnector("svn");

		// TODO: Should be done in Bootstrapping
		PluginFinder.checkPluginInitialization();

		connector.login("guest", "");
	}

	@Test
	public void testGetChildrenRoot() {
		RepositoryNodeCollection result = connector.getChildren("");
		Assert.assertTrue(result.asList().size() > 0);

		try {
			result = connector.getChildren("nonExistentPath");
			Assert.fail();
		} catch (RepositoryNodeNotFoundException e) {
			// this should happen
		}

	}

	@Test
	public void testGetArtifact() {
		RepositoryArtifact artifact = connector.getRepositoryArtifact("activiti/trunk/pom.xml");
		Assert.assertEquals("pom.xml", artifact.getMetadata().getName());
		try {
			connector.getRepositoryArtifact("nonExistentArtifact");
			Assert.fail();
		} catch (RepositoryNodeNotFoundException e) {
			// this should happen
		}
	}

	@Test
	public void testGetFolder() {
		RepositoryFolder folder = connector.getRepositoryFolder("activiti/trunk");
		Assert.assertEquals("trunk", folder.getMetadata().getName());
		try {
			connector.getRepositoryArtifact("nonExistentFolder");
			Assert.fail();
		} catch (RepositoryNodeNotFoundException e) {
			// this should happen
		}
	}

	@Test
	public void testGetXmlContent() {
		RepositoryArtifact artifact = connector.getRepositoryArtifact("activiti/trunk/pom.xml");

		Content content = connector.getContent(artifact.getNodeId(), artifact.getArtifactType().getContentRepresentations().get(0).getId());
		Assert.assertTrue(content.asByteArray().length > 0);
	}

}
