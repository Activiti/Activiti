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

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.identity.GroupEntity;
import org.activiti.engine.impl.persistence.identity.UserEntity;


/**
 * @author Joram Barrez
 */
public class SaveGroupCmd extends CmdVoid {
  
  protected GroupEntity group;
  
  public SaveGroupCmd(GroupEntity group) {
    this.group = group;
  }
  
  public void executeVoid(CommandContext commandContext) {
    if (group.getId()==null) {
      commandContext
        .getIdentitySession()
        .insertGroup(group);
    } else {
      GroupEntity persistentGroup = commandContext
        .getIdentitySession()
        .findGroupById(group.getId());
      
      persistentGroup.update(group);
      
    }
  }

}
