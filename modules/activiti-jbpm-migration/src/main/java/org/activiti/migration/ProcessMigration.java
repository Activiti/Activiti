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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.LogUtil;
import org.activiti.migration.util.XmlUtil;
import org.w3c.dom.Document;


/**
 * @author Joram Barrez
 */
public class ProcessMigration {
  
  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }
  
  protected static final Logger LOGGER = Logger.getLogger(ProcessConversion.class.getName());
  protected static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
  
  protected String workingDir;
  
  public ProcessMigration(String workingDir) {
    this.workingDir = workingDir;
  }
  
  public void execute() throws IOException {
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info("Process migration started. Working directory = '" + workingDir + "'");
    }
    
    // convert processes and write them out
    Jbpm3ToActivitiMigrator migrator = createMigrator();
    Map<String, Document> migratedProcesses = migrator.convertProcesses();
    writeConvertedProcesses(migratedProcesses, workingDir);
    
    // Deploy processes to Activiti
    migrator.deployConvertedProcessesToActiviti();
    
    // TODO: data migration
  }
  
  protected Jbpm3ToActivitiMigrator createMigrator() throws IOException {
    Jbpm3ToActivitiMigrator migrator = new Jbpm3ToActivitiMigrator();
    Properties jbpmDbProperties = loadProperties(workingDir + "/jbpm3.db.properties", true);
    Properties activitiDbProperties = loadProperties(workingDir + "/activiti.db.properties", false);
    migrator.configureFromProperties(jbpmDbProperties, activitiDbProperties);
    return migrator;
  }
  
  protected void writeConvertedProcesses(Map<String, Document> migratedProcesses, String workingDir) {
    File outputDir = new File(workingDir + "/converted-processes-" + DATE_FORMATTER.format(new Date()));
    outputDir.mkdir();
    for (String processName : migratedProcesses.keySet()) {
      String bpmnFileName = processName.replace(" ", "_") + ".bpmn20.xml";
      writeProcessToFile(XmlUtil.toString(migratedProcesses.get(processName)), outputDir.getAbsolutePath() + "/" + bpmnFileName);
    }
  }

  protected Properties loadProperties(String path, boolean required) throws IOException{
    FileInputStream is = new FileInputStream(new File(path));
    try {
      Properties properties = new Properties();
      properties.load(is);
      return properties;
    } finally {
      IoUtil.closeSilently(is);
    }
  }
  
  public void writeProcessToFile(String content, String filePath) {
    
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info("Writing converted process to " + filePath);
    }
    
    BufferedOutputStream outputStream = null;
    try {
      outputStream = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
      outputStream.write(content.getBytes());
      outputStream.flush();
    } catch(Exception e) {
      throw new ActivitiException("Couldn't write file " + filePath, e);
    } finally {
      IoUtil.closeSilently(outputStream);
    }
  }
  
  public static void main(String[] args) throws Exception {
    validateParameters(args);
    ProcessMigration migration = new ProcessMigration(args[0]);
    migration.execute();
  }
  
  protected static void validateParameters(String[] args) {
    if (args.length != 1) {
      throw new ActivitiException("Invalid number of arguments passed to " 
              + ProcessConversion.class.getName());
    }
   }

}
