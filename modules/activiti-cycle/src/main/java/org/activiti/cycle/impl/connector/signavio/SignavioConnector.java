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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.annotations.Interceptors;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.impl.RepositoryArtifactImpl;
import org.activiti.cycle.impl.RepositoryFolderImpl;
import org.activiti.cycle.impl.RepositoryNodeCollectionImpl;
import org.activiti.cycle.impl.connector.AbstractRepositoryConnector;
import org.activiti.cycle.impl.connector.ConnectorLoginInterceptor;
import org.activiti.cycle.impl.connector.PasswordEnabledRepositoryConnector;
import org.activiti.cycle.impl.connector.signavio.provider.JsonProvider;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioBpmn20ArtifactType;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioDefaultArtifactType;
import org.activiti.cycle.impl.connector.signavio.util.SignavioJsonHelper;
import org.activiti.cycle.impl.connector.util.RestClientLogHelper;
import org.activiti.cycle.impl.util.IoUtils;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import de.hpi.bpmn2_0.transformation.Json2XmlConverter;

/**
 * TODO: Check correct Exceptions to be thrown (use
 * {@link RepositoryNodeNotFoundException}
 * 
 * @author christian.lipphardt@camunda.com
 * @author ruecker
 */
@CycleComponent
@Interceptors({ ConnectorLoginInterceptor.class })
public class SignavioConnector extends AbstractRepositoryConnector implements SignavioConnectorInterface, PasswordEnabledRepositoryConnector {

  // TODO: use it or not?
  public final static String CONFIG_KEY_FOLDER_ROOT_URL = "folderRootUrl";
  public final static String CONFIG_KEY_SIGNAVIO_BASE_URL = "signavioBaseUrl";
  public final static String CONFIG_KEY_USERNAME = "username";
  public final static String CONFIG_KEY_PASSWORD = "password";
  // signavio | oryx
  public final static String CONFIG_KEY_TYPE = "type";
  /**
   * indicates if the {@link SignavioConnector}needs to login into Signavio,
   * which is the case for the enterprise/saas version of Signavio, but not the
   * OSS version (where trying to login leads to an exception)
   */
  public final static String CONFIG_KEY_LOGIN_REQUIRED = "loginRequired";

  protected final static String[] CONFIG_KEYS = { //
  CONFIG_KEY_FOLDER_ROOT_URL, //
      CONFIG_KEY_SIGNAVIO_BASE_URL, //
      CONFIG_KEY_LOGIN_REQUIRED, //
      CONFIG_KEY_USERNAME, //
      CONFIG_KEY_PASSWORD //
  };

  private SignavioConnectorConfiguration configuration;

  private static Logger log = Logger.getLogger(SignavioConnector.class.getName());

  /**
   * Captcha ID for REST access to Signavio
   */
  private static final String SERVER_SECURITY_ID = "000000";

  /**
   * Security token obtained from Signavio after login for the current session
   */
  private String securityToken = "";

  private transient Client restletClient;

  private boolean loggedIn = false;

  public SignavioConnector() {
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

    throw new RepositoryException("Encountered error while retrieving response: " + response);
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

  protected boolean registerUserWithSignavio(String firstname, String lastname, String email, String password) throws IOException {
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
    if (!getConfigValue(CONFIG_KEY_LOGIN_REQUIRED, Boolean.class)) {
      // skip login if configured to do so
      return true;
    }
    String token = null;
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
      token = representation.getText();
    } catch (Exception ex) {
      throw new RepositoryException("Error during login to connector '" + getName() + "'", ex);
    }
    if (token.matches("[a-f0-9]{32}")) {
      setSecurityToken(token);
      log.fine("SecurityToken: " + getSecurityToken());
    } else {
      Matcher matcher = Pattern.compile("<div id=\"warning\">([^<]+)</div>").matcher(token);
      if (matcher.find()) {
        String errorMessage = matcher.group(1);
        throw new RepositoryException(errorMessage);
      }
      throw new RepositoryException("No Security Token received. The user name and/or password might be incorrect.");
    }
    loggedIn = true;
    return true;
  }

