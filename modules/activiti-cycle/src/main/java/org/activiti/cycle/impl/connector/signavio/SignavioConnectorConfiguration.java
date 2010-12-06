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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.conf.PasswordEnabledRepositoryConnectorConfiguration;

/**
 * Object used to configure signavio connector. Candidate for Entity to save
 * config later on.
 * 
 * The Signavio URL has a trailing "/", the URL-SUFFIXES TODO: CHECK
 * 
 * @author bernd.ruecker@camunda.com
 */
public class SignavioConnectorConfiguration extends PasswordEnabledRepositoryConnectorConfiguration {

  // TODO: use it or not?
  private String folderRootUrl;

  private String signavioBaseUrl;

  /**
   * indicates if the {@link SignavioConnector}needs to login into Signavio,
   * which is the case for the enterprise/saas version of Signavio, but not the
   * OSS version (where trying to login leads to an exception)
   */
  private boolean loginRequired = false;

  // these values differ between Oryx and Signavio
  protected static String REPOSITORY_BACKEND_URL_SUFFIX = "p/";
  protected static String EDITOR_BACKEND_URL_SUFFIX = "editor/";
  protected static String EDITOR_URL_SUFFIX = "editor?id=";

  public static String REGISTRATION_URL_SUFFIX = "register/";
  public static String LOGIN_URL_SUFFIX = "login/";
  public static String EXPLORER_URL_SUFFIX = "explorer/";
  public static String MODEL_URL_SUFFIX = "model";
  public static String DIRECTORY_URL_SUFFIX = "directory";
  public static String MASHUP_URL_SUFFIX = "mashup/";
  public static String MODEL_INFO_URL_SUFFIX = "info";
  public static String STENCILSETS_URL_SUFFIX = "stencilsets/stencilsets.json";

  public static String BPMN_20_EXPORT_SERVLET = "bpmn2_0serialization";
  public static String BPMN_20_IMPORT_SERVLET = "bpmn2_0deserialization";

  public SignavioConnectorConfiguration() {
    signavioBaseUrl = "http://127.0.0.1:8080/";
  }

  public SignavioConnectorConfiguration(String signavioUrl) {
    signavioBaseUrl = signavioUrl;
  }

  public SignavioConnectorConfiguration(String name, String signavioBaseUrl, String folderRootUrl, String password, String user) {
    setName(name);
    this.signavioBaseUrl = signavioBaseUrl;
    this.folderRootUrl = folderRootUrl;
    setPassword(password);
    setUser(user);
  }

  public SignavioConnectorConfiguration(String name, String signavioBaseUrl) {
    setName(name);
    this.signavioBaseUrl = signavioBaseUrl;
  }

  /**
   * TODO: Think about a better concept to handle this
   */
  @Override
  public ArtifactType getDefaultArtifactType() {
    if (hasArtifactType(SignavioPluginDefinition.ARTIFACT_TYPE_DEFAULT)) {
      return getArtifactType(SignavioPluginDefinition.ARTIFACT_TYPE_DEFAULT);
    } else {
      return null;
    }
  }

  public String getSignavioUrl() {
    return signavioBaseUrl;
  }

  public void setSignavioUrl(String path) {
    if (path != null && !path.endsWith("/")) {
      path = path + "/";
    }
    this.signavioBaseUrl = path;
  }

  public String getRepositoryBackendUrl() {
    return getSignavioUrl() + getRepositoryBackendUrlSuffix();
  }

  public String getEditorBackendUrl() {
    return getSignavioUrl() + getEditorBackendUrlSuffix();
  }

  public String getDirectoryIdFromInfoUrl(String href) {
    String url = getDirectoryIdFromUrl(href);
    // to remove '/info' from url end
    url = url.substring(0, url.length() - 5);
    return url;
  }

  public String getDirectoryIdFromUrl(String href) {
    return retrieveIdFromUrl(href, "/" + DIRECTORY_URL_SUFFIX);
  }

  public String getModelIdFromUrl(String href) {
    return retrieveIdFromUrl(href, "/" + MODEL_URL_SUFFIX);
  }

