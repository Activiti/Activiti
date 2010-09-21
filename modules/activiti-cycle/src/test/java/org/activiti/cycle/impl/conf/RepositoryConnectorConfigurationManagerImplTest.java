package org.activiti.cycle.impl.conf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration;
import org.activiti.cycle.impl.connector.view.CustomizedViewConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO: Remove sysouts
 * 
 * @author christian.lipphardt
 */
public class RepositoryConnectorConfigurationManagerImplTest {

  private CycleService configurationService;

  @Before
  public void setUp() throws Exception {
    configurationService = new CycleServiceXStreamImpl();
  }

  @After
  public void tearDown() throws Exception {
    configurationService = null;
  }
  
  @Test
  public void testAPI() {
    try {
      ConfigurationContainer enterpriseConfiguration = new ConfigurationContainer("camunda");
      // This one is for all, so don't save a password, the GUI should query it!
      RepositoryConnectorConfiguration conf1 = new SignavioConnectorConfiguration("Activiti Modeler", "http://localhost:8080/activiti-modeler/");
      enterpriseConfiguration.addRepositoryConnectorConfiguration(conf1);

      ConfigurationContainer userConfiguration = new ConfigurationContainer("bernd");
      userConfiguration.addParent(enterpriseConfiguration);
      RepositoryConnectorConfiguration conf2 = new FileSystemConnectorConfiguration("Hard Drive", new File("c:"));
      userConfiguration.addRepositoryConnectorConfiguration(conf2);
      // This one ist just for me, I save the password
      RepositoryConnectorConfiguration conf3 = new SignavioConnectorConfiguration("Signavio SAAS", "http://editor.signavio.com/", null,
              "bernd.ruecker@camunda.com", "xxx");
      userConfiguration.addRepositoryConnectorConfiguration(conf3);

      // now we have a config for the user containing 2 repository configs

      configurationService.saveConfiguration(enterpriseConfiguration);
      configurationService.saveConfiguration(userConfiguration);

      ConfigurationContainer loadedConf = configurationService.getConfiguration("bernd");

      // ConfigurationContainer configuration =
      // configurationService.getConfiguration("bernd");
      List<RepositoryConnectorConfiguration> connectors = loadedConf.getConnectorConfigurations();
      assertEquals(3, connectors.size());
      assertEquals("Hard Drive", connectors.get(0).getName());
      assertEquals("Signavio SAAS", connectors.get(1).getName());
      assertEquals("Activiti Modeler", connectors.get(2).getName());

      RepositoryConnector connector = new CustomizedViewConfiguration("http://localhost:8080/activiti-cycle/", loadedConf).createConnector();
      
      // check that files were created
      assertTrue(new File("bernd.cycle-conf.xml").delete());
      assertTrue(new File("camunda.cycle-conf.xml").delete());
    } finally {
      // clean up to delete created configs, do it in the finally again to make
      // sure they are deleted
      new File("bernd.cycle-conf.xml").delete();
      new File("camunda.cycle-conf.xml").delete();
    }
  }
  
//  @Test
//  public void testInsertCycleConfiguration() {
//    String id = "kristin";
//    String configXML = "<org.activiti.cycle.impl.conf.ConfigurationContainer>"
//                       + " <name>kristin</name>"
//                       + " <linkedConnectors>"
//                       + "<org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration>"
//                       + "<name>Activiti Modeler</name>"
//                       + "<credentialsSaved>false</credentialsSaved>"
//                       + "<signavioBaseUrl>http://localhost:8080/activiti-modeler/</signavioBaseUrl>"
//                       + "<loginRequired>false</loginRequired>"
//                       + "</org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration>"
//                       + "<org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration>"
//                       + "<name>Eclipse Workspace (File System)</name>"
//                       + "<baseFilePath>C:/Dokumente+und+Einstellungen/polenz/workspace/activiti/distro/target/activiti-5.0.beta2-SNAPSHOT/apps/eclipse-workspace</baseFilePath>"
//                       + "</org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration>"
//                       + "<org.activiti.cycle.impl.connector.signavio.OryxConnectorConfiguration>"
//                       + "<name>oryx-project.org</name>"
//                       + "<credentialsSaved>false</credentialsSaved>"
//                       + "<signavioBaseUrl>http://oryx-project.org/</signavioBaseUrl>"
//                       + "<loginRequired>false</loginRequired>"
//                       + "</org.activiti.cycle.impl.connector.signavio.OryxConnectorConfiguration>"
//                       + "</linkedConnectors>"
//                       + "<parentContainers/>"
//                       + "</org.activiti.cycle.impl.conf.ConfigurationContainer>";
//    configurationService.createAndInsert(configXML, id);
//    
//  }
//  
//  @Test
//  public void testSelectById() {
//    CycleConfigEntity cycleConfig = configurationService.selectById("kristin");
//    if (cycleConfig != null)
//        System.out.println("Cycle Config XML: " + cycleConfig.getConfigXML());
//    
//    String configXML = "<org.activiti.cycle.impl.conf.ConfigurationContainer>"
//      + " <name>kristinPolenz</name>"
//      + " <linkedConnectors>"
//      + "<org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration>"
//      + "<name>Activiti Modeler</name>"
//      + "<credentialsSaved>false</credentialsSaved>"
//      + "<signavioBaseUrl>http://localhost:8080/activiti-modeler/</signavioBaseUrl>"
//      + "<loginRequired>false</loginRequired>"
//      + "</org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration>"
//      + "<org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration>"
//      + "<name>Eclipse Workspace (File System)</name>"
//      + "<baseFilePath>C:/Dokumente+und+Einstellungen/polenz/workspace/activiti/distro/target/activiti-5.0.beta2-SNAPSHOT/apps/eclipse-workspace</baseFilePath>"
//      + "</org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration>"
//      + "<org.activiti.cycle.impl.connector.signavio.OryxConnectorConfiguration>"
//      + "<name>oryx-project.org</name>"
//      + "<credentialsSaved>false</credentialsSaved>"
//      + "<signavioBaseUrl>http://oryx-project.org/</signavioBaseUrl>"
//      + "<loginRequired>false</loginRequired>"
//      + "</org.activiti.cycle.impl.connector.signavio.OryxConnectorConfiguration>"
//      + "</linkedConnectors>"
//      + "<parentContainers/>"
//      + "</org.activiti.cycle.impl.conf.ConfigurationContainer>";
//    
//    //update by id
//    cycleConfig.setConfigXML(configXML);
//    cycleConfig.setRevision(cycleConfig.getRevision()+1);
//    
//    configurationService.updateById(cycleConfig);
//  }
//  
//  @Test
//  public void testDeleteById() {
//    configurationService.deleteById("kristin");
//  }
  
