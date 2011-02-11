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
package org.activiti.engine.impl.cmd;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.identity.GroupEntity;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;


/**
 * @author Joram Barrez
 */
public class SaveGroupCmd implements Command<Void> {
  
  protected GroupEntity group;
  
  public SaveGroupCmd(GroupEntity group) {
    this.group = group;
  }
  
  public Void execute(CommandContext commandContext) {
    if(group == null) {
      throw new ActivitiException("group is null");
    }
    if (group.getRevision()==0) {
      commandContext
        .getIdentitySession()
        .insertGroup(group);
    } else {
      commandContext
        .getIdentitySession()
        .updateGroup(group);
    }
    
    return null;
  }

}
