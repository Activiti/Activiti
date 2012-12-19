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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tom Baeyens
 */
public class ControllableThread extends Thread {
  
  private static Logger log = LoggerFactory.getLogger(ControllableThread.class);

  public ControllableThread() {
    String className = getClass().getName();
    int dollarIndex = className.lastIndexOf('$');
    setName(className.substring(dollarIndex+1));
  }
  
  public synchronized void startAndWaitUntilControlIsReturned() {
    log.debug("test thread will start {} and wait till it returns control", getName());
    start();
    try {
      wait();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public synchronized void returnControlToTestThreadAndWait() {
    log.debug("{} will notify test thread and till test thread proceeds this thread", getName());
    this.notify();
    try {
      this.wait();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public synchronized void proceedAndWaitTillDone() {
    log.debug("test thread will notify {} and wait until it completes", getName());
    notify();
    try {
      join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
