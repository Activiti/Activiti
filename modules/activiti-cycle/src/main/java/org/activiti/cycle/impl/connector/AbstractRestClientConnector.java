package org.activiti.cycle.impl.connector;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.conf.PasswordEnabledRepositoryConnectorConfiguration;
import org.activiti.cycle.impl.connector.util.RestClientLogHelper;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;


public abstract class AbstractRestClientConnector<T extends PasswordEnabledRepositoryConnectorConfiguration> extends AbstractRepositoryConnector<T> {

  protected Map<String, Object> properties;
  protected transient Client restletClient;
  protected Context context;

  public AbstractRestClientConnector(T configuration) {
    super(configuration);
  }

  public Client initClient() {
    // TODO: Check timeout on client and re-create it

    if (restletClient == null) {
      // TODO: check for additional options
      // Create and initialize HTTP client for HTTP REST API calls
      context.getParameters().set("maxConnectionsPerHost", "20");
      restletClient = new Client(context, Protocol.HTTP);
    }
    return restletClient;
  }
  
  public Response sendRequest(Request request) throws IOException {
    injectSecurityMechanism(request);

    if (log.isLoggable(Level.FINE)) {
      RestClientLogHelper.logHttpRequest(log, Level.FINE, request);
    }
    Client client = initClient();
    Response response = client.handle(request);
    if (log.isLoggable(Level.FINE)) {
      RestClientLogHelper.logHttpResponse(log, Level.FINE, response);
    }
    if (response.getStatus().isSuccess()) {
      return response;
    }

    throw new RepositoryException("Encountered error while retrieving http response (HttpStatus: " + response.getStatus() + ", Body: "
            + response.getEntity().getText() + ")");
  }

  /**
   * Overwrite this method to add required security mechanisms to request.
   * For example: request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, getConfiguration().getUser(), getConfiguration().getPassword()));
   * @param request
   * @return modified request enhanced with security data
   */
  protected abstract Request injectSecurityMechanism(Request request);

  private Request createRequest(Reference reference, Representation representation) {
    if (reference != null) {
      Request request = new Request();
      request.setResourceRef(reference);
      if (representation != null) {
        request.setEntity(representation);
      }
    
      return request;
    }
    
    throw new RepositoryException("Reference object is null!");
  }
  
  private Request createJsonRequest(Reference reference, Representation representation) {
    Request jsonRequest = createRequest(reference, representation);
    jsonRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
    
    return jsonRequest;
  }
  
  private Request createXmlRequest(Reference reference, Representation representation) {
    Request xmlRequest = createRequest(reference, representation);
    xmlRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_XML));
    
    return xmlRequest;
  }
  
  public Response getJson(Reference reference, Representation representation) throws IOException {
    Request getRequest = createJsonRequest(reference, representation);
    getRequest.setMethod(Method.GET);

    return sendRequest(getRequest);
  }
  
  public Response postJson(Reference reference, Representation representation) throws IOException {
    Request postRequest = createJsonRequest(reference, representation);
    postRequest.setMethod(Method.POST);

    return sendRequest(postRequest);
  }
  
  public Response putJson(Reference reference, Representation representation) throws IOException {
    Request putRequest = createJsonRequest(reference, representation);
    putRequest.setMethod(Method.PUT);

    return sendRequest(putRequest);
  }
  
  public Response deleteJson(Reference reference, Representation representation) throws IOException {
    Request deleteRequest = createJsonRequest(reference, representation);
    deleteRequest.setMethod(Method.DELETE);

    return sendRequest(deleteRequest);
  }
  
  public Response getXml(Reference reference, Representation representation) throws IOException {
    Request getRequest = createXmlRequest(reference, representation);
    getRequest.setMethod(Method.GET);

    return sendRequest(getRequest);
  }
  
  public Response postXml(Reference reference, Representation representation) throws IOException {
    Request postRequest = createXmlRequest(reference, representation);
    postRequest.setMethod(Method.POST);

    return sendRequest(postRequest);
  }
  
  public Response putXml(Reference reference, Representation representation) throws IOException {
    Request putRequest = createXmlRequest(reference, representation);
    putRequest.setMethod(Method.PUT);

    return sendRequest(putRequest);
  }
  
  public Response deleteXml(Reference reference, Representation representation) throws IOException {
    Request deleteRequest = createXmlRequest(reference, representation);
    deleteRequest.setMethod(Method.DELETE);

    return sendRequest(deleteRequest);
  }

}
