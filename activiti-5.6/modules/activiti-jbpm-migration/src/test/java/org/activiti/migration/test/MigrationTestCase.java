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
package org.activiti.migration.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.migration.ServiceFactory;
import org.activiti.migration.service.ProcessConversionService;
import org.activiti.migration.service.XmlTransformationService;
import org.activiti.migration.util.Jbpm3Util;
import org.activiti.migration.util.ZipUtil;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.w3c.dom.Document;


/**
 * @author Joram Barrez
 */
public class MigrationTestCase extends PluggableActivitiTestCase {
  
  protected JbpmConfiguration jbpmConfiguration;

  protected ServiceFactory serviceFactory;
  protected ProcessConversionService processConversionService;
  protected XmlTransformationService xmlTransformationService;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.jbpmConfiguration = Jbpm3Util.getJbpmConfiguration("jbpm.in-mem.cfg.xml");
    this.serviceFactory = createServiceFactory();
    this.processConversionService = serviceFactory.getProcessConversionService();
    this.xmlTransformationService = serviceFactory.getXmlTransformationService();
  }
  
  @Override
  protected void tearDown() throws Exception {
    this.serviceFactory = null;
    super.tearDown();
  }
  
  protected void deployJbpmProcess(String packageName) {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      Map<String, byte[]> files = new HashMap<String, byte[]>();
      
      // process definition
      String processDefinitionPath = packageName + "/processdefinition.xml";
      files.put("processdefinition.xml", readProcessResource(processDefinitionPath, true));
      
      // gpd.xml
      String gpdPath = packageName + "/gpd.xml";
      byte[] gpdBytes = readProcessResource(gpdPath, false);
      if (gpdBytes != null) {
        files.put("gpd.xml", gpdBytes);
      }
        
      // process image
      String imagePath = packageName + "/processimage.jpg";
      byte[] imageBytes = readProcessResource(imagePath, false);
      if (imageBytes != null) {
        files.put("processimage.jpg", imageBytes);
      }
        
      try {
        ProcessDefinition processDefinition = 
          ProcessDefinition.parseParZipInputStream(ZipUtil.createZipInputStream(files));
        jbpmContext.deployProcessDefinition(processDefinition);
      } catch (IOException e) {
        throw new ActivitiException("Couldn't create zip file", e);
      }
    } finally {
      jbpmContext.close();
    }
  }
  
  protected byte[] readProcessResource(String path, boolean throwErrorOnMissing) {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);    
    if (is != null) {
      byte[] resource = IoUtil.readInputStream(is, path);
      IoUtil.closeSilently(is);
      return resource;
    } else {
      if (throwErrorOnMissing) {
        throw new ActivitiException("Can't find " + path);
      }
      return null;
    }
  }
  
  protected ServiceFactory createServiceFactory() throws IOException {
    Properties jbpm3DbProperties = new Properties();
    jbpm3DbProperties.load(this.getClass().getClassLoader().getResourceAsStream("jbpm3.db.in-mem.properties"));

    Properties activitiDbProperties = new Properties();
    activitiDbProperties.load(this.getClass().getClassLoader().getResourceAsStream("activiti.db.in-mem.properties"));
    
    return ServiceFactory.configureFromProperties(jbpm3DbProperties, activitiDbProperties);
  }
  
  protected String getConvertedProcess(String processName) {
    Map<String, Document> migratedProcessDefinitions = processConversionService.convertAllProcessDefinitions();
    return xmlTransformationService.convertToString(migratedProcessDefinitions.get(processName));
  }
  
}