  //  
  //
  // @Test
  // public void testPersistRepositoryConfiguration() {
  // SignavioConnectorConfiguration sigConf = new
  // SignavioConnectorConfiguration();
  // repoConfManager.persistRepositoryConfiguration(sigConf);
  //
  // DemoConnectorConfiguration demoConf = new DemoConnectorConfiguration();
  // repoConfManager.persistRepositoryConfiguration(demoConf);
  //
  // FileSystemConnectorConfiguration fileConf = new
  // FileSystemConnectorConfiguration();
  // repoConfManager.persistRepositoryConfiguration(fileConf);
  // }
  //
  // @Test
  // public void testCreateRepositoryConfiguration() {
  // // repoConfManager.registerRepositoryConnector(DemoConnector.class);
  // // repoConfManager.registerRepositoryConnector(SignavioConnector.class);
  // // repoConfManager.registerRepositoryConnector(FileSystemConnector.class);
  //
  // RepositoryConnectorConfiguration config = new
  // SignavioConnectorConfiguration("Local Signavio", "http://localhost:8080",
  // null, "christian.lipphardt", "xxx");
  // System.out.println(config);
  //
  // repoConfManager.persistRepositoryConfiguration(config);
  // }
  //
  // @Test
  // public void testRepoConfigUsage() {
  // // register connectors
  // // repoConfManager.registerRepositoryConnector(DemoConnector.class);
  // // repoConfManager.registerRepositoryConnector(SignavioConnector.class);
  // // repoConfManager.registerRepositoryConnector(FileSystemConnector.class);
  //
  // // create configurations
  // repoConfManager.persistRepositoryConfiguration(new
  // SignavioConnectorConfiguration("Activiti Modeler",
  // "http://localhost:8080/activiti-modeler/", null,
  // "christian.lipphardt", "xxx"));
  // repoConfManager.persistRepositoryConfiguration(new
  // FileSystemConnectorConfiguration("Hard Drive", new File("c:")));
  //
  // // // create connector instances based on configs
  // // List<RepositoryConnector> connectors =
  // // repoConfManager.createRepositoryConnectorsFromConfigurations();
  // //
  // // // use connectors
  // // for (RepositoryConnector repositoryConnector : connectors) {
  // // List<RepositoryNode> nodes = repositoryConnector.getChildNodes("");
  // // System.out.println(repositoryConnector.getClass().getName() + ": " +
  // // nodes);
  // // }
  // }
  //
  // @Test
  // public void testLoadRepoConfig() {
  // // register connectors
  // // config for demo connector does not exists, what to do?
  // // repoConfManager.registerRepositoryConnector(DemoConnector.class);
  // CycleConfigurationService repoConfManager1 = new
  // SimpleXstreamRepositoryConnectorConfigurationManager();
  // // repoConfManager1.registerRepositoryConnector(SignavioConnector.class);
  // // repoConfManager1.registerRepositoryConnector(FileSystemConnector.class);
  //
  // // get configs from filesystem
  // List< ? extends RepositoryConnectorConfiguration> configs =
  // repoConfManager1.findAllRepositoryConfigurations();
  // for (RepositoryConnectorConfiguration config : configs) {
  // System.out.println(config);
  // RepositoryConnector connector = config.createConnector();
  // List<RepositoryNode> nodes = connector.getChildNodes("");
  // System.out.println(connector.getClass().getName() + ": " + nodes);
  // }
  // }
}
