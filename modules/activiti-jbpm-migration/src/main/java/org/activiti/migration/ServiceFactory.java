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
package org.activiti.migration;

import java.io.StringReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.migration.dao.Jbpm3Dao;
import org.activiti.migration.dao.Jbpm3DaoImpl;
import org.activiti.migration.service.ActivitiService;
import org.activiti.migration.service.ActivitiServiceImpl;
import org.activiti.migration.service.ProcessConversionService;
import org.activiti.migration.service.ProcessConversionServiceImpl;
import org.activiti.migration.service.XmlTransformationService;
import org.activiti.migration.service.XmlTransformationServiceImpl;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


/**
 * TODO: two ways on constructing it
 * 
 * @author Joram Barrez
 */
public class ServiceFactory {
  
  protected static final Logger LOGGER = Logger.getLogger(ServiceFactory.class.getName());
  
  // jBPM db parameters
  protected String jbpm3DbDriver;
  protected String jbpm3DbHibernateDialect;
  protected String jbpm3DbUrl;
  protected String jbpm3DbUser;
  protected String jbpm3DbPassword;
  
  // Activiti db parameters
  protected String activitiDbDriver;
  protected String activitiDbUrl;
  protected String activitiDbUser;
  protected String activitiDbPassword;
  protected String activitiDbType;
  
  // Dependencies
  protected ProcessConversionService processConversionService;
  protected ActivitiService activitiService;
  protected ProcessEngine processEngine;
  protected Jbpm3Dao jbpm3Dao;
  protected SessionFactory sessionFactory;
  protected XmlTransformationService xmlTransformationService;
  
  
  public static ServiceFactory configureFromProperties(Properties jbpm3DbParameters, Properties activitiDbParameters) {
    
    ServiceFactory serviceFactory = new ServiceFactory();
    
    // jbpm 3
    serviceFactory.setJbpm3DbDriver(jbpm3DbParameters.getProperty("driver"));
    serviceFactory.setJbpm3DbUrl(jbpm3DbParameters.getProperty("url"));
    serviceFactory.setJbpm3DbUser(jbpm3DbParameters.getProperty("username"));
    serviceFactory.setJbpm3DbPassword(jbpm3DbParameters.getProperty("password"));
    serviceFactory.setJbpm3DbHibernateDialect(jbpm3DbParameters.getProperty("hibernate.dialect"));
     
    // activiti
    serviceFactory.setActivitiDbDriver(activitiDbParameters.getProperty("driver"));
    serviceFactory.setActivitiDbUrl(activitiDbParameters.getProperty("url"));
    serviceFactory.setActivitiDbUser(activitiDbParameters.getProperty("username"));
    serviceFactory.setActivitiDbPassword(activitiDbParameters.getProperty("password"));
    serviceFactory.setActivitiDbType(activitiDbParameters.getProperty("type"));
    
    serviceFactory.validateDbParameters();
    
    return serviceFactory;
  }
  
  protected void validateDbParameters() {
    
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info("Validating jBPM 3 and Activiti database parameters");
    }
    
    // jbpm db
    if (jbpm3DbDriver == null) {
      throw new ActivitiException("Invalid configuration : jbpm 3 database driver not set");
    }
    if (jbpm3DbHibernateDialect == null) {
      throw new ActivitiException("Invalid configuration : jbpm 3 hibernate dialect not set");
    }
    if (jbpm3DbUrl == null) {
      throw new ActivitiException("Invalid configuration : jbpm 3 database url not set");
    }
    if (jbpm3DbUser == null) {
      throw new ActivitiException("Invalid configuration : jbpm 3 database username not set");
    }
    if (jbpm3DbPassword == null) {
      throw new ActivitiException("Invalid configuration : jbpm 3 database password not set");
    }
    
