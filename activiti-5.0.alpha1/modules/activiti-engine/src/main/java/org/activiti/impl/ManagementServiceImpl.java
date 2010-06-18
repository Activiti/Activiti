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
package org.activiti.impl;

import java.util.Map;

import org.activiti.ManagementService;
import org.activiti.impl.cmd.GetTableCountCmd;
import org.activiti.impl.cmd.GetTablePageCmd;
import org.activiti.mgmt.TablePage;


/**
 * @author Tom Baeyens
 */
public class ManagementServiceImpl extends ServiceImpl implements ManagementService {

  public ManagementServiceImpl(ProcessEngineImpl processEngine) {
    super(processEngine);
  }

  public Map<String, Long> getTableCount() {
    return cmdExecutor.execute(new GetTableCountCmd(), processEngine);
  }

  public TablePage getTablePage(String tableName, int firstResult, int maxResults) {
    return cmdExecutor.execute(new GetTablePageCmd(tableName, firstResult, maxResults), processEngine);
  }
}
