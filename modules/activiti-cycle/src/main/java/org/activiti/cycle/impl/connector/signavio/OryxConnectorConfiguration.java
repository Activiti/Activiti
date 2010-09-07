package org.activiti.cycle.impl.connector.signavio;


public class OryxConnectorConfiguration extends SignavioConnectorConfiguration {
  
  // these values differ between Oryx and Signavio
  protected static String REPOSITORY_BACKEND_URL_SUFFIX = "backend/poem/";
  protected static String EDITOR_BACKEND_URL_SUFFIX = "oryx/";

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

  public static String getRepositoryBackendUrlSuffix() {
    return REPOSITORY_BACKEND_URL_SUFFIX;
  }

  public String getEditorBackendUrlSuffix() {
    return EDITOR_BACKEND_URL_SUFFIX;
  }

}
