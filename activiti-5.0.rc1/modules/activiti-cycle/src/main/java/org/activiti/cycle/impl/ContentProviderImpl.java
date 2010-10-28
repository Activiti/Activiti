package org.activiti.cycle.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentProvider;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.restlet.ext.xml.DomRepresentation;

/**
 * 
 * @author bernd.ruecker
 */
public abstract class ContentProviderImpl implements ContentProvider {

  protected Logger log = Logger.getLogger(this.getClass().getName());

  public abstract void addValueToContent(Content content, RepositoryConnector connector, RepositoryArtifact artifact);

  public Content createContent(RepositoryConnector connector, RepositoryArtifact artifact) {
    Content c = new Content();

    addValueToContent(c, connector, artifact);
    if (c.isNull()) {
      throw new RepositoryException("No content created for artifact " + artifact.getGlobalUniqueId() + " ' by provider '" + this.getClass().getName()
              + "' (was null). Please check provider or artifact.");
    }

    return c;
  }
  
  /**
   * helper method to transform XML to String if required by any
   * {@link ContentProvider}
   */
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
  
  public String getXmlAsString(DomRepresentation xmlData) throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException,
          IOException {
    return getXmlAsString(xmlData.getDomSource());
  }  
  
}
