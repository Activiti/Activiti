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
package org.activiti.spring.test.transaction;

import org.activiti.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**

 */
public class DeployBean {

  @Autowired
  protected RepositoryService repositoryService;

  @Transactional
  public void deployProcesses() {
    repositoryService
        .createDeployment()
        .addString(
            "process01.bpmn20.xml",
            "<definitions xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL' targetNamespace='http://activiti.org/BPMN20'>"
                + "<process id='process01' name='Insurance Damage Report' /></definitions>").deploy();

    repositoryService
        .createDeployment()
        .addString(
            "process01.bpmn20.xml",
            "<definitions xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL' targetNamespace='http://activiti.org/BPMN20'>"
                + "<process id='process01' name='Insurance Damage Report' this_should='fail' /></definitions>").deploy();
  }

  public RepositoryService getRepositoryService() {
    return repositoryService;
  }

  public void setRepositoryService(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }

}
