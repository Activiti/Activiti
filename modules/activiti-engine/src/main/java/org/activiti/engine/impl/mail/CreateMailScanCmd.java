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

package org.activiti.engine.impl.mail;

import java.util.Map;

import org.activiti.engine.impl.cmd.GetUserAccountCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Account;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;


/**
 * @author Tom Baeyens
 */
public class CreateMailScanCmd implements Command<MailScanCmd> {

  protected String userId;
  protected String userPassword;

  public CreateMailScanCmd(String userId, String userPassword) {
    this.userId = userId;
    this.userPassword = userPassword;
  }

  public MailScanCmd execute(CommandContext commandContext) {
    MailScanCmd mailScanCmd = null;
    Account account = new GetUserAccountCmd(userId, userPassword, "todo-scanner").execute(commandContext);
    if (account!=null) {
      Map<String, String> details = account.getDetails();
      
      String imapUsername = account.getUsername();
      String imapPassword = account.getPassword();
      String toDoFolderName = details.get("toDoFolderName");
      String toDoInActivitiFolderName = details.get("toDoInActivitiFolderName");
      String imapHost = (String) details.get("imapHost");
      String imapProtocol = (String) details.get("imapProtocol");
      
      // fall back to the default imapHost and imapProtocol
      if (imapHost==null) {
        Map<Object, Object> beans = Context
          .getProcessEngineConfiguration()
          .getBeans();
        imapHost = (String) beans.get("imapHost");
        imapProtocol = (String) beans.get("imapProtocol");
      }
      
      mailScanCmd = new MailScanCmd();
      mailScanCmd.setUserId(userId);
      mailScanCmd.setImapUsername(imapUsername);
      mailScanCmd.setImapPassword(imapPassword);
      mailScanCmd.setImapHost(imapHost);
      mailScanCmd.setImapProtocol(imapProtocol);
      mailScanCmd.setToDoFolderName(toDoFolderName);
      mailScanCmd.setToDoInActivitiFolderName(toDoInActivitiFolderName);
    }
    
    return mailScanCmd;
  }

}
