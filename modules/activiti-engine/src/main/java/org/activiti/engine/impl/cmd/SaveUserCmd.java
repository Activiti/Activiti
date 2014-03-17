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
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.UserEntity;


/**
 * @author Joram Barrez
 */
public class SaveUserCmd implements Command<Void>, Serializable {
  
  private static final long serialVersionUID = 1L;
  protected UserEntity user;
  
  public SaveUserCmd(User user) {
    if (user == null) { 
      throw new ActivitiIllegalArgumentException("user is null");
    } else if (user instanceof UserEntity) { 
      this.user = (UserEntity) user; 
    } else { 
      this.user = new UserEntity();
      this.user.setId(user.getId());
      // revision does not exist in interface so this will always result in 
      // insert, see execute below.  
      this.user.setFirstName(user.getFirstName());
      this.user.setLastName(user.getLastName());
      this.user.setEmail(user.getEmail());
      this.user.setPassword(user.getPassword());
    }
  }
  
  public Void execute(CommandContext commandContext) {
    if(user == null) {
      throw new ActivitiIllegalArgumentException("user is null");
    }
    if (user.getRevision()==0) {
      try { 
        commandContext
          .getUserIdentityManager()
          .insertUser(user);
      } catch (Exception e) { 
        // This occurs when the received User instance was not a 
        // UserEntity at construction time AND the user record 
        // does already exist. 
        UserEntity user = (UserEntity) commandContext.getProcessEngineConfiguration().getIdentityService().createUserQuery().userId(this.user.getId()).singleResult();
        user.setFirstName(this.user.getFirstName());
        user.setLastName(this.user.getLastName());
        user.setEmail(this.user.getEmail());
        user.setPassword(this.user.getPassword());
        commandContext
          .getUserIdentityManager()
          .updateUser(user);
      }   
    } else {
      commandContext
        .getUserIdentityManager()
        .updateUser(user);
    }
    
    return null;
  }
}
