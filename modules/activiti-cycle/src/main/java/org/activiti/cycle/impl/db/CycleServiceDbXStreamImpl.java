package org.activiti.cycle.impl.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.Cycle;
import org.activiti.cycle.CycleConfig;
import org.activiti.cycle.CycleLink;
import org.activiti.cycle.CycleService;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.RepositoryArtifactLinkImpl;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.CycleDbSqlSessionFactory;
import org.activiti.cycle.impl.connector.demo.DemoConnectorConfiguration;
import org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration;
import org.activiti.cycle.impl.connector.view.RootConnectorConfiguration;
import org.activiti.cycle.impl.plugin.PluginFinder;
import org.activiti.engine.DbSchemaStrategy;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.thoughtworks.xstream.XStream;

/**
 * First implementation of {@link CycleService} using the database in the
 * background to write stuff as XML in the database.
 */
public class CycleServiceDbXStreamImpl extends DummyBaseCycleService implements CycleService {
  
  private XStream xStream = new XStream();
  
  private String processEngineName = null;
  private static Long DEFAULT_REVISION = new Long(0);
  private static String DEFAULT_ENGINE = "DEFAULT_PROCESS_ENGINE";
  
  private static HashMap<String, CycleDbSqlSessionFactory> dbFactories = new HashMap<String, CycleDbSqlSessionFactory>();

  /**
   * TODO: Check if list roots can return an empty array
   */
  private static final File fsBaseDir = File.listRoots()[0];

  
  public CycleServiceDbXStreamImpl(String processEngineName) {
    if (processEngineName == null) {
      this.processEngineName = DEFAULT_ENGINE;
    } else {
      this.processEngineName = processEngineName;
    }
  }
  
  public CycleServiceDbXStreamImpl() {
    this(DEFAULT_ENGINE);
  }
    
  private SqlSessionFactory getSessionFactory() {
    if (dbFactories.get(processEngineName) == null) {
      synchronized (dbFactories) {
        // lazy initialization, only done once per proces engine!
        if (dbFactories.get(processEngineName) == null) {          
          CycleDbSqlSessionFactory factory = new CycleDbSqlSessionFactory();
          factory.configurationCompleted(getProcessEngineConfiguration());
          performDbSchemaCreation(factory, getProcessEngineConfiguration());
          dbFactories.put(processEngineName, factory);
        }
      }
    }
    return dbFactories.get(processEngineName).getSqlSessionFactory();
  }
  
  public XStream getXStream() {
    return xStream;
  }

  //-- copied from SessionUtil class --
  
  public static RepositoryConnector getRepositoryConnector(String currentUserId, HttpSession session) {
    String key = currentUserId + "_connector";
    RepositoryConnector connector = (RepositoryConnector) session.getAttribute(key);
    if (connector == null) {
      PluginFinder.registerServletContext(session.getServletContext());

      ConfigurationContainer configuration = loadUserConfiguration(currentUserId);
      connector = new RootConnectorConfiguration(configuration).createConnector();
      
      // TODO: Correct user / password handling
      connector.login(currentUserId, currentUserId);
      
      session.setAttribute(key, connector);      
    }
    return connector;
  }

  /**
   * loads the configuration for this user. If no configuration exists, a demo
   * config is created and save to file (this xml can be easily modified later
   * on to play around with it).
   * 
   * This is a temporary solution until real persistence for configs is in place
   * 
   * TODO: This should be rewritten as soon as we have real persistence and
   * stuff
   */
  public static ConfigurationContainer loadUserConfiguration(String currentUserId) {
    CycleService cycleConfigurationService = Cycle.getCycleService(); // new CycleServiceDbXStreamImpl(configBaseDir);

    ConfigurationContainer configuration;
    try{
      configuration = cycleConfigurationService.getConfiguration(currentUserId);
    } catch(RepositoryException e) {
      configuration = createDefaultDemoConfiguration(currentUserId);
      cycleConfigurationService.saveConfiguration(configuration);
    }
    return configuration;
  }

