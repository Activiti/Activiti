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

package org.activiti.engine.test.mail;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.mail.MailScanner;
import org.activiti.engine.impl.util.LogUtil;


/**
 * @author Tom Baeyens
 */
public class MailScanTester {
  
  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }
  
  public static void main(String[] args) {
    ProcessEngineImpl processEngine = (ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
    IdentityService identityService = processEngine.getIdentityService();
    
    User user = identityService.newUser("johndoe");
    identityService.saveUser(user);
    
    String accountUsername = System.getProperty("user");
    String accountPassword = System.getProperty("pwd");
    Map<String, String> accountDetails = new HashMap<String, String>();
    accountDetails.put("toDoFolderName", "Cases");
    accountDetails.put("toDoInActivitiFolderName", "CasesInActiviti");
    accountDetails.put("imapHost", "imap.gmail.com");
    accountDetails.put("imapProtocol", "imaps");
    
    identityService.setUserAccount("tom", null, "mailscan", accountUsername, accountPassword, accountDetails);
    
    MailScanner mailScanner = processEngine
      .getProcessEngineConfiguration()
      .getMailScanner();
    
    mailScanner.start();
    
    mailScanner.addUser("tom", null);
    
    try {
      Thread.sleep(1000);
    } catch (Exception e) {
      e.printStackTrace();
    }

    mailScanner.shutdown();
  }
}
