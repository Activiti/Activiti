package org.activiti.cycle.impl.connector.fs.provider;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.w3c.dom.Document;

public class FileSystemXmlProvider extends FileSystemContentRepresentationProvider {

  public static final String NAME = "Xml";

  public FileSystemXmlProvider() {
    super(NAME, ContentType.XML, true);
  }

  @Override
  public void addValueToContent(Content content, RepositoryArtifact artifact) {
    File file = new File(getConnector(artifact).getConfiguration().getBasePath() + artifact.getId());

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
