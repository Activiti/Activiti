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
package org.activiti.cdi.impl;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Used to deploy processes. When the activiti-cdi extension is initialized, the
 * classpath is scanned for a file named {@value #PROCESSES_FILE_NAME}. All
 * processes listed in that file are automatically deployed to the engine.
 * 
 * @author Daniel Meyer
 */
public class ProcessDeployer {

  public final static String PROCESSES_FILE_NAME = "processes.xml";
  public final static String PROCESS_ELEMENT_NAME = "process";
  public final static String PROCESS_ATTR_RESOURCE = "resource";

  private static Logger logger = Logger.getLogger(ProcessDeployer.class.getName());

  ProcessEngine processEngine = ActivitiExtension.getProcessEngine();

  /**
   * Deploys a single process
   * 
   * @return the processDefinitionId of the deployed process as returned by
   *         {@link ProcessDefinition#getId()}
   */
  public String deployProcess(String resourceName) {
    logger.fine("Start deploying single process.");
    // deploy processes as one deployment
    DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService().createDeployment();
    deploymentBuilder.addClasspathResource(resourceName);
    // deploy the processes
    Deployment deployment = deploymentBuilder.deploy();
    logger.fine("Process deployed");
    // retreive the processDefinitionId for this process
    return processEngine.getRepositoryService().createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult().getId();
  }

  /**
   * Deploys all processes listed in the {@link #PROCESSES_FILE_NAME}-file.
   */
  public void deployProcesses() {
    // build a single deployment containing all discovered processes
    Set<String> resourceNames = getResourceNames();
    if (resourceNames.size() == 0) {
      logger.info("Not creating a deployment");
      return;
    }
    logger.fine("Start deploying processes.");
    DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService().createDeployment();
    for (String string : resourceNames) {
      logger.info("Adding '" + string + "' to deployment.");
      deploymentBuilder.addClasspathResource(string);
    }
    // deploy the processes
    deploymentBuilder.deploy();
    logger.fine("Done deploying processes.");
  }

  public Set<String> getResourceNames() {
    Set<String> result = new HashSet<String>();
    URL processFileUrl = getClass().getClassLoader().getResource(PROCESSES_FILE_NAME);
    if (processFileUrl == null) {
      logger.info("No '" + PROCESSES_FILE_NAME + "'-file provided.");
      // return empty set
      return result;
    }
    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(processFileUrl.openStream());
      NodeList nodeList = document.getElementsByTagName(PROCESS_ELEMENT_NAME);
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node cn = nodeList.item(i);
        if (!(cn instanceof Element)) {
          continue;
        }
        Element ce = (Element) cn;
        String resourceName = ce.getAttribute(PROCESS_ATTR_RESOURCE);
        if (resourceName == null || resourceName.length() == 0) {
          continue;
        }
        result.add(resourceName);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "could not parse file " + PROCESSES_FILE_NAME + "'. " + e.getMessage(), e);
    }
    return result;
  }
}
