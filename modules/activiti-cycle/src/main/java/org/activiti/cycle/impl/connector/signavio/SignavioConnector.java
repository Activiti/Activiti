/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.cycle.impl.connector.signavio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentationDefinition;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.impl.connector.AbstractRepositoryConnector;
import org.activiti.cycle.impl.connector.signavio.util.SignavioJsonHelper;
import org.activiti.cycle.impl.connector.signavio.util.SignavioLogHelper;
import org.activiti.cycle.impl.plugin.ActivitiCyclePluginRegistry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;

/**
 * TODO: Check correct Exceptions to be thrown (use
 * {@link RepositoryNodeNotFoundException}
 * 
 * @author christian.lipphardt@camunda.com
 * @author ruecker
 */
public class SignavioConnector extends AbstractRepositoryConnector<SignavioConnectorConfiguration> {

  private static Logger log = Logger.getLogger(SignavioConnector.class.getName());

  // register Signavio stencilsets to identify file types
  public static final String SIGNAVIO_NAMESPACE_FOR_BPMN_2_0 = "http://b3mn.org/stencilset/bpmn2.0#";
  public static final String SIGNAVIO_NAMESPACE_FOR_BPMN_JBPM4 = "http://b3mn.org/stencilset/jbpm4#";

  public static final String BPMN_2_0_XML = "bpm2.0"; // FIXME looks like typo
  public static final String ORYX_TYPE_ATTRIBUTE_FOR_BPMN_20 = "BPMN 2.0";

  /**
   * Captcha ID for REST access to Signavio
   */
  private static final String SERVER_SECURITY_ID = "000000";

  /**
   * Security token obtained from Signavio after login for the current session
   */
  private String securityToken = "";

  private transient Client restletClient;

  public SignavioConnector(SignavioConnectorConfiguration signavioConfiguration) {
    super(signavioConfiguration);
  }

  public Client initClient() {
    // TODO: Check timeout on client and re-create it

    if (restletClient == null) {
      // Create and initialize HTTP client for HTTP REST API calls
      restletClient = new Client(new Context(), Protocol.HTTP);
      restletClient.getContext().getParameters().add("converter", "com.noelios.restlet.http.HttpClientConverter");
    }
    return restletClient;
  }

  public Response sendRequest(Request request) throws IOException {
    injectSecurityToken(request);

    if (log.isLoggable(Level.FINE)) {
      SignavioLogHelper.logHttpRequest(log, Level.FINE, request);
    }

    Client client = initClient();
    Response response = client.handle(request);

    if (log.isLoggable(Level.FINE)) {
      SignavioLogHelper.logHttpResponse(log, Level.FINE, response);
    }

    if (response.getStatus().isSuccess()) {
      return response;
    }

    throw new RepositoryException("Encountered error while retrieving response (HttpStatus: " + response.getStatus() + ", Body: "
            + response.getEntity().getText() + ")");
  }

  private Request injectSecurityToken(Request request) {
    Form requestHeaders = new Form();
    requestHeaders.add("token", getSecurityToken());
    request.getAttributes().put("org.restlet.http.headers", requestHeaders);

    return request;
  }

  public Response getJsonResponse(String url) throws IOException {
    Request jsonRequest = new Request(Method.GET, new Reference(url));
    jsonRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

    return sendRequest(jsonRequest);
  }

