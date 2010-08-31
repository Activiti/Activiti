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

package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.Page;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionQueryImpl extends AbstractQuery<ProcessDefinition> implements ProcessDefinitionQuery {
  
  protected String deploymentId;

  public ProcessDefinitionQueryImpl() {
  }

  public ProcessDefinitionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public ProcessDefinitionQueryImpl deploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
    return this;
  }

  public ProcessDefinitionQueryImpl orderAsc(String column) {
    super.addOrder(column, SORTORDER_ASC);
    return this;
  }
  
  public ProcessDefinitionQueryImpl orderDesc(String column) {
    super.addOrder(column, SORTORDER_DESC);
    return this;
  }
  
  @Override
  public long executeCount(CommandContext commandContext) {
    return commandContext
      .getRepositorySession()
      .findProcessDefinitionCountByQueryCriteria(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ProcessDefinition> executeList(CommandContext commandContext, Page page) {
    return (List) commandContext
      .getRepositorySession()
      .findProcessDefinitionsByQueryCriteria(this, page);
  }
  
  public String getDeploymentId() {
    return deploymentId;
  }
}