  public static ConfigurationContainer createDefaultDemoConfiguration(String currentUserId) {
    ConfigurationContainer configuration = new ConfigurationContainer(currentUserId);
    configuration.addRepositoryConnectorConfiguration(new DemoConnectorConfiguration("demo"));
    configuration.addRepositoryConnectorConfiguration(new SignavioConnectorConfiguration("signavio", "http://localhost:8080/activiti-modeler/"));
    configuration.addRepositoryConnectorConfiguration(new FileSystemConnectorConfiguration("files", fsBaseDir));
    return configuration;
  }
  
  //-- end of copy --
  
  public void saveConfiguration(ConfigurationContainer container) {
    createAndInsert(container, container.getName());
  }

  public ConfigurationContainer getConfiguration(String name) {
    CycleConfig cycleConfig = selectById(name);
    Object configXML = getXStream().fromXML(cycleConfig.getConfigXML());
    return (ConfigurationContainer) configXML;
  }
  
  /**
   *
   * Should be the parameter connectorId and artifactId or is the sourceArtifactId parameter composed of
   * connectorId and artifactId??
   * If yes, how can I split the sourceArtifactId in connectorId and artifactId? Only with String-Operation? 
   */
  @Override
  public void addArtifactLink(String sourceArtifactId, String targetArtifactId) {
    SqlSessionFactory sqlMapper = getSessionFactory();
    
    CycleLink link = new CycleLink();
//    link.setSourceConnectorId(sourceConnectorId);
//    link.setTargetConnectorId(targetConnectorId);
    link.setSourceArtifactId(sourceArtifactId);
    link.setTargetArtifactId(targetArtifactId);
    
    SqlSession session = sqlMapper.openSession();
    
    session.insert("org.activiti.cycle.CycleLink.insertCycleLink", link);
    session.commit();
  }
  