  public boolean registerUserWithSignavio(String firstname, String lastname, String email, String password) throws IOException {
    // Create the Post Parameters for registering a new user
    Form registrationForm = new Form();
    registrationForm.add("mode", "external");
    registrationForm.add("firstName", firstname);
    registrationForm.add("lastName", lastname);
    registrationForm.add("mail", email);
    registrationForm.add("password", password);
    registrationForm.add("serverSecurityId", SERVER_SECURITY_ID);
    Representation registrationRep = registrationForm.getWebRepresentation();

    Request registrationRequest = new Request(Method.POST, getConfiguration().getRegistrationUrl(), registrationRep);
    Response registrationResponse = sendRequest(registrationRequest);

    if (registrationResponse.getStatus().equals(Status.SUCCESS_CREATED)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean login(String username, String password) {
    if (!getConfiguration().isLoginRequired()) {
      // skip login if configured to do so
      return true;
    }
    if (getConfiguration().isCredentialsSaved()) {
      // TODO: Should we do that more generically?
      username = getConfiguration().getUser();
      password = getConfiguration().getPassword();
    }
    try {
      log.info("Logging into Signavio on url: " + getConfiguration().getLoginUrl());

      // Login a user
      Form loginForm = new Form();
      loginForm.add("name", username);
      loginForm.add("password", password);
      loginForm.add("tokenonly", "true");
      Representation loginRep = loginForm.getWebRepresentation();

      Request loginRequest = new Request(Method.POST, getConfiguration().getLoginUrl(), loginRep);
      Response loginResponse = sendRequest(loginRequest);

      Representation representation = loginResponse.getEntity();
      setSecurityToken(representation.getText());
      log.fine("SecurityToken: " + getSecurityToken());
      return true;
    } catch (Exception ex) {
      throw new RepositoryException("Error during login to connector '" + getConfiguration().getName() + "'", ex);
    }
  }

  private RepositoryFolder getFolderInfo(JSONObject jsonDirectoryObject) throws JSONException {
    if (!"dir".equals(jsonDirectoryObject.getString("rel"))) {
      // TODO: Think about that!
      throw new RepositoryException(jsonDirectoryObject + " is not a directory");
    }
    String directoryName = jsonDirectoryObject.getJSONObject("rep").getString("name");
    log.finest("Directoryname: " + directoryName);

    // String directoryDescription =
    // jsonDirectoryObject.getJSONObject("rep").getString("description");
    String href = jsonDirectoryObject.getString("href");

    // for (JSONObject subDirectoryInfo : getSubDirectoryInfos(href)) {
    // printDirectory(subDirectoryInfo, indention);
    // }
    //
    // for (JSONObject modelInfo : getSubModelInfos(href)) {
    // String modelName = modelInfo.getJSONObject("rep").getString("name");
    // String modelType = modelInfo.getJSONObject("rep").getString("type");
    // System.out.println(indention + "- MODEL " + modelName + " (" + modelType
    // + ")");
    // }

    RepositoryFolder folderInfo = new RepositoryFolder(this);
    // folderInfo.setId( directoryId );
    // TODO: Check where we get the real ID from!
    folderInfo.setId(getConfiguration().getDirectoryIdFromUrl(href));

    folderInfo.getMetadata().setName(directoryName);
    // TODO: Where do we get the path from?
    // folderInfo.getMetadata().setPath();

    return folderInfo;
  }

  private RepositoryArtifact getArtifactInfoFromFolderLink(JSONObject json) throws JSONException {
    String id = getConfiguration().getModelIdFromUrl(json.getString("href"));
    return getArtifactInfoFromFile(id, json.getJSONObject("rep"));
  }

  private RepositoryArtifact getArtifactInfoFromFile(String id, JSONObject json) throws JSONException {
    RepositoryArtifact fileInfo = new RepositoryArtifact(this);
    fileInfo.setId(id);

    if (json.has("name")) {
      fileInfo.getMetadata().setName(json.optString("name"));
    } else {
      fileInfo.getMetadata().setName(json.optString("title"));
    }
    // TODO: This seems not to work 100% correctly
    fileInfo.getMetadata().setVersion(json.optString("rev"));

    // TODO: Check if that is really last author and if we can get the original
    // author
    fileInfo.getMetadata().setLastAuthor(json.optString("author"));
    fileInfo.getMetadata().setCreated(SignavioJsonHelper.getDateValueIfExists(json, "created"));
    fileInfo.getMetadata().setLastChanged(SignavioJsonHelper.getDateValueIfExists(json, "updated"));

    if (json.has("namespace")) {
      // Commercial Signavio way of doing it
      String fileTypeIdentifier = json.getString("namespace");
      fileInfo.setArtifactType(ActivitiCyclePluginRegistry.getArtifactTypeByIdentifier(fileTypeIdentifier));
    } else {
      // Oryx/Signavio OSS = Activiti Modeler way of doing it
      String fileTypeIdentifier = json.getString("type");
      fileInfo.setArtifactType(ActivitiCyclePluginRegistry.getArtifactTypeByIdentifier(fileTypeIdentifier));
    }

    // relObject.getJSONObject("rep").getString("revision"); --> UUID of
    // revision
    // relObject.getJSONObject("rep").getString("description");
    // relObject.getJSONObject("rep").getString("comment");
    return fileInfo;

    // TODO: Add file actions here (jpdl4/bpmn20/png), maybe define an action
    // factory which produces the concrete actions?
  }

  public List<RepositoryNode> getChildNodes(String parentId) {
    try {
      ArrayList<RepositoryNode> nodes;
      try {
        nodes = new ArrayList<RepositoryNode>();
        Response directoryResponse = getJsonResponse(getConfiguration().getDirectoryUrl(parentId));
        JsonRepresentation jsonData = new JsonRepresentation(directoryResponse.getEntity());
        JSONArray relJsonArray = jsonData.toJsonArray();
  
        if (log.isLoggable(Level.FINEST)) {
          SignavioLogHelper.logJSONArray(log, Level.FINEST, relJsonArray);
        }
  
        for (int i = 0; i < relJsonArray.length(); i++) {
          JSONObject relObject = relJsonArray.getJSONObject(i);
  
          if ("dir".equals(relObject.getString("rel"))) {
            RepositoryFolder folderInfo = getFolderInfo(relObject);
            nodes.add(folderInfo);
          } else if ("mod".equals(relObject.getString("rel"))) {
            RepositoryArtifact fileInfo = getArtifactInfoFromFolderLink(relObject);
            nodes.add(fileInfo);
          }
        }
      } catch (RepositoryException e) {
        nodes = getModelsFromOryxBackend();
      }
      return nodes;
    } catch (Exception ex) {
      throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, parentId, ex);
    }
  }

  private ArrayList<RepositoryNode> getModelsFromOryxBackend() throws IOException, JSONException {
    ArrayList<RepositoryNode> nodes = new ArrayList<RepositoryNode>();
    // extracts only BPMN 2.0 models, since everything else is more or less unsupported
    Response filterResponse = getJsonResponse(getConfiguration().getRepositoryBackendUrl() + "filter?type=http%3A%2F%2Fb3mn.org%2Fstencilset%2Fbpmn2.0%23&sort=rating");
    JsonRepresentation jsonRepresentation = new JsonRepresentation(filterResponse.getEntity());
    JSONArray modelRefs = jsonRepresentation.toJsonArray();
    for (int i = 0; i < modelRefs.length(); i++) {
      String modelRef = modelRefs.getString(i);
      String modelId = getConfiguration().getModelIdFromUrl(modelRef);
      Response infoResponse = getJsonResponse(getConfiguration().getModelInfoUrl(modelId));
      jsonRepresentation = new JsonRepresentation(infoResponse.getEntity());
      RepositoryArtifact fileInfo = getArtifactInfoFromFile(modelId, jsonRepresentation.toJsonObject());
      nodes.add(fileInfo);
    }
    return nodes;
  }

  public Content getContent(String nodeId, String representationName) {
    RepositoryArtifact artifact = getRepositoryArtifact(nodeId);
    return artifact.loadContent(representationName);
  }

  public RepositoryFolder getRepositoryFolder(String id) {
    try {
      Response directoryResponse = getJsonResponse(getConfiguration().getDirectoryUrl(id));
      JsonRepresentation jsonData = new JsonRepresentation(directoryResponse.getEntity());
      JSONObject jsonObject = jsonData.toJsonObject();
      return getFolderInfo(jsonObject);
    } catch (Exception ex) {
      throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, id, ex);
    }
  }