  private RepositoryFolder getFolderInfo(JSONObject jsonDirectoryObject) throws JSONException {
    if (!"dir".equals(jsonDirectoryObject.getString("rel")) && !"info".equals(jsonDirectoryObject.getString("rel"))) {
      // TODO: Think about that!
      throw new RepositoryException(jsonDirectoryObject + " is not a directory");
    }
    String directoryName = jsonDirectoryObject.getJSONObject("rep").getString("name");

    if (jsonDirectoryObject.getJSONObject("rep").has("type")) {
      // need hard coded translation of some special folders with special
      // treatment by signavio
      // see https://app.camunda.com/jira/browse/HEMERA-328
      String type = jsonDirectoryObject.getJSONObject("rep").optString("type");
      if ("public".equals(type)) {
        // TODO: Think about what to show here (I18n?)
        directoryName = "Public";
      }
      if ("private".equals(type)) {
        directoryName = "Private";
      }
    }
    log.finest("Directoryname: " + directoryName);

    // String directoryDescription =
    // jsonDirectoryObject.getJSONObject("rep").getString("description");
    String href = jsonDirectoryObject.getString("href");

    // TODO: Check where we get the real ID from!
    String id = null;
    if (href.endsWith("/info")) {
      // checking for '/info' at url end and remove it
      id = getConfiguration().getDirectoryIdFromInfoUrl(href);
    } else {
      id = getConfiguration().getDirectoryIdFromUrl(href);
    }
    RepositoryFolderImpl folderInfo = new RepositoryFolderImpl(getId(), id);

    folderInfo.getMetadata().setName(directoryName);

    String parent = jsonDirectoryObject.getJSONObject("rep").optString("parent");
    if (parent != null) {
      String parentId = getConfiguration().getDirectoryIdFromUrl(parent);
      folderInfo.getMetadata().setParentFolderId(parentId);
    }

    // TODO: Where do we get the path from?
    // folderInfo.getMetadata().setPath();
    return folderInfo;
  }

  private RepositoryArtifact getArtifactInfoFromFolderLink(JSONObject json) throws JSONException {
    String id = getConfiguration().getModelIdFromUrl(json.getString("href"));
    return getArtifactInfoFromFile(id, json.getJSONObject("rep"));
  }

  private RepositoryArtifact getArtifactInfoFromFile(String id, JSONObject json) throws JSONException {
    RepositoryArtifactType artifactType = getArtifactTypeForSignavioArtifact(json);
    RepositoryArtifactImpl fileInfo = new RepositoryArtifactImpl(getId(), id, artifactType, this);

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

    String parent = json.optString("parent");
    if (parent != null) {
      String parentId = getConfiguration().getModelIdFromUrl(parent);
      parentId = parentId.replace("/directory", "");
      fileInfo.getMetadata().setParentFolderId(parentId);
    }

    // relObject.getJSONObject("rep").getString("revision"); --> UUID of
    // revision
    // relObject.getJSONObject("rep").getString("description");
    // relObject.getJSONObject("rep").getString("comment");
    return fileInfo;

    // TODO: Add file actions here (jpdl4/bpmn20/png), maybe define an action
    // factory which produces the concrete actions?
  }