  /**
   * Should be the parameter connectorId and artifactId or is the sourceArtifactId parameter composed of
   * connectorId and artifactId??
   * If yes, how can I split the sourceArtifactId in connectorId and artifactId? Only with String-Operation? 
   */
  @Override
  public List<RepositoryArtifactLink> getArtifactLinks(String sourceArtifactId) {
    
    List<RepositoryArtifactLink> repositoryArtifactLinkList = new ArrayList<RepositoryArtifactLink>();
    SqlSessionFactory sqlMapper = getSessionFactory();
    
    SqlSession session = sqlMapper.openSession();
    
    try {
      
      List<CycleLink> linkResultList = session.selectList(
              "org.activiti.cycle.CycleLink.selectArtifactLinkForSourceArtifact", sourceArtifactId);
      
      if (linkResultList != null && !linkResultList.isEmpty()) {
         for (CycleLink cycleLink : linkResultList) {
              RepositoryArtifactLink repositoryArtifactLink = new RepositoryArtifactLinkImpl();
              repositoryArtifactLink.setId(cycleLink.getId());
              repositoryArtifactLink.setSourceElementId(cycleLink.getSourceElementId());
              repositoryArtifactLink.setTargetElementId(cycleLink.getTargetElementId());
              repositoryArtifactLink.setSourceElementName(cycleLink.getSourceElementName());
              repositoryArtifactLink.setTargetElementName(cycleLink.getTargetElementName());
              repositoryArtifactLink.setSourceRevision(cycleLink.getSourceRevision());
              repositoryArtifactLink.setTargetRevision(cycleLink.getTargetRevision());
              repositoryArtifactLink.setDescription(cycleLink.getDescription());
              repositoryArtifactLink.setLinkType(cycleLink.getLinkType());
              repositoryArtifactLink.setLinkedBothWays(cycleLink.isLinkedBothWays());
              
              //TODO: How can I get the connector to create a new RepositoryArtifact?
//              RepositoryArtifact sourceRepositoryArtifact = new RepositoryArtifactLinkImpl(cycleLink.getSourceConnectorId(), cycleLink.getSourceArtifactId(),  );
//              RepositoryArtifact targetRepositoryArtifact = new RepositoryArtifactLinkImpl(cycleLink.getTargetConnectorId(), cycleLink.getTargetArtifactId(),  );
//              repositoryArtifactLink.setSourceArtifactId(sourceRepositoryArtifact);
//              repositoryArtifactLink.setTargetArtifactId(targetRepositoryArtifact);
              
              repositoryArtifactLinkList.add(repositoryArtifactLink);
        }
      }
      
      return repositoryArtifactLinkList;
    } finally {
      session.close();
    }
  }
  
//  @Override
//  public void addArtifactLink(String sourceArtifactId, String targetArtifactId) {
//    Artifact cycleLink = new Artifact();
//    CycleLink cycleLinkTarget = new CycleLink();
//    cycleLinkTarget.setTargetArtifactId(targetArtifactId);
//
//    cycleLink.setSourceArtifactId(sourceArtifactId);
//    cycleLinkTarget.setCycleLink(cycleLink);
//    
//    SqlSessionFactory sqlMapper = getSessionFactory();
//    
//    SqlSession session = sqlMapper.openSession();
//    try {
//      session.insert("org.activiti.cycle.CycleLink.insertCycleLink", cycleLink);
//      session.insert("org.activiti.cycle.CycleLinkTarget.insertCycleLinkTarget", cycleLinkTarget);
//      session.commit();
//    } finally {
//      session.close();
//    }
//  }
//  
//  @Override
//  public void addLink(Artifact link) {
//    SqlSessionFactory sqlMapper = getSessionFactory();
//    
//    SqlSession session = sqlMapper.openSession();
//    try {
//      session.insert("org.activiti.cycle.CycleLink.insertCycleLink", link);    
//      session.commit();
//    } finally {
//      session.close();
//    }
//  }
//  
//  @Override
//  public List<Artifact> getArtifactLinks(String sourceArtifactId) {
//    SqlSessionFactory sqlMapper = getSessionFactory();
//    
//    SqlSession session = sqlMapper.openSession();
//    try {
//      
//      List<Artifact> linkList = (List<Artifact>) session.selectList(
//              "org.activiti.cycle.CycleLink.selectCycleLinkBySourceArtifactId", sourceArtifactId);
//      Artifact link = linkList.get(0);
//      return linkList;
//    } finally {
//      session.close();
//    }
//  }
//  
//  @Override
//  public List<Artifact> getArtifactLinks(String sourceArtifactId, String type) {
//    Artifact cycleLink = new Artifact();
//    CycleLink cycleLinkTarget = new CycleLink();
//    List<CycleLink> cycleLinkTargetList = new ArrayList<CycleLink>();
//    
//    cycleLinkTarget.setLinkType(type);
//    cycleLinkTargetList.add(cycleLinkTarget);
//    
//    cycleLink.setSourceArtifactId(sourceArtifactId);
//    cycleLink.setCycleLinkTarget(cycleLinkTargetList);
//    
//    SqlSessionFactory sqlMapper = getSessionFactory();
//    
//    SqlSession session = sqlMapper.openSession();
//    try {
//      return (List<Artifact>) session.selectList(
//              "org.activiti.cycle.CycleLink.selectCycleLinkBySourceArtifactIdAndType", cycleLink);
//
//    } finally {
//      session.close();
//    }
//  }
//  
//  @Override
//  public List<Artifact> getArtifactLinks(String sourceArtifactId, Long sourceRevision) {
//    Artifact cycleLink = new Artifact();
//    cycleLink.setSourceArtifactId(sourceArtifactId);
//    cycleLink.setSourceRevision(sourceRevision);
//    
//    SqlSessionFactory sqlMapper = getSessionFactory();
//    
//    SqlSession session = sqlMapper.openSession();
//    try {
//      return (List<Artifact>) session.selectList(
//              "org.activiti.cycle.CycleLink.selectCycleLinkBySourceArtifactIdAndSourceRevision", cycleLink);
//
//    } finally {
//      session.close();
//    }
//    
//  }
//
//  @Override
//  public List<Artifact> getArtifactLinks(String sourceArtifactId, Long sourceRevision, String type) {
//    Artifact cycleLink = new Artifact();
//    CycleLink cycleLinkTarget = new CycleLink();
//    List<CycleLink> cycleLinkTargetList = new ArrayList<CycleLink>();
//    
//    cycleLinkTarget.setLinkType(type);
//    cycleLinkTargetList.add(cycleLinkTarget);
//    
//    cycleLink.setSourceArtifactId(sourceArtifactId);
//    cycleLink.setSourceRevision(sourceRevision);
//    cycleLink.setCycleLinkTarget(cycleLinkTargetList);
//    
//    SqlSessionFactory sqlMapper = getSessionFactory();
//    
//    SqlSession session = sqlMapper.openSession();
//    try {
//      return (List<Artifact>) session.selectList(
//              "org.activiti.cycle.CycleLink.selectCycleLinkBySourceArtifactIdAndTypeAndSourceRevision", cycleLink);
//
//    } finally {
//      session.close();
//    }
//
//  }
//  
//  
//  @Override
//  public void deleteLink(long linkId) {
//    SqlSessionFactory sqlMapper = getSessionFactory();
//    
//    SqlSession session = sqlMapper.openSession();
//    try {
//      session.insert("org.activiti.cycle.CycleLink.deleteCycleLink", linkId);    
//      session.commit();
//    } finally {
//      session.close();
//    }
//  }

