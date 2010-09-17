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

package org.activiti.engine.test.history;

import java.util.List;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.test.Deployment;


/**
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceTest extends ActivitiInternalTestCase {
  
  @Deployment
  public void testHistoricActivityForSingleAutomaticActivity() {
    runtimeService.startProcessInstanceByKey("singleAutomaticActivity");
    
    List<HistoricActivityInstance> historicActivityInstances = historyService
      .createHistoricActivityInstanceQuery()
      .list();
    
    assertEquals(1, historicActivityInstances.size());
  }
}
