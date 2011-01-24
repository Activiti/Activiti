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
package org.activiti.kickstart.service;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.activiti.engine.repository.Deployment;
import org.activiti.kickstart.dto.KickstartWorkflowDto;
import org.activiti.kickstart.dto.KickstartWorkflowInfo;

/**
 * @author Joram Barrez
 */
public interface KickstartService {

  /**
   * deploys the Workflow and returns the deployment id
   */
  String deployKickstartWorkflow(KickstartWorkflowDto adhocWorkflow) throws JAXBException;

  List<KickstartWorkflowInfo> findKickstartWorkflowInformation();

  KickstartWorkflowDto findKickstartWorkflowById(String id) throws JAXBException;

  InputStream getProcessImage(String processDefinitionId);

  InputStream getProcessBpmnXml(String processDefinitionId);

}
