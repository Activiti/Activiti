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

import org.activiti.engine.impl.identity.Account;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;


/**
 * @author Tom Baeyens
 */
public class GetUserAccountCmd implements Command<Account>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String userId;
  protected String userPassword;
  protected String accountName;
  
  public GetUserAccountCmd(String userId, String userPassword, String accountName) {
    this.userId = userId;
    this.userPassword = userPassword;
    this.accountName = accountName;
  }

  public Account execute(CommandContext commandContext) {
    return commandContext
      .getIdentityInfoManager()
      .findUserAccountByUserIdAndKey(userId, userPassword, accountName);
  }
}
