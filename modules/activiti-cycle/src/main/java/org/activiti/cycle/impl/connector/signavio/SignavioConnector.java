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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.UnsupportedRepositoryOpperation;
import org.activiti.cycle.impl.RepositoryRegistry;
import org.activiti.cycle.impl.connector.signavio.action.OpenModelerAction;
import org.activiti.cycle.impl.connector.signavio.provider.Bpmn20Provider;
import org.activiti.cycle.impl.connector.signavio.provider.EmbeddableModelProvider;
import org.activiti.cycle.impl.connector.signavio.provider.JasonProvider;
import org.activiti.cycle.impl.connector.signavio.provider.Jpdl4Provider;
import org.activiti.cycle.impl.connector.signavio.provider.PngProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
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
import org.restlet.util.Series;

/**
 * TODO: refactor to differentiate between enterprise and os signavio.
 * 
 * @author christian.lipphardt@camunda.com
 */
public class SignavioConnector implements RepositoryConnector {

  // register Signavio stencilsets to identify file types
  public static final String SIGNAVIO_BPMN_2_0 = "http://b3mn.org/stencilset/bpmn2.0#";
  public static final String SIGNAVIO_BPMN_JBPM4 = "http://b3mn.org/stencilset/jbpm4#";

  static {
    // initialize associated file types
    RepositoryRegistry.registerArtifactType(new ArtifactType("Signavio BPMN 2.0", SIGNAVIO_BPMN_2_0));
    RepositoryRegistry.registerArtifactType(new ArtifactType("Signavio BPMN for jBPM 4", SIGNAVIO_BPMN_JBPM4));

    RepositoryRegistry.registerContentRepresentationProvider(SIGNAVIO_BPMN_2_0, Bpmn20Provider.class);
    RepositoryRegistry.registerContentRepresentationProvider(SIGNAVIO_BPMN_2_0, JasonProvider.class);
    RepositoryRegistry.registerContentRepresentationProvider(SIGNAVIO_BPMN_2_0, PngProvider.class);
    RepositoryRegistry.registerContentRepresentationProvider(SIGNAVIO_BPMN_2_0, EmbeddableModelProvider.class);

    RepositoryRegistry.registerContentRepresentationProvider(SIGNAVIO_BPMN_JBPM4, JasonProvider.class);
    RepositoryRegistry.registerContentRepresentationProvider(SIGNAVIO_BPMN_JBPM4, Jpdl4Provider.class);
    RepositoryRegistry.registerContentRepresentationProvider(SIGNAVIO_BPMN_JBPM4, PngProvider.class);
    RepositoryRegistry.registerContentRepresentationProvider(SIGNAVIO_BPMN_JBPM4, EmbeddableModelProvider.class);

     RepositoryRegistry.registerArtifactAction(SIGNAVIO_BPMN_2_0, OpenModelerAction.class);
    // RepositoryRegistry.registerArtifactAction(SIGNAVIO_BPMN_2_0,
    // CopyBpmn20ToSvnAction.class);

    RepositoryRegistry.registerArtifactAction(SIGNAVIO_BPMN_JBPM4, OpenModelerAction.class);
    // RepositoryRegistry.registerArtifactAction(SIGNAVIO_BPMN_JBPM4,
    // CopyJpdl4ToSvnAction.class);
    // RepositoryRegistry.registerArtifactAction(SIGNAVIO_BPMN_JBPM4,
    // CreateJbpm4AntProject.class);
    
    // RepositoryRegistry.registerArtifactAction(SIGNAVIO_BPMN_2_0,
    // ShowModelViewerAction.class);
    // RepositoryRegistry.registerArtifactAction(SIGNAVIO_BPMN_JBPM4,
    // ShowModelViewerAction.class);
  }

  private static Logger log = Logger.getLogger(SignavioConnector.class.getName());

  /**
   * Captcha ID for REST access to Signavio
   */
  private static final String SERVER_SECURITY_ID = "000000";

  /**
   * Security token obtained from Signavio after login for the current session
   */
  private String securityToken = "";
  private List<Cookie> securityCookieList = new ArrayList<Cookie>();

  private SignavioConnectorConfiguration conf;

  public SignavioConnector(SignavioConnectorConfiguration signavioConfiguration) {
    this.conf = signavioConfiguration;
  }

  /**
   * get the part of the URL identifying the real ID needed to be stored in the
   * API object to be able to identify the object later on
   */
  private String retrieveIdFromUrl(String href, String baseUrl) {
    // TODO: Check implementation!
    return href.replaceAll(baseUrl, "");
  }

