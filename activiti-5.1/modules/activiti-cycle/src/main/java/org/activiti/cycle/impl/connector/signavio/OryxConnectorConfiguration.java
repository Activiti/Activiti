package org.activiti.cycle.impl.connector.signavio;


public class OryxConnectorConfiguration extends SignavioConnectorConfiguration {
  
  // these values differ between Oryx and Signavio
  protected static String REPOSITORY_BACKEND_URL_SUFFIX = "backend/poem/";
  protected static String EDITOR_BACKEND_URL_SUFFIX = "oryx/";
  
  public static String EDITOR_URL_SUFFIX = "editor#/model/";

  public OryxConnectorConfiguration() {
  }

  public OryxConnectorConfiguration(String signavioUrl) {
    super(signavioUrl);
  }

  public OryxConnectorConfiguration(String name, String signavioBaseUrl, String folderRootUrl, String password, String user) {
    super(name, signavioBaseUrl, folderRootUrl, password, user);
  }

  public OryxConnectorConfiguration(String name, String signavioBaseUrl) {
    super(name, signavioBaseUrl);
  }

  @Override
  public String getRepositoryBackendUrlSuffix() {
    return REPOSITORY_BACKEND_URL_SUFFIX;
  }

  @Override
  public String getEditorBackendUrlSuffix() {
    return EDITOR_BACKEND_URL_SUFFIX;
  }

  @Override
  public String getEditorUrl(String id) {
    if (id.startsWith("/")) {
      // this is how it should be now
      return getEditorBackendUrl() + EDITOR_URL_SUFFIX + id.substring(1);
    } else {
      // this is how it was in ancient times
      return getEditorBackendUrl() + EDITOR_URL_SUFFIX + id;
    }
  }

}
