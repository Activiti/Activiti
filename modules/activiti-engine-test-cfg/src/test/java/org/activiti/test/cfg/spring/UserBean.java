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

import org.activiti.ProcessService;
import org.activiti.ProcessEngine;
import org.activiti.impl.cmduser.CommandVoid;


/**
 * @author Tom Baeyens
 */
public class UserBean {

  /** injected by spring */
  protected ProcessEngine processEngine;
  
  public void doTransactional() {
//    processEngine.execute(new CommandVoid() {
//      public void executeVoid(ProcessService processService) throws Exception {
//        processService.newDeployment()
//          .addString("userprocess.bpmn.xml",
//            "<bpmn-process />" )
//          .deploy();
//      }
//    });
  }

  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }
}
