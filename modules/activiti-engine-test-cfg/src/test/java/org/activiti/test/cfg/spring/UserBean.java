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
package org.activiti.test.cfg.spring;

import org.activiti.engine.ProcessEngine;

/**
 * @author Tom Baeyens
 */
public class UserBean {

  /** injected by spring */
  private ProcessEngine processEngine;

  private boolean fail = false;

  private static final String NAMESPACE = "xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL'";

  private static final String TARGET_NAMESPACE = "targetNamespace='http://activiti.org/BPMN20'";

  public void doTransactional() {
    processEngine.getRepositoryService().createDeployment().addString("userprocess.bpmn20.xml",
            "<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report' />" + "</definitions>").deploy();
    if (fail) {
      processEngine.getRepositoryService().createDeployment().addString("invalidprocess.bpmn20.xml",
              "<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <aprocess id='IDR' name='Insurance Damage Report' />" + "</definitions>").deploy();
    }
  }

  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  public void setFail(boolean fail) {
    this.fail = fail;
  }
}
