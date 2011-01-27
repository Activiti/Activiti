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

package org.activiti.migration.service;

import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.w3c.dom.Document;

/**
 * @author Joram Barrez
 */
public class ActivitiServiceImpl implements ActivitiService {

  protected ProcessEngine processEngine;
  protected XmlTransformationService xmlTransformationService;

  public void deployConvertedProcesses(Map<String, Document> migratedProcesses) {
    for (String processName : migratedProcesses.keySet()) {
      processEngine.getRepositoryService().createDeployment()
      .addString(processName.replace(" ", "_") + ".bpmn20.xml", 
            xmlTransformationService.convertToString(migratedProcesses.get(processName))).deploy();
    }
    processEngine.close();
  }

  
  public ProcessEngine getProcessEngine() {
    return processEngine;
  }
  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }
  public XmlTransformationService getXmlTransformationService() {
    return xmlTransformationService;
  }
  public void setXmlTransformationService(XmlTransformationService xmlTransformationService) {
    this.xmlTransformationService = xmlTransformationService;
  }
  
}