  private RepositoryArtifactType getArtifactTypeForSignavioArtifact(JSONObject json) throws JSONException {
    RepositoryArtifactType artifactType = CycleApplicationContext.get(SignavioDefaultArtifactType.class);

    if (json.has("namespace")) {
      // Commercial Signavio way of doing it
      String namespace = json.getString("namespace");
      if (SignavioBpmn20ArtifactType.SIGNAVIO_NAMESPACE_FOR_BPMN_2_0.equals(namespace)) {
        artifactType = CycleApplicationContext.get(SignavioBpmn20ArtifactType.class);
      }
      // else if
      // (SignavioJpdl4ArtifactType.SIGNAVIO_NAMESPACE_FOR_BPMN_JBPM4.equals(namespace))
      // {
      // artifactType =
      // CycleApplicationContext.get(SignavioJpdl4ArtifactType.class);
      // }
    } else {
      // Oryx/Signavio OSS = Activiti Modeler way of doing it
      String type = json.getString("type");
      if (SignavioBpmn20ArtifactType.ORYX_TYPE_ATTRIBUTE_FOR_BPMN_20.equals(type)) {
        artifactType = CycleApplicationContext.get(SignavioBpmn20ArtifactType.class);
      } else if (SignavioBpmn20ArtifactType.SIGNAVIO_NAMESPACE_FOR_BPMN_2_0.equals(type)) {
        artifactType = CycleApplicationContext.get(SignavioBpmn20ArtifactType.class);
      }
    }
    return artifactType;
  }

