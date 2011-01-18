package org.activiti.cycle.impl.connector;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.connector.util.RestClientLogHelper;
import org.apache.http.auth.AuthenticationException;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

/**
 * unused ? 
 */
public abstract class AbstractRestClientConnector extends AbstractRepositoryConnector implements PasswordEnabledRepositoryConnector {

  protected Map<String, Object> properties;
  protected transient Client restletClient;
  protected Context context;
  
  public AbstractRestClientConnector() {
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
    injectCustomSecurityMechanism(request);

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
    } else if (response.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {
      // retry request with additional authentication
      Request retryRequest = createChallengeResponse(response);
      return sendRequest(retryRequest);
    }

    throw new RepositoryException("Encountered error while retrieving http response (HttpStatus: " + response.getStatus() + ", Body: "
            + response.getEntity().getText() + ")");
  }

  /**
   * Overwrite this method to add required security mechanisms to requests.
   * For example: add a token to request headers
   * @param request
   * @return modified request enhanced with custom security mechanism
   */
  protected abstract Request injectCustomSecurityMechanism(Request request);

  private Request createChallengeResponse(Response resourceNeedsAuth) {
    ChallengeResponse cResponse = null;
      
    for (ChallengeRequest challengeRequest : resourceNeedsAuth.getChallengeRequests()) {
      ChallengeScheme cs = challengeRequest.getScheme();
      
      if (ChallengeScheme.HTTP_BASIC.equals(cs)) {
        if (log.isLoggable(Level.INFO)) {
          log.info("Received 401 Error -> retrying request with HTTP_BASIC AUTH now!");
        }
        cResponse = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, getUsername(), getPassword());
        break;
      }
    }
    
    if (cResponse != null) {
      Request authenticatedRequest = resourceNeedsAuth.getRequest();
      authenticatedRequest.setChallengeResponse(cResponse);
      return authenticatedRequest;
    }
    
    throw new IllegalStateException(new AuthenticationException("Unable to determine required authentication scheme!"));
  }
  
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
