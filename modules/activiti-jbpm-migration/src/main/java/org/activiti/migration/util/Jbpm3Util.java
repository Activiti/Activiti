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
package org.activiti.migration.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.util.IoUtil;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;


/**
 * @author Joram Barrez
 */
public class Jbpm3Util {
  
  protected static final Logger LOGGER = Logger.getLogger(Jbpm3Util.class.getName());
  
  public static Map<String, JbpmConfiguration> JBPM_CONFIGS = new HashMap<String, JbpmConfiguration>();
  
  public static JbpmConfiguration getJbpmConfiguration(String jbpmCfgResource) {
    if (JBPM_CONFIGS.get(jbpmCfgResource) == null) {
      JbpmConfiguration config = JbpmConfiguration.getInstance(jbpmCfgResource);
      JBPM_CONFIGS.put(jbpmCfgResource, config);
    }
    return JBPM_CONFIGS.get(jbpmCfgResource);
  }
  
  public static String deployJbpmProcess(JbpmContext jbpmContext, String packageName) {
    Map<String, byte[]> files = new HashMap<String, byte[]>();
    
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info("Deploying resources in " + packageName);
    }
      
    // process definition
    String processDefinitionPath = packageName + "/processdefinition.xml";
    files.put("processdefinition.xml", readProcessResource(processDefinitionPath, true));
    
    // gpd.xml
    String gpdPath = packageName + "/gpd.xml";
    byte[] gpdBytes = readProcessResource(gpdPath, false);
    if (gpdBytes != null) {
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("gpd.xml available in " + packageName);
      }
      files.put("gpd.xml", gpdBytes);
    }
      
    // process image
    String imagePath = packageName + "/processimage.jpg";
    byte[] imageBytes = readProcessResource(imagePath, false);
    if (imageBytes != null) {
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("processimage.jpg available in " + packageName);
      }
      files.put("processimage.jpg", imageBytes);
    }
      
    try {
      ProcessDefinition processDefinition = 
        ProcessDefinition.parseParZipInputStream(ZipUtil.createZipInputStream(files));
      jbpmContext.deployProcessDefinition(processDefinition);
      return processDefinition.getName();
    } catch (IOException e) {
      throw new ActivitiException("Couldn't create zip file", e);
    }
      
  }
  
  protected static byte[] readProcessResource(String path, boolean throwErrorOnMissing) {
    InputStream is = null;
    try {
      is = new FileInputStream(new File(path));
      byte[] resource = IoUtil.readInputStream(is, path);
      IoUtil.closeSilently(is);
      return resource;
    } catch (FileNotFoundException e) {
      if (throwErrorOnMissing) {
        throw new ActivitiException("Can't read " + path , e);
      }
      return null;
    } 
  }

}
