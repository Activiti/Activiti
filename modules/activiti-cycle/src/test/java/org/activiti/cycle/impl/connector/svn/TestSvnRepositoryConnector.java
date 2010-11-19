package org.activiti.cycle.impl.connector.svn;

import java.util.UUID;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.plugin.PluginFinder;
import org.activiti.cycle.incubator.connector.svn.SvnConnectorConfiguration;
import org.activiti.cycle.incubator.connector.svn.SvnRepositoryConnector;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

/**
 * WARNING: this Test is not usable without my local repo :)
 * 
 * @author daniel.meyer@camunda.com
 * 
 */
public class TestSvnRepositoryConnector {

	// public final static String REPO_LOCATION =
	// "http://svn.codehaus.org/activiti/";
	public final static String REPO_LOCATION = "file:///home/meyerd/tmp/svn-tmp/";

	private static ConfigurationContainer userConfiguration;

	private static SvnRepositoryConnector connector;

	@BeforeClass
	public static void createConnector() {
		userConfiguration = new ConfigurationContainer("daniel");
		userConfiguration.addRepositoryConnectorConfiguration(new SvnConnectorConfiguration("svn", REPO_LOCATION, "/tmp"));
		connector = (SvnRepositoryConnector) userConfiguration.getConnector("svn");

		// TODO: Should be done in Bootstrapping
		PluginFinder.checkPluginInitialization();

		// connector.login("guest", "");
	}

	@Test
	public void testGetChildrenRoot() {
		RepositoryNodeCollection result = connector.getChildren("");
		Assert.assertTrue(result.asList().size() > 0);
		for (RepositoryNode node : result.asList()) {
			System.out.println(node.getNodeId());
		}

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

	@Test
	public void testCreateArtifact() {
		RepositoryArtifact artifact = connector.getRepositoryArtifact("test.txt");

		Content content = connector.getContent(artifact.getNodeId(), "Text");

		RepositoryArtifact newArtifact = connector.createArtifact("", UUID.randomUUID() + ".txt", "Text", content);

	}

	@Test
	public void testTransactionalCreateArtifact() {
		
		RepositoryArtifact artifact = connector.getRepositoryArtifact("test.txt");

		Content content = connector.getContent(artifact.getNodeId(), "Text");

		connector.beginTransaction("//", "begin transaction on repository root", false);

		RepositoryFolder folder = connector.createFolder("//", UUID.randomUUID().toString());

		RepositoryFolder folder2 = connector.createFolder(folder.getNodeId(), UUID.randomUUID().toString());
		System.out.println(folder2);

		String filename = UUID.randomUUID() + ".txt";
		RepositoryArtifact newArtifact = connector.createArtifact(folder2.getNodeId(), filename, "Text", content);
		Assert.assertNotNull(newArtifact);

		RepositoryNodeCollection nodeCollection = connector.getChildren(folder2.getNodeId());
		Assert.assertTrue(nodeCollection.containsArtifact(newArtifact.getGlobalUniqueId()));

		connector.commitPendingChanges("");

		nodeCollection = connector.getChildren(folder2.getNodeId());
		Assert.assertTrue(nodeCollection.containsArtifact(newArtifact.getGlobalUniqueId()));

	}

	@Test
	public void testCreateFolder() {
		RepositoryFolder folder = connector.createFolder("", UUID.randomUUID().toString());
	}

	@Test
	public void testUpdateContent() {
		RepositoryArtifact test1 = connector.getRepositoryArtifact("test.txt");
		RepositoryArtifact test2 = connector.getRepositoryArtifact("test2.txt");

		Content contentTest1 = connector.getContent(test1.getNodeId(), "Text");
		Content contentTest2 = connector.getContent(test2.getNodeId(), "Text");

		connector.updateContent(test1.getNodeId(), contentTest2);
		connector.updateContent(test2.getNodeId(), contentTest1);
	}

	@Test
	public void testDeleteArtifact() {
		connector.deleteArtifact("test_delete.txt");
	}

	@Test
	public void testDeleteFolder() {
		connector.deleteFolder("trunk");
	}

	@Test
	public void testTransaction() {

		connector.beginTransaction("", "begin transaction on repository root", false);

		RepositoryArtifact test1 = connector.getRepositoryArtifact("test.txt");
		RepositoryArtifact test2 = connector.getRepositoryArtifact("test2.txt");

		Content contentTest1 = connector.getContent(test1.getNodeId(), "Text");
		Content contentTest2 = connector.getContent(test2.getNodeId(), "Text");

		connector.updateContent(test1.getNodeId(), contentTest2);
		connector.updateContent(test2.getNodeId(), contentTest1);

		connector.commitPendingChanges("commit");
	}

}