  /**
   * get the part of the URL identifying the real ID needed to be stored in the
   * API object to be able to identify the object later on
   */
  private String retrieveIdFromUrl(String href, String baseUrl) {
    // TODO: Check implementation!
    return href.replaceAll(baseUrl, "");
  }

  public String getModelUrl(String id) {
    if (id.startsWith("/")) {
      // don't encode this one!
      id = id.substring(1);
    }
    return getRepositoryBackendUrl() + MODEL_URL_SUFFIX + "/" + encode(id);
  }

  private String encode(String id) {
    try {
      return URLEncoder.encode(id, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      throw new RepositoryException("Couldn't UTF-8 encode id '" + id + "' for Signavio.", ex);
    }
  }

  // TODO: CARE about correct encoding of ID! But wasn't that easy and need some
  // serious thinking
  // try {
  // if (id.startsWith("/")) {
  // id = id.substring(1);
  // }
  // // TODO: Check encoding on other places!
  // String encodedId = URLEncoder.encode(id, "UTF-8");
  // // this is how it should be now
  // return getSignavioBackendUrl() + MODEL_URL_SUFFIX + "/" + encodedId;
  // } catch (UnsupportedEncodingException ex) {
  // throw new
  // IllegalStateException("Unexpected excpetion while encoding the URL to UTF-8 for model id '"
  // + id + "'", ex);
  // }

  public String getDirectoryUrl(String id) {
    if (id.startsWith("/")) {
      // don't encode this one!
      id = id.substring(1);
    }
    return getRepositoryBackendUrl() + DIRECTORY_URL_SUFFIX + "/" + encode(id);
  }

  public String getRegistrationUrl() {
    return getRepositoryBackendUrl() + REGISTRATION_URL_SUFFIX;
  }

  public String getLoginUrl() {
    return getRepositoryBackendUrl() + LOGIN_URL_SUFFIX;
  }

  public String getEditorUrl(String id) {
    if (id.startsWith("/")) {
      // this is how it should be now
      return getRepositoryBackendUrl() + EDITOR_URL_SUFFIX + id.substring(1);
    } else {
      // this is how it was in ancient times
      return getRepositoryBackendUrl() + EDITOR_URL_SUFFIX + id;
    }
  }

  public String getPngUrl(String id, String securityToken) {
    return getModelUrl(id) + "/png?token=" + securityToken;
  }

  public String getExplorerUrl() {
    return getRepositoryBackendUrl() + EXPLORER_URL_SUFFIX;
  }

  /**
   * TODO: Rename?
   */
  public String getModelRootUrl() {
    return getRepositoryBackendUrl() + MODEL_URL_SUFFIX + "/";
  }

  public String getDirectoryRootUrl() {
    return getRepositoryBackendUrl() + DIRECTORY_URL_SUFFIX + "/";
  }

  public String getMashupUrl() {
    return getRepositoryBackendUrl() + MASHUP_URL_SUFFIX;
  }

  public String getBpmn20XmlExportServletUrl() {
    return getEditorBackendUrl() + BPMN_20_EXPORT_SERVLET;
  }

  public String getBpmn20XmlImportServletUrl() {
    return getEditorBackendUrl() + BPMN_20_IMPORT_SERVLET;
  }

  @Override
  public RepositoryConnector createConnector() {
    RepositoryConnector connector = CycleComponentFactory.getCycleComponentInstance(SignavioConnector.class, RepositoryConnector.class);
    connector.setConfiguration(this);
    return connector;
  }

  public boolean isLoginRequired() {
    return loginRequired;
  }

  public void setLoginRequired(boolean loginRequired) {
    this.loginRequired = loginRequired;
  }

  /**
   * TODO: What is that and why it is needed?
   */
  public String getPurl() {
    return getSignavioUrl() + "purl";
  }

  public String getModelInfoUrl(String modelId) {
    return getModelUrl(modelId) + "/" + MODEL_INFO_URL_SUFFIX;
  }

  public String getRepositoryBackendUrlSuffix() {
    return REPOSITORY_BACKEND_URL_SUFFIX;
  }

  public String getEditorBackendUrlSuffix() {
    return EDITOR_BACKEND_URL_SUFFIX;
  }

  public String getStencilsetsUrl() {
    return getEditorBackendUrl() + STENCILSETS_URL_SUFFIX;
  }

}
