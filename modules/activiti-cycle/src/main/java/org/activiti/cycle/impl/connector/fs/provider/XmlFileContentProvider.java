package org.activiti.cycle.impl.connector.fs.provider;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.ContentProviderImpl;
import org.activiti.cycle.impl.connector.fs.FileSystemConnector;
import org.w3c.dom.Document;

public class XmlFileContentProvider extends ContentProviderImpl {

  @Override
  public void addValueToContent(Content content, RepositoryConnector connector, RepositoryArtifact artifact) {
    String fileName = ((FileSystemConnector) connector).getConfiguration().getBasePath() + artifact.getOriginalNodeId();
    File file = new File(fileName);

    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db;
      db = dbf.newDocumentBuilder();
      Document doc = db.parse(file);

      content.setValue(getXmlAsString(new DOMSource(doc)));
    } catch (Exception e) {
      throw new RepositoryException("Error while retrieving artifact " + artifact + " as xml content", e);
    }
  }
}
