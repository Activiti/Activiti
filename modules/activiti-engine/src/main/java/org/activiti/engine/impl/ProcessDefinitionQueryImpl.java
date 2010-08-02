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
import org.activiti.engine.ProcessDefinition;
import org.activiti.engine.ProcessDefinitionQuery;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionQueryImpl extends AbstractQuery<ProcessDefinition> implements ProcessDefinitionQuery {

  public ProcessDefinitionQueryImpl() {
  }

  public ProcessDefinitionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    return 0;
  }

  @Override
  public List<ProcessDefinition> executeList(CommandContext commandContext, Page page) {
    return null;
  }

  public ProcessDefinitionQueryImpl deploymentId(String deploymentId) {
    return null;
  }

}
