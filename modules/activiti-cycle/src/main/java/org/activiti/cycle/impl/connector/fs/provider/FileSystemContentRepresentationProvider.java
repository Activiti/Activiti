package org.activiti.cycle.impl.connector.fs.provider;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.activiti.cycle.ContentRepresentationProvider;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.impl.connector.fs.FileSystemConnector;

public abstract class FileSystemContentRepresentationProvider extends ContentRepresentationProvider {

  public FileSystemContentRepresentationProvider(String contentRepresentationName, String contentRepresentationType, boolean downloadable) {
    super(contentRepresentationName, contentRepresentationType, downloadable);
  }

  public FileSystemConnector getConnector(RepositoryArtifact artifact) {
    return (FileSystemConnector) artifact.getOriginalConnector();
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " [" + getContentRepresentationName() + "]";
  }

  public String getXmlAsString(DOMSource xmlData) throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException,
          IOException {
    StringWriter stringWriter = new StringWriter();
    StreamResult streamResult = new StreamResult(stringWriter);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.transform(xmlData, streamResult);

    return stringWriter.toString();
  }
}
