package org.activiti.cdi.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;

/**
 * @author daniel.meyer@camunda.com
 */
public class CdiTestUtils {

  private static Logger logger = Logger.getLogger(CdiTestUtils.class.getName());

  public static String loadResourceAsString(String resourceName, Class clazz) {
    BufferedReader reader = null;
    try {
      InputStream is = clazz.getResourceAsStream(resourceName);
      reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      StringWriter resultWriter = new StringWriter();
      String line;
      while ((line = reader.readLine()) != null) {
        resultWriter.append(line + "\n");
      }
      reader.close();
      return resultWriter.toString();
    } catch (IOException e) {     
      try {
        reader.close();
      } catch (IOException ex) {

      }
      return null;
    }
  }
  
  public static boolean deleteFileRec(File path) {
    if (path.exists() && path.isAbsolute()) {
      File[] files = path.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          deleteFileRec(files[i]);
        } else {
          files[i].delete();
        }
      }
    }
    return path.delete();
  }
  
  @SuppressWarnings({"unchecked" })
  protected static <T extends AbstractCdiTestEnvironment> T loadCycleTestEnvironment(Class<T> defaultTestEnvironmentClass) {
    Properties environmentProperties = null;
    try {
      environmentProperties = loadProperties("environment.properties");
    }catch (RuntimeException e) {
      logger.info("Could not load 'environment.properties' file, falling back to default environment properties.");
      environmentProperties = new Properties();
    }
    String environmentClassName = (String) environmentProperties.get("cycle.test.environment.class");
    if(environmentClassName == null) {
      environmentClassName = defaultTestEnvironmentClass.getName();
      logger.info("No cycle.test.environment.class specified, falling back to default environment: '"+environmentClassName+"'.");      
    }
    try {      
      Class<T> environmentClass = (Class<T>) CdiPluginTestCase.class.getClassLoader().loadClass(environmentClassName);
      T testEnvironment = (T) environmentClass.newInstance();
      return testEnvironment;
    }catch (ClassNotFoundException e) {
      throw new ActivitiException("Could not find specified cycle test environment class: "+e.getMessage(), e);
    }catch (Exception e) {
      throw new ActivitiException("Error while setting up cycle test enviroment: "+e.getMessage(), e);
    }
  }
  
  protected static Properties loadProperties(String propertiesFile) {
    InputStream is = null;
    try {
      is = CdiTestUtils.class.getClassLoader().getResourceAsStream(propertiesFile);
      Properties properties = new Properties();
      properties.load(is);
      return properties;
    } catch (Exception e) {
      throw new RuntimeException("Could not load properties from file '" + propertiesFile + "'", e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // nop
        }
      }
    }
  }
}