  public RepositoryArtifact getRepositoryArtifact(String id) {
    JsonRepresentation jsonData = null;
    JSONObject jsonObject = null;

    try {
      Response modelResponse = getJsonResponse(getConfiguration().getModelUrl(id) + "/info");
      jsonData = new JsonRepresentation(modelResponse.getEntity());
      if (log.isLoggable(Level.FINE)) {
        log.fine("JsonData - (" + jsonData.getText() + ")");
      }
      jsonObject = jsonData.toJsonObject();
      return getArtifactInfoFromFile(id, jsonObject);
    } catch (Exception ex) {
      throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, id, ex);
    }
  }

  public String getSecurityToken() {
    return securityToken;
  }

  public void setSecurityToken(String securityToken) {
    this.securityToken = securityToken;
  }

  public void createNewSubFolder(String parentFolderId, RepositoryFolder subFolder) {
    try {
      Form createFolderForm = new Form();
      createFolderForm.add("name", subFolder.getMetadata().getName());
      createFolderForm.add("description", ""); // TODO: what should we use here?
      createFolderForm.add("parent", "/directory/" + parentFolderId);
      Representation createFolderRep = createFolderForm.getWebRepresentation();

      Request jsonRequest = new Request(Method.POST, new Reference(getConfiguration().getDirectoryRootUrl()), createFolderRep);
      jsonRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
      sendRequest(jsonRequest);
    } catch (Exception ex) {
      // throw new RepositoryNodeNotFoundException(getConfiguration().getName(),
      // RepositoryArtifact.class, id);

      throw new RepositoryException("Unable to create subFolder '" + subFolder + "' in parent folder '" + parentFolderId + "'");
    }
  }

  public void deleteArtifact(String artifactId) {
    try {
      Request jsonRequest = new Request(Method.DELETE, new Reference(getConfiguration().getModelUrl(artifactId)));
      jsonRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

      sendRequest(jsonRequest);
    } catch (Exception ex) {
      // throw new RepositoryNodeNotFoundException(getConfiguration().getName(),
      // RepositoryArtifact.class, id);
      throw new RepositoryException("Unable to delete model " + artifactId);
    }
  }

  public void deleteSubFolder(String subFolderId) {
    try {
      Request jsonRequest = new Request(Method.DELETE, new Reference(getConfiguration().getDirectoryUrl(subFolderId)));
      jsonRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

      sendRequest(jsonRequest);
    } catch (Exception ex) {
      // throw new RepositoryNodeNotFoundException(getConfiguration().getName(),
      // RepositoryArtifact.class, id);
      throw new RepositoryException("Unable to delete directory " + subFolderId);
    }
  }

  public String getModelUrl(RepositoryArtifact artifact) {
    return getConfiguration().getModelUrl(artifact.getId());
  }

  public void commitPendingChanges(String comment) {
  }

  public void moveModel(String targetFolderId, String modelId) throws IOException {
    try {
      Form bodyForm = new Form();
      bodyForm.add("parent", "/directory/" + targetFolderId);
      Representation bodyRep = bodyForm.getWebRepresentation();

      Request jsonRequest = new Request(Method.PUT, new Reference(getConfiguration().getModelUrl(modelId)), bodyRep);
      jsonRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
      sendRequest(jsonRequest);
    } catch (Exception ex) {
      // throw new RepositoryNodeNotFoundException(getConfiguration().getName(),
      // RepositoryArtifact.class, id);
      throw new RepositoryException("Unable to move model " + modelId + " to folder " + targetFolderId, ex);
    }
  }

  public void moveDirectory(String targetFolderId, String directoryId) throws IOException {
    try {
      Form bodyForm = new Form();
      bodyForm.add("parent", "/directory/" + targetFolderId);
      Representation bodyRep = bodyForm.getWebRepresentation();

      Request jsonRequest = new Request(Method.PUT, new Reference(getConfiguration().getDirectoryUrl(directoryId)), bodyRep);
      jsonRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
      sendRequest(jsonRequest);
    } catch (Exception ex) {
      // throw new RepositoryNodeNotFoundException(getConfiguration().getName(),
      // RepositoryArtifact.class, id);
      throw new RepositoryException("Unable to move folder " + directoryId + " to directory " + targetFolderId, ex);
    }
  }

  public void createNewArtifact(String folderId, RepositoryArtifact artifact, Content content) {
    // TODO: Should we have a check for the content type? Is it possible to
    // create an artifact by different types?
    try {
      createNewModel(folderId, artifact.getMetadata().getName(), content.asString(), null, null);
    } catch (IOException ioe) {
      throw new RepositoryException("Unable to create new model '" + artifact.getMetadata().getName() + "' in folder '" + folderId + "'", ioe);
    }
  }

  public void createNewModel(String parentFolderId, String name, String jsonData, String revisionComment, String description) throws IOException {
    try {
      // do this to check if jsonString is valid
      JSONObject jsonModel = new JSONObject(jsonData);

      Form modelForm = new Form();
      // TODO: Check if this is correct, maybe we need to include an empty
      // string
      if (revisionComment != null) {
        modelForm.add("comment", revisionComment);
      }
      if (description != null) {
        modelForm.add("description", description);
      }
      modelForm.add("glossary_xml", new JSONArray().toString());
      // signavio generates a new id for POSTed models
      // modelForm.add("id", null);
      modelForm.add("json_xml", jsonModel.toString());
      modelForm.add("name", name);
      modelForm.add("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
      modelForm.add("parent", "/directory/" + parentFolderId);
      // we have to provide a SVG (even if don't have the correct one) because
      // otherwise Signavio throws an exception in its GUI
      modelForm
              .add(
                      "svg_xml",
                      "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:oryx=\"http://oryx-editor.org\" id=\"sid-80D82B67-3B30-4B35-A6CB-16EEE17A719F\" width=\"50\" height=\"50\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:svg=\"http://www.w3.org/2000/svg\"><defs/><g stroke=\"black\" font-family=\"Verdana, sans-serif\" font-size-adjust=\"none\" font-style=\"normal\" font-variant=\"normal\" font-weight=\"normal\" line-heigth=\"normal\" font-size=\"12\"><g class=\"stencils\" transform=\"translate(25, 25)\"><g class=\"me\"/><g class=\"children\"/><g class=\"edge\"/></g></g></svg>");
      modelForm.add("type", "BPMN 2.0");
      // modelForm.add("views", new JSONArray().toString());
      Representation modelRep = modelForm.getWebRepresentation();

      Request jsonRequest = new Request(Method.POST, new Reference(getConfiguration().getModelRootUrl()), modelRep);
      jsonRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
      sendRequest(jsonRequest);
    } catch (Exception je) {
      throw new RepositoryException("Unable to create model '" + name + "' in parent folder '" + parentFolderId + "'", je);
    }
  }

  public void restoreRevisionOfModel(String parentFolderId, String modelId, String revisionId, String comment) throws IOException {
    try {
      Form reivisionForm = new Form();
      reivisionForm.add("comment", comment);
      reivisionForm.add("parent", "/directory/" + parentFolderId);
      reivisionForm.add("revision", revisionId);
      Representation modelRep = reivisionForm.getWebRepresentation();

      Request jsonRequest = new Request(Method.PUT, new Reference(getConfiguration().getModelUrl(modelId)), modelRep);
      jsonRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
      sendRequest(jsonRequest);
    } catch (Exception je) {
      throw new RepositoryException("Unable to restore revision '" + revisionId + "' of model '" + modelId + "' in directory '" + parentFolderId + "'", je);
    }
  }

  public void modifyArtifact(RepositoryArtifact artifact, ContentRepresentationDefinition artifactContent) {
  }

  public String transformJsonToBpmn20Xml(String jsonData) {
    try {
      JSONObject json = new JSONObject(jsonData);

      Form dataForm = new Form();
      dataForm.add("data", json.toString());
      Representation jsonDataRep = dataForm.getWebRepresentation();

      Request request = new Request(Method.POST, new Reference(getConfiguration().getBpmn20XmlExportServletUrl()), jsonDataRep);
      Response jsonResponse = sendRequest(request);

      // "xml" is just one entry in the returned JSON map
      return new JSONObject(jsonResponse.getEntity().getText()).getString("xml");

    } catch (Exception ex) {
      throw new RepositoryException("Error while transforming BPMN2_0_JSON to BPMN2_0_XML", ex);
    }
  }

  /**
   * FIXME: unfinished but maybe it works... method accepts a xml String and
   * returns the json representation
   */
  public String transformBpmn20XmltoJson(String xmlData) {
    try {
      Form dataForm = new Form();
      dataForm.add("data", xmlData);
      Representation xmlDataRep = dataForm.getWebRepresentation();

      Request request = new Request(Method.POST, new Reference(getConfiguration().getBpmn20XmlImportServletUrl()), xmlDataRep);
      request.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
      Response jsonResponse = sendRequest(request);

      return new JSONObject(jsonResponse.getEntity().getText()).toString();

    } catch (Exception ex) {
      throw new RepositoryException("Error while transforming BPMN2_0_XML to BPMN2_0_JSON", ex);
    }
  }
}
