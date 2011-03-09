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

import org.activiti.engine.impl.identity.IdentityInfoEntity;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;


/**
 * @author Tom Baeyens
 */
public class SetUserInfoCmd implements Command<Object> {

  protected String userId;
  protected String key;
  protected String value;
  protected String type;
  protected String password;
  
  public SetUserInfoCmd(String userId, String key, String value) {
    this.userId = userId;
    this.type = IdentityInfoEntity.TYPE_USERINFO;
    this.key = key;
    this.value = value;
  }

  public SetUserInfoCmd(String userId, String accountName, String accountUsername, String accountPassword) {
    this.userId = userId;
    this.type = IdentityInfoEntity.TYPE_USERACCOUNT;
    this.key = accountName;
    this.value = accountUsername;
    this.password = accountPassword;
  }

  public Object execute(CommandContext commandContext) {
    commandContext
      .getIdentitySession()
      .setUserInfo(userId, type, key, value, password);
    return null;
  }
}
