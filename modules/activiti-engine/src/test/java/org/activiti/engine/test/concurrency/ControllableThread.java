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

package org.activiti.engine.test.concurrency;

import java.util.logging.Logger;


/**
 * @author Tom Baeyens
 */
public class ControllableThread extends Thread {
  
  private static Logger log = Logger.getLogger(ControllableThread.class.getName());

  public ControllableThread() {
    String className = getClass().getName();
    int dollarIndex = className.lastIndexOf('$');
    setName(className.substring(dollarIndex+1));
  }
  
  public synchronized void startAndWaitUntilControlIsReturned() {
    log.fine("test thread will start "+getName()+" and wait till it returns control");
    start();
    try {
      wait();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public synchronized void returnControlToTestThreadAndWait() {
    log.fine(getName()+" will notify test thread and till test thread proceeds this thread");
    this.notify();
    try {
      this.wait();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public synchronized void proceedAndWaitTillDone() {
    log.fine("test thread will notify "+getName()+" and wait until it completes");
    notify();
    try {
      join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
