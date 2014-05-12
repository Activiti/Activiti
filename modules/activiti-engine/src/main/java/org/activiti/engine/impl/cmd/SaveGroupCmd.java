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

import java.io.Serializable;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;


/**
 * @author Joram Barrez
 */
public class SaveGroupCmd implements Command<Void>, Serializable {
  
  private static final long serialVersionUID = 1L;
  protected Group group;
  
  public SaveGroupCmd(Group group) {
    this.group = group;
  }
  
  public Void execute(CommandContext commandContext) {
    if(group == null) {
      throw new ActivitiIllegalArgumentException("group is null");
    }
    
    if(commandContext.getGroupIdentityManager().isNewGroup(group)) {
    	commandContext
    		.getGroupIdentityManager()
    		.insertGroup(group);
    } else {
    	commandContext
    		.getGroupIdentityManager()
    		.updateGroup(group);
    }
    return null;
  }

}
