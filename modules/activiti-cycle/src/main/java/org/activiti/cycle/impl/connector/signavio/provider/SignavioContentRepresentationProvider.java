package org.activiti.cycle.impl.connector.signavio.provider;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;

import org.activiti.cycle.ContentRepresentationProvider;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;

public abstract class SignavioContentRepresentationProvider extends ContentRepresentationProvider {

  protected Logger log = Logger.getLogger(this.getClass().getName());

  public SignavioContentRepresentationProvider(String name, String type) {
    super(name, type);
  }

  public SignavioConnector getConnector(RepositoryArtifact artifact) {
    return (SignavioConnector) artifact.getConnector();
  }
  
  public Response getJsonResponse(RepositoryArtifact artifact, String urlSuffix) {
    SignavioConnector connector = getConnector(artifact);
    Client client = connector.initClient();

    Reference jsonRef = new Reference(connector.getSignavioConfiguration().getModelUrl() + artifact.getId() + urlSuffix);

    Request jsonRequest = new Request(Method.GET, jsonRef);
    jsonRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

    Form requestHeaders = new Form();
    requestHeaders.add("token", connector.getSecurityToken());
    jsonRequest.getAttributes().put("org.restlet.http.headers", requestHeaders);

    return client.handle(jsonRequest);
  }

  public String getXmlAsString(DomRepresentation xmlData) throws TransformerFactoryConfigurationError, TransformerConfigurationException,
          TransformerException, IOException {
    StringWriter stringWriter = new StringWriter();
    StreamResult streamResult = new StreamResult(stringWriter);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.transform(xmlData.getDomSource(), streamResult);

    return stringWriter.toString();
  }
}
