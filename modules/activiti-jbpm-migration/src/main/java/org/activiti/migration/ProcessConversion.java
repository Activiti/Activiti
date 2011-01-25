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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.activiti.engine.ActivitiException;
import org.activiti.migration.util.Jbpm3Util;
import org.jbpm.JbpmContext;
import org.w3c.dom.Document;


/**
 * @author Joram Barrez
 */
public class ProcessConversion extends ProcessMigration {
  
  protected static final String JBPM_IN_MEM_CFG = "jbpm.in-mem.cfg.xml";
  protected static final String ACTIVITI_IN_MEM_DB_PROPS = "activiti.db.in-mem.properties";
  protected static final String JBPM3_IN_MEM_DB_PROPS = "jbpm3.db.in-mem.properties";
  
  public ProcessConversion(String workingDir) {
    super(workingDir);
  }
  
  public void execute() throws IOException {
    searchAndDeployProcessesToInMemDatabase(new File(workingDir + "/processes"));
    
    Jbpm3ToActivitiMigrator migrator = createMigrator();
    Map<String, Document> migratedProcesses = migrator.convertProcesses();
    writeConvertedProcesses(migratedProcesses, workingDir);
  }
  
   @Override
  protected Jbpm3ToActivitiMigrator createMigrator() throws IOException {
     Jbpm3ToActivitiMigrator migrator = new Jbpm3ToActivitiMigrator();
     Properties jbpmDbProperties = loadProperties(JBPM3_IN_MEM_DB_PROPS, true); // This is the in-memory db properties file INSIDE the jar
     Properties activitiDbProperties = loadProperties(ACTIVITI_IN_MEM_DB_PROPS, false); // This is the in-memory db properties file INSIDE the jar
     migrator.configureFromProperties(jbpmDbProperties, activitiDbProperties);
     return migrator;
  }
   
  // properties are loaded from classpath for conversion
  protected Properties loadProperties(String path, boolean required) throws IOException {
    InputStream is = ProcessConversion.class.getClassLoader().getResourceAsStream(path);
    if (is == null) {
      if (required) {
        throw new ActivitiException("Couldn't find " + path);
      } else {
        return null;
      }
    } else {
      try {
        Properties properties = new Properties();
        properties.load(is);
        return properties;
      } finally {
        is.close();
      }
    }
  }

  protected void searchAndDeployProcessesToInMemDatabase(File directory) {

    if (directory == null || !directory.exists()) {
      throw new ActivitiException("Provided directory does not exist");
    }
    if (!directory.isDirectory()) {
      throw new ActivitiException(directory.getAbsolutePath() + " is not a folder");
    }
    
    File processDefinitionFile = new File(directory.getAbsolutePath() + "/processdefinition.xml");
    if (processDefinitionFile.exists()) {
      
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("Process definition found in " + directory.getAbsolutePath());
      }
      
      JbpmContext context = Jbpm3Util.getJbpmConfiguration(JBPM_IN_MEM_CFG).createJbpmContext();
      try {
        Jbpm3Util.deployJbpmProcess(context, directory.getAbsolutePath());
      } finally {
        context.close();
      }
    } else {
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("No process definition found in " + directory.getAbsolutePath());
      }
    }
    
    File[] subDirectories = directory.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    });
    
    if (subDirectories.length > 0) {
      for (File subDirectory : subDirectories) {
        searchAndDeployProcessesToInMemDatabase(subDirectory);
      }
    }
  }
  
  public static void main(String[] args) throws Exception {
    validateParameters(args);
    ProcessConversion processConversion = new ProcessConversion(args[0]);
    processConversion.execute();
  }
  
  protected static void validateParameters(String[] args) {
    if (args.length != 1) {
      throw new ActivitiException("Invalid number of arguments passed to " 
              + ProcessConversion.class.getName());
    }
   }
  
}