  public Client initClient() {
    // Create and initialize HTTP client for HTTP REST API calls
    Client client = new Client(new Context(), Protocol.HTTP);
    client.getContext().getParameters().add("converter", "com.noelios.restlet.http.HttpClientConverter");

    return client;
  }

  public boolean registerUserWithSignavio(String firstname, String lastname, String email, String password) {
    Client client = initClient();

    Reference registrationRef = new Reference(conf.getRegistrationUrl());

    // Create the Post Parameters for registering a new user
    Form registrationForm = new Form();
    registrationForm.add("mode", "external");
    registrationForm.add("firstName", firstname);
    registrationForm.add("lastName", lastname);
    registrationForm.add("mail", email);
    registrationForm.add("password", password);
    registrationForm.add("serverSecurityId", SERVER_SECURITY_ID);
    Representation registrationRep = registrationForm.getWebRepresentation();

    Request registrationRequest = new Request(Method.POST, registrationRef, registrationRep);
    // registrationRequest.getClientInfo().getAcceptedMediaTypes().add(new
    // Preference<MediaType>(MediaType.APPLICATION_JSON));
    Response registrationResponse = client.handle(registrationRequest);

    if (log.isLoggable(Level.FINEST)) {
      SignavioLogHelper.logCookieAndBody(log, registrationResponse);
    }

    if (registrationResponse.getStatus().equals(Status.SUCCESS_CREATED)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean login(String username, String password) {
    try {
      Client client = initClient();

      log.info("Logging into Signavio on url: " + conf.getLoginUrl());

      Reference loginRef = new Reference(conf.getLoginUrl());

      // Login a user
      Form loginForm = new Form();
      // loginForm.add("mode", "external");
      loginForm.add("name", username);
      loginForm.add("password", password);
      loginForm.add("tokenonly", "true");
      // loginForm.add("serverSecurityId", "000000");
      // loginForm.add("remember", "on");
      // loginForm.add("fragment", "");
      Representation loginRep = loginForm.getWebRepresentation();

      Request loginRequest = new Request(Method.POST, loginRef, loginRep);
      Response loginResponse = client.handle(loginRequest);

      if (log.isLoggable(Level.FINEST)) {
        Series<CookieSetting> cookieSentByServer = loginResponse.getCookieSettings();
        for (Iterator<Entry<String, String>> it = cookieSentByServer.getValuesMap().entrySet().iterator(); it.hasNext();) {
          Entry<String, String> cookieParam = (Entry<String, String>) it.next();
          Cookie tempCookie = new Cookie(cookieParam.getKey(), cookieParam.getValue());
          securityCookieList.add(tempCookie);
        }
      }

      log.finest("SecurityCookieList: " + securityCookieList);
      securityToken = loginResponse.getEntity().getText();
      log.finest("SecurityToken: " + securityToken);

      if (log.isLoggable(Level.FINEST)) {
        SignavioLogHelper.logCookieAndBody(log, loginResponse);
      }

      if (securityToken != null && securityToken.length() != 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception ex) {
      throw new RepositoryException("Exception during login to Signavio", ex);
    }
  }

  public JSONObject getPublicRootDirectory() throws IOException, JSONException {
    Client client = initClient();

    Reference directoryRef = new Reference(conf.getDirectoryUrl());

    Request directoryRequest = new Request(Method.GET, directoryRef);
    directoryRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

    Form requestHeaders = new Form();
    requestHeaders.add("token", securityToken);
    directoryRequest.getAttributes().put("org.restlet.http.headers", requestHeaders);

    Response directoryResponse = client.handle(directoryRequest);
    JsonRepresentation jsonData = new JsonRepresentation(directoryResponse.getEntity());

    JSONArray rootJsonArray = jsonData.toJsonArray();

    if (log.isLoggable(Level.FINEST)) {
      SignavioLogHelper.logJSONArray(log, rootJsonArray);
    }

    // find the directory of type public which contains all directories and
    // models of this account
    for (int i = 0; i < rootJsonArray.length(); i++) {
      JSONObject rootObject = rootJsonArray.getJSONObject(i);

      if ("public".equals(rootObject.getJSONObject("rep").get("type"))) {
        return rootObject;
      }
    }

    if (log.isLoggable(Level.FINEST)) {
      SignavioLogHelper.logCookieAndBody(log, directoryResponse);
    }

    throw new RepositoryException("No directory root found in signavio repository.");
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
    folderInfo.setId(retrieveIdFromUrl(href, conf.getDirectoryUrl()));

    folderInfo.getMetadata().setName(directoryName);
    // TODO: Where do we get the path from?
    // folderInfo.getMetadata().setPath();

    return folderInfo;
  }

  private RepositoryArtifact getArtifactInfo(JSONObject relObject) throws JSONException {
    RepositoryArtifact fileInfo = new RepositoryArtifact(this);
    // TODO: where do we get the id from?
    fileInfo.setId(retrieveIdFromUrl(relObject.getString("href"), conf.getModelUrl()));
    log.finest("FileInfo-setPath: " + relObject.getString("href"));
    fileInfo.getMetadata().setName(relObject.getJSONObject("rep").getString("name"));

    // fileInfo.setId(id);
    // relObject.getJSONObject("rep").getInt("rev");

    String fileTypeIdentifier = relObject.getJSONObject("rep").getString("namespace");
    fileInfo.setArtifactType(RepositoryRegistry.getArtifactTypeByIdentifier(fileTypeIdentifier));
    // if ("BPMN 1.2".equals(relObject.getJSONObject("rep").getString("type")))
    // {
    // fileInfo.setFileType(FileTypeRegistry.getFileTyp(SIGNAVIO_BPMN_1_2));
    // }
    // else if
    // ("BPMN 2.0".equals(relObject.getJSONObject("rep").getString("type"))) {
    // fileInfo.setFileType(FileTypeRegistry.getFileTyp(SIGNAVIO_BPMN_2_0));
    // }
    // relObject.getJSONObject("rep").getString("author");
    // relObject.getJSONObject("rep").getString("revision");
    // relObject.getJSONObject("rep").getString("description");
    // relObject.getJSONObject("rep").getString("comment");
    // relObject.getJSONObject("rep").getString("namespace");
    return fileInfo;

    // TODO: Add file actions here (jpdl4/bpmn20/png), maybe define an action
    // factory which produces the concrete actions?
  }

  public RepositoryFolder getRootFolder() {
    try {
      return getFolderInfo(getPublicRootDirectory());
    } catch (Exception e) {
      throw new RepositoryException("Eror while accessing the Signavio Repository", e);
    }
  }

  public List<RepositoryNode> getChildNodes(String parentId) {
    try {
      Client client = initClient();

      Reference directoryRef = new Reference(conf.getDirectoryUrl() + parentId);

      Request directoryRequest = new Request(Method.GET, directoryRef);
      directoryRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

      Form requestHeaders = new Form();
      requestHeaders.add("token", securityToken);
      directoryRequest.getAttributes().put("org.restlet.http.headers", requestHeaders);

      Response directoryResponse = client.handle(directoryRequest);
      JsonRepresentation jsonData = new JsonRepresentation(directoryResponse.getEntity());

      JSONArray relJsonArray = jsonData.toJsonArray();

      if (log.isLoggable(Level.FINEST)) {
        SignavioLogHelper.logJSONArray(log, relJsonArray);
      }

      ArrayList<RepositoryNode> nodes = new ArrayList<RepositoryNode>();

      for (int i = 0; i < relJsonArray.length(); i++) {
        JSONObject relObject = relJsonArray.getJSONObject(i);

        if ("dir".equals(relObject.getString("rel"))) {
          RepositoryFolder folderInfo = getFolderInfo(relObject);
          nodes.add(folderInfo);
        } else if ("mod".equals(relObject.getString("rel"))) {
          RepositoryArtifact fileInfo = getArtifactInfo(relObject);
          nodes.add(fileInfo);
        }
      }

      return nodes;
    } catch (Exception ex) {
      throw new RepositoryException("Exception while accessing Signavio repository", ex);
    }
  }

  public ContentRepresentation getContent(String nodeId, String representationName) {
    return null;
  }

  public RepositoryNode getNodeDetails(String id) {
    return null;
  }

  public String getSecurityToken() {
    return securityToken;
  }

  public void setSecurityToken(String securityToken) {
    this.securityToken = securityToken;
  }

  public SignavioConnectorConfiguration getSignavioConfiguration() {
    return conf;
  }

  public void createNewFile(String folderId, RepositoryArtifact file) {
    throw new UnsupportedRepositoryOpperation("not yet implemented in Signavio repo");
  }

  public void createNewSubFolder(String parentFolderId, RepositoryFolder subFolder) {
    throw new UnsupportedRepositoryOpperation("not yet implemented in Signavio repo");
  }

  public void deleteArtifact(String artifactId) {
    throw new UnsupportedRepositoryOpperation("not yet implemented in Signavio repo");
  }

  public void deleteSubFolder(String subFolderId) {
    throw new UnsupportedRepositoryOpperation("not yet implemented in Signavio repo");
  }

  public String getModellerUrl(RepositoryArtifact artifact) {
    return getSignavioConfiguration().getModelUrl() + artifact.getId();
  }
}
