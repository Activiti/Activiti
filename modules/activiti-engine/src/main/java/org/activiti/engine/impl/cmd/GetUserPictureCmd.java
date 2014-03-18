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
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;


/**
 * @author Tom Baeyens
 */
public class GetUserPictureCmd implements Command<Picture>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String userId;
  
  public GetUserPictureCmd(String userId) {
    this.userId = userId;
  }

  public Picture execute(CommandContext commandContext) {
    if(userId == null) {
      throw new ActivitiIllegalArgumentException("userId is null");
    }
    User user = commandContext
      .getUserIdentityManager()
      .findUserById(userId);
    if(user == null) {
      throw new ActivitiObjectNotFoundException("user "+userId+" doesn't exist", User.class);
    }
    return commandContext.getUserIdentityManager().getUserPicture(userId);
  }

}
