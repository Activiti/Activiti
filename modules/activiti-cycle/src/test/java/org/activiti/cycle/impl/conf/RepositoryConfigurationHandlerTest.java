package org.activiti.cycle.impl.conf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration;
import org.activiti.cycle.incubator.connector.svn.SvnConnectorConfiguration;
import org.activiti.cycle.incubator.connector.vfs.VfsConnectorConfiguration;
import org.junit.Test;

public class RepositoryConfigurationHandlerTest {

  @Test
  public void testGetConfigurationFields() {
    RepositoryConfigurationHandler handler = new RepositoryConfigurationHandler();
    System.out.println(handler.getConfigurationFields(SvnConnectorConfiguration.class.getCanonicalName()));
    System.out.println(handler.getConfigurationFields(SignavioConnectorConfiguration.class.getCanonicalName()));
    System.out.println(handler.getConfigurationFields(FileSystemConnectorConfiguration.class.getCanonicalName()));
    System.out.println(handler.getConfigurationFields(VfsConnectorConfiguration.class.getCanonicalName()));
  }

  @Test
  public void setConfigurationFields() {
    RepositoryConfigurationHandler handler = new RepositoryConfigurationHandler();
    Map<String, String> keyValueMap = new HashMap<String, String>();
    for (String fieldname : handler.getConfigurationFields(SvnConnectorConfiguration.class.getCanonicalName()).keySet()) {
      keyValueMap.put(fieldname, UUID.randomUUID().toString());
    }
    
    SvnConnectorConfiguration config = new SvnConnectorConfiguration();
    handler.setConfigurationfields(keyValueMap, config);
    
    System.out.println(config);

  }
  
  
}
