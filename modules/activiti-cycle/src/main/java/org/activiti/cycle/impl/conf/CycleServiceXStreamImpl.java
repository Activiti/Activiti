package org.activiti.cycle.impl.conf;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.plugin.PluginFinder;

import com.thoughtworks.xstream.XStream;

/**
 * VERY EASY implementation of Configuration Service to write stuff as XML on
 * disk.
 * 
 * This is just used temporary until real persistence is implemented. The API
 * <b>and the resulting XML</b> should NOT be seen as stable in the meantime.
 */
public class CycleServiceXStreamImpl implements CycleService {

  // TODO: Set a config dir for xstream?
  // private static final String CONFIG_DIR = "";
  private static final String FILE_EXT = ".cycle-conf.xml";
  
  private XStream xStream = new XStream();
  

  // private List<Class< ? extends RepositoryConnector>>
  // registeredRepositoryConnnectors = new ArrayList<Class< ? extends
  // RepositoryConnector>>();
  // private List<RepositoryConnectorConfiguration> repoConnectorConfigurations
  // = findAllRepositoryConfigurations();
  // private List<RepositoryConnectorConfiguration> repoConnectorConfigurations
  // = new ArrayList<RepositoryConnectorConfiguration>();

  public CycleServiceXStreamImpl() {
    PluginFinder.checkPluginInitialization();
  }
  
  public CycleServiceXStreamImpl(File baseDir) {
    // TODO: Do something with the base dir

    PluginFinder.checkPluginInitialization();
  }

  public XStream getXStream() {
    return xStream;
  }

  // public RepositoryConnectorConfiguration
  // createRepositoryConfiguration(Class< ? extends RepositoryConnector>
  // repositoryConnector, String user,
  // String password, String basePath) {
  // try {
  // if (registeredRepositoryConnnectors.contains(repositoryConnector)) {
  // RepositoryConnectorConfiguration config = null;
  // // FIXME: Better way for instance creation
  // config = (RepositoryConnectorConfiguration)
  // Class.forName(repositoryConnector.getCanonicalName() +
  // "Configuration").newInstance();
  // config.setUser(user);
  // config.setPassword(password);
  // config.setSignavioUrl(basePath);
  // repoConnectorConfigurations.add(config);
  // return config;
  // }
  // throw new RepositoryException("RepositoryConnector '" +
  // repositoryConnector.getClass().getName() + "' is not registered!");
  // } catch (Exception e) {
  // throw new RepositoryException("Unable to create configuration for " +
  // repositoryConnector.getClass().getName(), e);
  // }
  // }
  //
  // public void registerRepositoryConnector(Class< ? extends
  // RepositoryConnector> repositoryConnector) {
  // if (repositoryConnector != null) {
  // registeredRepositoryConnnectors.add(repositoryConnector);
  // }
  // }
  //
  // public List<Class< ? extends RepositoryConnector>>
  // getRegisteredRepositoryConnectors() {
  // return registeredRepositoryConnnectors;
  // }

  public void persistRepositoryConfiguration(RepositoryConnectorConfiguration config) {
    saveObjectToFile(config.getName(), config);
  }

  public List<RepositoryConnectorConfiguration> findAllRepositoryConfigurations() {
    // TODO: Implement retrieving all files
    return new ArrayList<RepositoryConnectorConfiguration>();
  }

  public RepositoryConnectorConfiguration getRepositoryConfiguration(String name) {
    return (RepositoryConnectorConfiguration) loadFromFile(name);
  }

  public void removeRepositoryConfiguration(String name) {
    new File(getFileName(name)).delete();
  }
  
  public void saveObjectToFile(String name, Object o) {
    String configFileName = getFileName(name);
    try {
      FileWriter fileWriter = new FileWriter(configFileName);
      getXStream().toXML(o, fileWriter);
      fileWriter.close();
    } catch (IOException ioe) {
      throw new RepositoryException("Unable to persist '" + name + "' as XML in the file system sd file '" + configFileName + "'", ioe);
    }
  }

  private String getFileName(String name) {
    return name + FILE_EXT;
  }

  public Object loadFromFile(String name) {
    String configFileName = getFileName(name);
    try {
      FileReader fileReader = new FileReader(configFileName);
      Object config = getXStream().fromXML(fileReader);
      fileReader.close();
      return config;
    } catch (IOException ioe) {
      throw new RepositoryException("Unable to load '" + name + "' as XML in the file system sd file '" + configFileName + "'", ioe);
    }
  }

  public void saveConfiguration(ConfigurationContainer container) {
    saveObjectToFile(container.getName(), container);
  }

  public ConfigurationContainer getConfiguration(String name) {
    return (ConfigurationContainer) loadFromFile(name);
  }

  // public void persistAllRepositoryConfigurations() {
  // for (RepositoryConnectorConfiguration config : repoConnectorConfigurations)
  // {
  // persistRepositoryConfiguration(config);
  // }
  // }

  // public RepositoryConnector
  // createRepositoryConnectorFromConfiguration(RepositoryConnectorConfiguration
  // repositoryConfig) {
  // try {
  // Class connectorClass =
  // Class.forName(repositoryConfig.getClass().getName().replace("Configuration",
  // ""));
  // return (RepositoryConnector)
  // connectorClass.getConstructor(repositoryConfig.getClass()).newInstance(repositoryConfig);
  // } catch (Exception e) {
  // throw new RepositoryException("Unable to create repository connector!", e);
  // }
  // }
  //
  // public List<RepositoryConnector>
  // createRepositoryConnectorsFromConfigurations() {
  // List<RepositoryConnector> connectors = new
  // ArrayList<RepositoryConnector>();
  //
  // for (RepositoryConnectorConfiguration config : repoConnectorConfigurations)
  // {
  // connectors.add(createRepositoryConnectorFromConfiguration(config));
  // }
  //
  // return connectors;
  // }
  //
  // public List<RepositoryConnectorConfiguration>
  // findAllRepositoryConfigurations() {
  // List<RepositoryConnectorConfiguration> configs = new
  // ArrayList<RepositoryConnectorConfiguration>();
  //
  // for (Class< ? extends RepositoryConnector> connector :
  // registeredRepositoryConnnectors) {
  // String clazzName = connector.getName() + "Configuration";
  // try {
  // RepositoryConnectorConfiguration config =
  // (RepositoryConnectorConfiguration) Class.forName(clazzName).newInstance();
  // configs.add(findRepositoryConfiguration(config.getClass()));
  // } catch (Exception e) {
  // throw new RepositoryException("Unable to find class '" + clazzName + "'",
  // e);
  // }
  // }
  //
  // return configs;
  // }
  //
  // public RepositoryConnectorConfiguration findRepositoryConfiguration(Class<
  // ? extends RepositoryConnectorConfiguration> config) {
  // String configFileName = config.getSimpleName() + FILE_EXT;
  // try {
  // XStream xStream = new XStream();
  // RepositoryConnectorConfiguration loadedConfig =
  // (RepositoryConnectorConfiguration) xStream.fromXML(new
  // FileReader(configFileName));
  // return loadedConfig;
  // } catch (FileNotFoundException fnfe) {
  // throw new
  // RepositoryException("Unable to find RepositoryConnectorConfiguration '" +
  // configFileName + "'", fnfe);
  // }
  // }

}
