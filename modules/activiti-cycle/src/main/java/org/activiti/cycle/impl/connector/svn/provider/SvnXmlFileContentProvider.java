package org.activiti.cycle.impl.connector.svn.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.w3c.dom.Document;

/**
 * 
 * @author daniel.meyer@camunda.com
 * 
 */
public class SvnXmlFileContentProvider extends SvnBinaryContentProvider {
	Logger log = Logger.getLogger(SvnBinaryContentProvider.class.getName());

	@Override
	public void addValueToContent(Content content, RepositoryConnector connector, RepositoryArtifact artifact) {
		// throws exception
		super.addValueToContent(content, connector, artifact);

		InputStream is = content.asInputStream();

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(is);
			// sets InputStream to 'null'
			content.setValue(getXmlAsString(new DOMSource(doc)));
		} catch (Exception e) {
			throw new RepositoryException("Error while retrieving artifact " + artifact + " as xml content", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// TODO: I think we can let this one pass, as everything worked
				// up to here... !?
				log.log(Level.WARNING, "Error while closing input stream while  retreiving content of " + artifact, e);

			}

		}

	}
}