  public RepositoryNodeCollection getChildren(String id) throws RepositoryNodeNotFoundException {
    try {
      ArrayList<RepositoryNode> nodes = new ArrayList<RepositoryNode>();
      if (getConfiguration() instanceof OryxConnectorConfiguration) {
        nodes = getChildrenFromOryxBackend(id);
      } else {
        Response directoryResponse = getJsonResponse(getConfiguration().getDirectoryUrl(id));
        JsonRepresentation jsonData = new JsonRepresentation(directoryResponse.getEntity());
        JSONArray relJsonArray = jsonData.getJsonArray();

        if (log.isLoggable(Level.FINEST)) {
          RestClientLogHelper.logJSONArray(log, Level.FINEST, relJsonArray);
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
        Collections.sort(nodes, new Comparator<RepositoryNode>() {

          public int compare(RepositoryNode arg0, RepositoryNode arg1) {
            return arg0.getMetadata().getName().compareTo(arg1.getMetadata().getName());
          }
        });
      }
      return new RepositoryNodeCollectionImpl(nodes);
    } catch (Exception ex) {
      throw new RepositoryNodeNotFoundException(getName(), RepositoryFolder.class, id, ex);
    }
  }

  private ArrayList<RepositoryNode> getChildrenFromOryxBackend(String id) throws IOException, JSONException {
    ArrayList<RepositoryNode> nodes = new ArrayList<RepositoryNode>();
    // extracts only BPMN 2.0 models, since everything else is more or less
    // unsupported
    SignavioConnectorConfiguration connectorConfiguration = getConfiguration();
    String connectorId = getId();
    int pageSize = 10;
    if ("/".equals(id)) {
      JSONArray stencilsets = getStencilsets();
      for (int i = 0; i < stencilsets.length(); i++) {
        JSONObject stencilsetInfo = stencilsets.getJSONObject(i);
        String stencilsetName = stencilsetInfo.getString("title");
        String stencilsetNamespace = stencilsetInfo.getString("namespace");
        // takes too long: JSONArray modelRefs =
        // getModelIdsFromOryxBackend(stencilsetNamespace);
        String folderId = stencilsetNamespace;
        String folderName = stencilsetName; // takes too long: + " (" +
                                            // modelRefs.length() + ")";
        RepositoryFolder virtualFolder = new RepositoryFolderImpl(connectorId, folderId);
        virtualFolder.getMetadata().setName(folderName);
        nodes.add(virtualFolder);
      }
    } else {
      int lastIndexOfNumberSign = id.lastIndexOf('#');
      if (lastIndexOfNumberSign == id.length() - 1) {
        JSONArray modelRefs = getModelIdsFromOryxBackend(id);
        int numberOfModels = modelRefs.length();
        if (numberOfModels <= pageSize) {
          for (int i = modelRefs.length() - 1; (i >= 0 && i > modelRefs.length() - pageSize); i--) {
            String modelRef = modelRefs.getString(i);
            String modelId = connectorConfiguration.getModelIdFromUrl(modelRef);
            Response infoResponse = getJsonResponse(connectorConfiguration.getModelInfoUrl(modelId));
            JsonRepresentation jsonRepresentation = new JsonRepresentation(infoResponse.getEntity());
            RepositoryArtifact fileInfo = getArtifactInfoFromFile(modelId, jsonRepresentation.getJsonObject());
            nodes.add(fileInfo);
          }
        } else {
          for (int j = 0; j <= numberOfModels / pageSize; j++) {
            String folderId = id + j;
            int upperBound = numberOfModels;
            if (j + 1 <= numberOfModels / pageSize) {
              upperBound = (j + 1) * pageSize;
            }
            String folderName = j * pageSize + "-" + upperBound;
            RepositoryFolder virtualFolder = new RepositoryFolderImpl(connectorId, folderId);
            virtualFolder.getMetadata().setName(folderName);
            nodes.add(virtualFolder);
          }
        }
      } else {
        Integer pageNumber = Integer.valueOf(id.substring(lastIndexOfNumberSign + 1));
        id = (String) id.subSequence(0, lastIndexOfNumberSign + 1);
        JSONArray modelRefs = getModelIdsFromOryxBackend(id);
        for (int i = modelRefs.length() - 1 - pageNumber * pageSize; (i >= 0 && i > modelRefs.length() - (pageNumber + 1) * pageSize); i--) {
          String modelRef = modelRefs.getString(i);
          String modelId = connectorConfiguration.getModelIdFromUrl(modelRef);
          Response infoResponse = getJsonResponse(connectorConfiguration.getModelInfoUrl(modelId));
          JsonRepresentation jsonRepresentation = new JsonRepresentation(infoResponse.getEntity());
          RepositoryArtifact fileInfo = getArtifactInfoFromFile(modelId, jsonRepresentation.getJsonObject());
          nodes.add(fileInfo);
        }
      }
    }
    return nodes;
  }

  private JSONArray getModelIdsFromOryxBackend(String stencilsetNamespace) throws IOException, JSONException {
    String type = URLEncoder.encode(stencilsetNamespace, "UTF-8");
    Response filterResponse = getJsonResponse(getConfiguration().getRepositoryBackendUrl() + "filter?type=" + type + "&sort=rating");
    JsonRepresentation jsonRepresentation = new JsonRepresentation(filterResponse.getEntity());
    JSONArray modelRefs = jsonRepresentation.getJsonArray();
    return modelRefs;
  }

  private JSONArray getStencilsets() throws IOException, JSONException {
    JSONArray stencilsets;
    Response stencilsetsResponse = getJsonResponse(getConfiguration().getStencilsetsUrl());
    JsonRepresentation stencilsetsJsonRepresentation = new JsonRepresentation(stencilsetsResponse.getEntity());
    stencilsets = stencilsetsJsonRepresentation.getJsonArray();
    return stencilsets;
  }

  public RepositoryFolder getRepositoryFolder(String id) {
    try {
      Response directoryResponse = getJsonResponse(getConfiguration().getDirectoryUrl(id));
      JsonRepresentation jsonData = new JsonRepresentation(directoryResponse.getEntity());
      JSONArray jsonArray = jsonData.getJsonArray();

      // search for rel: "info" in jsonArray to get directory specified by id;
      // FIXME: possible to throw npe
      JSONObject jsonDir = null;
      for (int i = 0; i < jsonArray.length(); i++) {
        jsonDir = jsonArray.getJSONObject(i);
        if ("info".equals(jsonDir.get("rel"))) {
          break;
        }
        jsonDir = null;
      }

      return getFolderInfo(jsonDir);
    } catch (Exception ex) {
      throw new RepositoryNodeNotFoundException(getName(), RepositoryArtifact.class, id, ex);
    }
  }

  public RepositoryNode getRepositoryNode(String id) throws RepositoryNodeNotFoundException {
    // <!> HACK: improve!
    try {
      return getRepositoryFolder(id);
    } catch (RepositoryNodeNotFoundException e) {
      return getRepositoryArtifact(id);
    }
  }

  public RepositoryArtifact getRepositoryArtifact(String id) {
    JsonRepresentation jsonData = null;
    JSONObject jsonObject = null;

    try {
      Response modelResponse = getJsonResponse(getConfiguration().getModelUrl(id) + "/info");
      jsonData = new JsonRepresentation(modelResponse.getEntity());
      jsonObject = jsonData.getJsonObject();
      return getArtifactInfoFromFile(id, jsonObject);
    } catch (Exception ex) {
      throw new RepositoryNodeNotFoundException(getName(), RepositoryArtifact.class, id, ex);
    }
  }

  public String getSecurityToken() {
    return securityToken;
  }

  protected void setSecurityToken(String securityToken) {
    this.securityToken = securityToken;
  }

  public RepositoryFolder createFolder(String parentFolderId, String name) throws RepositoryNodeNotFoundException {
    try {
      Form createFolderForm = new Form();
      createFolderForm.add("name", name);
      createFolderForm.add("description", ""); // TODO: what should we use here?
      createFolderForm.add("parent", "/directory" + parentFolderId);
      Representation createFolderRep = createFolderForm.getWebRepresentation();

      Request jsonRequest = new Request(Method.POST, new Reference(getConfiguration().getDirectoryRootUrl()), createFolderRep);
      jsonRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
      sendRequest(jsonRequest);

      // TODO: create response object which contains the ID or maybe skip the
      // whole artifact returning?

      // <!> HACK: getting the created folder through iteration of the child
      // nodes of the parent folder and comparing the names...
      RepositoryNodeCollection nodes = getChildren(parentFolderId);
      for (RepositoryNode node : nodes.asList()) {
        if (!(node instanceof RepositoryFolder)) {
          continue;
        }
        if (node.getMetadata().getName().equals(name)) {
          return (RepositoryFolder) node;
        }
      }

      return null;
    } catch (Exception ex) {
      // throw new RepositoryNodeNotFoundException(getConfiguration().getName(),
      // RepositoryArtifact.class, id);

      throw new RepositoryException("Unable to create subFolder '" + name + "' in parent folder '" + parentFolderId + "'");
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

  public void deleteFolder(String subFolderId) {
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
    return getConfiguration().getModelUrl(artifact.getNodeId());
  }

  protected void moveModel(String targetFolderId, String modelId) throws IOException {
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

  protected void moveDirectory(String targetFolderId, String directoryId) throws IOException {
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

  public RepositoryArtifact createEmptyArtifact(String parentFolderId, String artifactName, String artifactType) throws RepositoryNodeNotFoundException {
    return createArtifactFromJSON(parentFolderId, artifactName, artifactType, SignavioJsonHelper.getEmptypModelTemplate());
  }

  public RepositoryArtifact createArtifact(String parentFolderId, String artifactName, String artifactType, Content artifactContent)
          throws RepositoryNodeNotFoundException {
    // TODO: Add handling of different artifact types!
    return createArtifactFromJSON(parentFolderId, artifactName, artifactType, artifactContent.asString());
  }

  public RepositoryArtifact createArtifactFromContentRepresentation(String parentFolderId, String artifactName, String artifactType,
          String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException {
    // TODO: Add handling of different content representations, e.g. BPMN 2.0
    // Import
    return createArtifactFromJSON(parentFolderId, artifactName, artifactType, artifactContent.asString());
  }

  protected RepositoryArtifact createArtifactFromJSON(String parentFolderId, String artifactName, String artifactType, String jsonContent)
          throws RepositoryNodeNotFoundException {

    // TODO: Add check if model already exists (overwrite or throw exception?)

    String revisionComment = null;
    String description = null;

    try {
      // do this to check if jsonString is valid
      JSONObject jsonModel = new JSONObject(jsonContent);

      Form modelForm = new Form();
      if (revisionComment != null) {
        modelForm.add("comment", revisionComment);
      } else {
        modelForm.add("comment", "");
      }
      if (description != null) {
        modelForm.add("description", description);
      } else {
        modelForm.add("description", "");
      }
      modelForm.add("glossary_xml", new JSONArray().toString());
      modelForm.add("json_xml", jsonModel.toString());
      modelForm.add("name", artifactName);

      // TODO: Check ArtifactType here correctly
      modelForm.add("namespace", SignavioBpmn20ArtifactType.SIGNAVIO_NAMESPACE_FOR_BPMN_2_0);
      // Important: Don't set the type attribute here, otherwise it will not
      // work!

      modelForm.add("parent", "/" + getConfiguration().DIRECTORY_URL_SUFFIX + parentFolderId);
      // we have to provide a SVG (even if don't have the correct one) because
      // otherwise Signavio throws an exception in its GUI
      modelForm
              .add("svg_xml", //
                      "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:oryx=\"http://oryx-editor.org\" id=\"sid-80D82B67-3B30-4B35-A6CB-16EEE17A719F\" width=\"50\" height=\"50\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:svg=\"http://www.w3.org/2000/svg\"><defs/><g stroke=\"black\" font-family=\"Verdana, sans-serif\" font-size-adjust=\"none\" font-style=\"normal\" font-variant=\"normal\" font-weight=\"normal\" line-heigth=\"normal\" font-size=\"12\"><g class=\"stencils\" transform=\"translate(25, 25)\"><g class=\"me\"/><g class=\"children\"/><g class=\"edge\"/></g></g></svg>");
      modelForm.add("type", "BPMN 2.0");

      // Signavio generates a new id for POSTed models, so we don't have to set
      // this ID ourself.
      // But anyway, then we don't know the id and cannot load the artifact down
      // to return it correctly, so we generate one ourself
      // Christian: We need to remove the hypen in the generated uuid, otherwise
      // signavio is unable to create a model
      String id = UUID.randomUUID().toString().replace("-", "");
      modelForm.add("id", id);

      // modelForm.add("views", new JSONArray().toString());
      Representation modelRep = modelForm.getWebRepresentation();

      Request jsonRequest = new Request(Method.POST, new Reference(getConfiguration().getModelRootUrl()), modelRep);
      jsonRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
      sendRequest(jsonRequest);

      // TODO: return the object
      return getRepositoryArtifact(id);
    } catch (Exception je) {
      throw new RepositoryException("Unable to create model '" + artifactName + "' in parent folder '" + parentFolderId + "'", je);
    }
  }

  protected void restoreRevisionOfModel(String parentFolderId, String modelId, String revisionId, String comment) throws IOException {
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

  public String transformJsonToBpmn20Xml(String jsonData) {
    try {
      JSONObject json = new JSONObject(jsonData);
      // disable regeneration of IDs that don't match signavio's ID pattern
      de.hpi.bpmn2_0.factory.configuration.Configuration.ensureSignavioStyle = false;
      Json2XmlConverter converter = new Json2XmlConverter(json.toString(), this.getClass().getClassLoader().getResource("META-INF/validation/xsd/BPMN20.xsd")
              .toString());
      return converter.getXml().toString();
    } catch (Exception ex) {
      throw new RepositoryException("Error while transforming BPMN2_0_JSON to BPMN2_0_XML", ex);
    }
  }

  /**
   * FIXME: This implementation uses the bpmn2_0-import servlet which returns a
   * temporary(?) nodeid for which we then retrieve JSon Content.
   * 
   * NOTE: I could not make this work using Restlet. Maybe I just dont get it...
   * (daniel)
   */
  public String transformBpmn20XmltoJson(String xml) {
    File tmpfile = null;
    try {
      HttpClient client = new DefaultHttpClient();
      client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
      String postUrl = getConfiguration().getBpmn20XmlImportServletUrl();
      HttpPost post = new HttpPost(postUrl);
      post.addHeader("token", getSecurityToken());
      MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

      // creating a temporary file
      tmpfile = File.createTempFile(UUID.randomUUID().toString(), ".xml");
      OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpfile));
      InputStream is = new ByteArrayInputStream(xml.getBytes("UTF8"));
      IoUtils.copyBytes(is, os);
      os.flush();
      os.close();
      entity.addPart("bpmn2_0file", new FileBody(tmpfile));
      // apparently this works:
      entity.addPart("directory", new StringBody("/directory", Charset.forName("UTF-8")));
      post.setEntity(entity);

      // get the response (id of the temporary model)
      String response = EntityUtils.toString(client.execute(post).getEntity(), "UTF-8");

      // the returned response is of the form ["..."], I suggest this is the
      // case because an xml-file can contain multiple process definitions. For
      // the moment we just cut the json-list brackets.
      String artifactId = response.substring(2);
      artifactId = artifactId.substring(0, artifactId.length() - 2);

      HttpGet get = new HttpGet(getConfiguration().getEditorUrl(artifactId) + "&data");
      get.addHeader("token", getSecurityToken());
      // let's pretend we're Firefox on Windows
      get.addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13");

      // get the json Representation for the temporary model
      response = EntityUtils.toString(client.execute(get).getEntity(), "UTF-8");

      client.getConnectionManager().shutdown();
      
      JSONObject jsonObj = new JSONObject(response);      
      return jsonObj.getString("model");
      
    } catch (Exception ex) {
      throw new RepositoryException("Error while transforming BPMN2_0_XML to BPMN2_0_JSON", ex);
    } finally {
      if (tmpfile != null) {
        tmpfile.delete();
      }
    }
  }
  public void updateContent(String artifactId, Content content) throws RepositoryNodeNotFoundException {
    throw new RepositoryException("Moving artifacts is not (yet) supported by the Signavio Connector");
  }

  public void updateContent(String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException {
    throw new RepositoryException("Moving artifacts is not (yet) supported by the Signavio Connector");
  }

  public boolean isLoggedIn() {
    Boolean loginRequired = getConfigValue(CONFIG_KEY_LOGIN_REQUIRED, Boolean.class);
    if (loginRequired==null) {
      throw new RepositoryException("Configuration of Signavio Connector must contain the configuration attribute '"+CONFIG_KEY_LOGIN_REQUIRED+"'");
    }
    return loggedIn || !loginRequired;
  }

  public Content getContent(String artifactId) throws RepositoryNodeNotFoundException {
    RepositoryArtifact artifact = getRepositoryArtifact(artifactId);
    return getDefaultContentRepresentation(artifactId).getContent(artifact);
  }

  public ContentRepresentation getDefaultContentRepresentation(String artifactId) throws RepositoryNodeNotFoundException {
    // use the JSON-representation as default.
    // TODO: good idea?
    return CycleApplicationContext.get(JsonProvider.class);
  }

  public String[] getConfigurationKeys() {
    return CONFIG_KEYS;
  }

  public String getPassword() {
    return getConfigValue(CONFIG_KEY_PASSWORD, String.class);
  }

  public String getUsername() {
    return getConfigValue(CONFIG_KEY_USERNAME, String.class);
  }

  protected String getSignavioUrl() {
    return getConfigValue(CONFIG_KEY_SIGNAVIO_BASE_URL, String.class);
  }

  @Override
  protected void validateConfiguration() {
    String path = (String) getConfigValue(CONFIG_KEY_SIGNAVIO_BASE_URL);
    if (path != null && !path.endsWith("/")) {
      path = path + "/";
    }
    setConfigValue(CONFIG_KEY_SIGNAVIO_BASE_URL, path);
    String type = getConfigValue(CONFIG_KEY_TYPE, String.class);
    if ("oryx".equals(type)) {
      configuration = new OryxConnectorConfiguration(this);
    } else {
      configuration = new SignavioConnectorConfiguration(this);
    }
  }

  public SignavioConnectorConfiguration getConfiguration() {
    return configuration;
  }

  public String concatenateNodeId(String prefix, String suffix) {
    return prefix + ";" + suffix;
  }

}