  //----- start implementation for cycle persistence -----
  
  public ProcessEngineConfiguration getProcessEngineConfiguration() {
    if (DEFAULT_ENGINE.equals(processEngineName)) {
      return ((ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine()).getProcessEngineConfiguration();
    } else {
      return ((ProcessEngineImpl) ProcessEngines.getProcessEngine(processEngineName)).getProcessEngineConfiguration();
    }
  }
  
  public CycleConfig selectById(String id) {
    SqlSessionFactory sqlMapper = getSessionFactory();
    
    SqlSession session = sqlMapper.openSession();
    try {
      return (CycleConfig) session.selectOne(
              "org.activiti.cycle.CycleConfig.selectCycleConfigById", id);

    } finally {
      session.close();
    }
  }
  
  public void createAndInsert(Object o, String id) {
    CycleConfig cycleConfig = new CycleConfig();
    cycleConfig.setId(id);
    String configXML = getXStream().toXML(o);
    cycleConfig.setConfigXML(configXML);
    
    SqlSessionFactory sqlMapper = getSessionFactory();
    
    SqlSession session = sqlMapper.openSession();
    try {
      session.insert("org.activiti.cycle.CycleConfig.insertCycleConfig", cycleConfig);    
      session.commit();
    } finally {
      session.close();
    }
  }
  
  public void updateById(CycleConfig cycleConfig) {
    SqlSessionFactory sqlMapper = getSessionFactory();
    
    SqlSession session = sqlMapper.openSession();
    try {
      session.update(
              "org.activiti.cycle.CycleConfig.updateCycleConfigById", cycleConfig);
      session.commit();
    } finally {
      session.close();
    }
  }
  
  public void deleteById(String id) {
    SqlSessionFactory sqlMapper = getSessionFactory();
    
    SqlSession session = sqlMapper.openSession();
    try {
      session.delete(
              "org.activiti.cycle.CycleConfig.deleteCycleConfigById", id);
      session.commit();
    } finally {
      session.close();
    }
  }

  private void performDbSchemaCreation(CycleDbSqlSessionFactory dbSqlSessionFactory, ProcessEngineConfiguration processEngineConfiguration) {    
    String dbSchemaStrategy = processEngineConfiguration.getDbSchemaStrategy();
    
    if (ProcessEngineConfiguration.DBSCHEMASTRATEGY_DROP_CREATE.equals(dbSchemaStrategy)) {
      try {
        dbSqlSessionFactory.dbSchemaDrop();
      } catch (RuntimeException e) {
        // ignore
      }
    }
    
    if ( DbSchemaStrategy.CREATE_DROP.equals(dbSchemaStrategy) 
         || ProcessEngineConfiguration.DBSCHEMASTRATEGY_DROP_CREATE.equals(dbSchemaStrategy)
         || ProcessEngineConfiguration.DBSCHEMASTRATEGY_CREATE.equals(dbSchemaStrategy)
       ) {
      dbSqlSessionFactory.dbSchemaCreate();
      
    } else if (DbSchemaStrategy.CHECK_VERSION.equals(dbSchemaStrategy)) {
      dbSqlSessionFactory.dbSchemaCheckVersion();
    }    
  }

}
