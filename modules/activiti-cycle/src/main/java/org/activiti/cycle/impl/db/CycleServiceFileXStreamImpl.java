package org.activiti.cycle.impl.db;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.activiti.cycle.CycleService;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;
import org.activiti.cycle.impl.plugin.PluginFinder;

import com.thoughtworks.xstream.XStream;

/**
 * VERY EASY implementation of Configuration Service to write stuff as XML on
 * disk. DO NOT USE THIS ANYMORE, WILL BE DELETED SOON.
 * 
 * This is just used temporary until real persistence is implemented. The API
 * <b>and the resulting XML</b> should NOT be seen as stable in the meantime.
 */
@Deprecated
public class CycleServiceFileXStreamImpl extends DummyBaseCycleService implements CycleService {
 
  // TODO: Set a config dir for xstream?
  // private static final String CONFIG_DIR = "";
  private static final String FILE_EXT = ".cycle-conf.xml";
  
  private XStream xStream = new XStream();
  
  public CycleServiceFileXStreamImpl() {
    PluginFinder.checkPluginInitialization();
  }

  public XStream getXStream() {
    return xStream;
  }

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

}
