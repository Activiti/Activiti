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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * @author Tom Baeyens
 */
public class MailScanner implements Runnable {
  
  private static Logger log = Logger.getLogger(MailScanner.class.getName());
  
  protected Thread thread = null;
  protected boolean isActive;
  protected Map<String, MailScanCmd> scanCommands = Collections.synchronizedMap(new HashMap<String, MailScanCmd>());
  protected CommandExecutor commandExecutor;

  public void start() {
    thread = new Thread(this);
  }
  
  public void stop() {
    isActive = false;
    thread.interrupt();
  }
  
  public void addUser(String userId, String userPassword) {
    MailScanCmd mailScanCmd = commandExecutor.execute(new CreateMailScanCmd(userId, userPassword));
    if (mailScanCmd!=null) {
      scanCommands.put(userId, mailScanCmd);
    }
  }

  public void removeUser(String userId) {
    scanCommands.remove(userId);
  }

  public void run() {
    while (isActive) {
      List<MailScanCmd> round = new ArrayList<MailScanCmd>(scanCommands.values());
      for (MailScanCmd mailScanCmd: round) {
        try {
          commandExecutor.execute(mailScanCmd);
        } catch (Exception e) {
          log.log(Level.SEVERE, "couldn't check todo mail for "+mailScanCmd.getUserId()+": "+e.getMessage(), e);
        }
      }

      if (scanCommands.isEmpty()) {
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          log.fine("sleep got interrupted");
        }
      }
    }
  }

  public void setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }
}