    // activiti db
    if (activitiDbDriver == null) {
      throw new ActivitiException("Invalid configuration: Activiti database driver not set");
    }
    if (activitiDbUrl == null) {
      throw new ActivitiException("Invalid configuration: Activiti database url not set");
    }
    if (activitiDbUser == null) {
      throw new ActivitiException("Invalid configuration: Activiti database username not set");
    }
    if (activitiDbPassword == null) {
      throw new ActivitiException("Invalid configuration: Activiti database password not set");
    }
    
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info("Validation complete: all DB parameters set");
    }
  }
  
  // Getters and setters //////////////////////////////////////////////////////////////////////////
  
  public ProcessConversionService getProcessConversionService() {
    if (processConversionService == null) {
      this.processConversionService = createDefaultProcessConversionService();
    } 
    return processConversionService;
  }
  
  protected ProcessConversionService createDefaultProcessConversionService() {
    ProcessConversionServiceImpl service = new ProcessConversionServiceImpl(getJbpm3Dao());
    return service;
  }
  
  protected Jbpm3Dao createDefaultJbpm3Dao() {
    Jbpm3DaoImpl jbpm3Dao = new Jbpm3DaoImpl();
    jbpm3Dao.setSessionFactory(getSessionFactory());
    return jbpm3Dao;
  }
  
  protected SessionFactory createDefaultSessionFactory() {
   
    String hibernateConfigXml = new String(IoUtil.readInputStream(
            this.getClass().getClassLoader().getResourceAsStream("hibernate.cfg.xml"), null));
    
    // replace placeholders in hibernate.cfg.xml with provided values
    hibernateConfigXml = hibernateConfigXml.replace("@jbpm.db.driver.class@", jbpm3DbDriver);
    hibernateConfigXml = hibernateConfigXml.replace("@jbpm.db.hibernate.dialect@", jbpm3DbHibernateDialect);
    hibernateConfigXml = hibernateConfigXml.replace("@jbpm.db.url@", jbpm3DbUrl);
    hibernateConfigXml = hibernateConfigXml.replace("@jbpm.db.username@", jbpm3DbUser);
    hibernateConfigXml = hibernateConfigXml.replace("@jbpm.db.password@", jbpm3DbPassword);
    
    Document document = null;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(hibernateConfigXml));
      document = builder.parse(is);
    } catch (Exception e) {
      throw new ActivitiException("Could not construct the Hibernate session factory", e);
    }
    
    return new Configuration().configure(document).buildSessionFactory();
  }
  
  public ActivitiService getActivitiService() {
    if (activitiService == null) {
      this.activitiService = createDefaultActivitiService();
    }
    return activitiService;
  }
  
  public Jbpm3Dao getJbpm3Dao() {
    if (jbpm3Dao == null) {
      this.jbpm3Dao = createDefaultJbpm3Dao();
    }
    return jbpm3Dao;
  }
  
  public SessionFactory getSessionFactory() {
    if (sessionFactory == null) {
      this.sessionFactory = createDefaultSessionFactory();
    }
    return sessionFactory;
  }
  
  public ProcessEngine getProcessEngine() {
    if (processEngine == null) {
      this.processEngine = createDefaultProcessEngine();
    }
    return processEngine;
  }
  
  public ActivitiService createDefaultActivitiService() {
    ActivitiServiceImpl activitiServiceImpl = new ActivitiServiceImpl();
    activitiServiceImpl.setProcessEngine(getProcessEngine());
    activitiServiceImpl.setXmlTransformationService(xmlTransformationService);
    return activitiServiceImpl;
  }
  
  public ProcessEngine createDefaultProcessEngine() {
    return StandaloneProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
      .setDatabaseType(activitiDbType)
      .setJdbcDriver(activitiDbDriver)
      .setJdbcUrl(activitiDbUrl)
      .setJdbcUsername(activitiDbUser)
      .setJdbcPassword(activitiDbPassword)
      .setDatabaseSchemaUpdate("true")
      .buildProcessEngine();
  }
  
  public XmlTransformationService getXmlTransformationService() {
    if (xmlTransformationService == null) {
      this.xmlTransformationService = createDefaultXmlTransformationService();
    }
    return xmlTransformationService;
  }
  
  protected XmlTransformationService createDefaultXmlTransformationService() {
    XmlTransformationServiceImpl xmlTransformationService = new XmlTransformationServiceImpl();
    return xmlTransformationService;
  }
  
  // Dependency setters
  
  public void setProcessConversionService(ProcessConversionService processConversionService) {
    this.processConversionService = processConversionService;
  }
  public void setActivitiService(ActivitiService activitiService) {
    this.activitiService = activitiService;
  }
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }
  public void setJbpm3Dao(Jbpm3Dao jbpm3Dao) {
    this.jbpm3Dao = jbpm3Dao;
  }
  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }
  public void setXmlTransformationService(XmlTransformationService xmlTransformationService) {
    this.xmlTransformationService = xmlTransformationService;
  }
  
  // Regular setters and getters ////////////////////////////////////////////////////////////////
  
  public void setJbpm3DbParameters(String jbpm3DbDriver, String jbpm3DbUrl, 
          String jbpm3DbUser, String jbpm3DbPassword, String jbpm3HibernateDialect) {
    setJbpm3DbDriver(jbpm3DbDriver);
    setJbpm3DbUrl(jbpm3DbUrl);
    setJbpm3DbUser(jbpm3DbUser);
    setJbpm3DbPassword(jbpm3DbPassword);
    setJbpm3DbHibernateDialect(jbpm3HibernateDialect);
  }
  public void setActivitiDbParameters(String activitiDbType, String ActivitiDbDriver, 
          String ActivitiDbUrl, String ActivitiDbUser, String ActivitiDbPassword) {
    setActivitiDbType(activitiDbType);
    setActivitiDbDriver(ActivitiDbDriver);
    setActivitiDbUrl(ActivitiDbUrl);
    setActivitiDbUser(ActivitiDbUser);
    setActivitiDbPassword(ActivitiDbPassword);
  }

  public String getJbpm3DbDriver() {
    return jbpm3DbDriver;
  }
  public void setJbpm3DbDriver(String jbpm3DbDriver) {
    this.jbpm3DbDriver = jbpm3DbDriver;
  }
  public String getJbpm3DbHibernateDialect() {
    return jbpm3DbHibernateDialect;
  }
  public void setJbpm3DbHibernateDialect(String jbpm3DbHibernateDialect) {
    this.jbpm3DbHibernateDialect = jbpm3DbHibernateDialect;
  }
  public String getJbpm3DbUrl() {
    return jbpm3DbUrl;
  }
  public void setJbpm3DbUrl(String jbpm3DbUrl) {
    this.jbpm3DbUrl = jbpm3DbUrl;
  }
  public String getJbpm3DbUser() {
    return jbpm3DbUser;
  }
  public void setJbpm3DbUser(String jbpm3DbUser) {
    this.jbpm3DbUser = jbpm3DbUser;
  }
  public String getJbpm3DbPassword() {
    return jbpm3DbPassword;
  }
  public void setJbpm3DbPassword(String jbpm3DbPassword) {
    this.jbpm3DbPassword = jbpm3DbPassword;
  }
  public String getActivitiDbDriver() {
    return activitiDbDriver;
  }
  public void setActivitiDbDriver(String activitiDbDriver) {
    this.activitiDbDriver = activitiDbDriver;
  }
  public String getActivitiDbUrl() {
    return activitiDbUrl;
  }
  public void setActivitiDbUrl(String activitiDbUrl) {
    this.activitiDbUrl = activitiDbUrl;
  }
  public String getActivitiDbUser() {
    return activitiDbUser;
  }
  public void setActivitiDbUser(String activitiDbUser) {
    this.activitiDbUser = activitiDbUser;
  }
  public String getActivitiDbPassword() {
    return activitiDbPassword;
  }
  public void setActivitiDbPassword(String activitiDbPassword) {
    this.activitiDbPassword = activitiDbPassword;
  }
  public String getActivitiDbType() {
    return activitiDbType;
  }
  public void setActivitiDbType(String activitiDbType) {
    this.activitiDbType = activitiDbType;
  }
  
}
