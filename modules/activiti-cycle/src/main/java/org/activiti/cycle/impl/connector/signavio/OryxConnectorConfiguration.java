package org.activiti.cycle.impl.connector.signavio;


public class OryxConnectorConfiguration extends SignavioConnectorConfiguration {
  
  public OryxConnectorConfiguration() {
    init();
  }

  public OryxConnectorConfiguration(String signavioUrl) {
    super(signavioUrl);
    init();
  }

  public OryxConnectorConfiguration(String name, String signavioBaseUrl, String folderRootUrl, String password, String user) {
    super(name, signavioBaseUrl, folderRootUrl, password, user);
    init();
  }

  public OryxConnectorConfiguration(String name, String signavioBaseUrl) {
    super(name, signavioBaseUrl);
    init();
  }
  
  private void init() {
    // these values differ between Oryx and Signavio
    REPOSITORY_BACKEND_URL_SUFFIX = "backend/poem/";
    EDITOR_BACKEND_URL_SUFFIX = "oryx/";
  }

}
