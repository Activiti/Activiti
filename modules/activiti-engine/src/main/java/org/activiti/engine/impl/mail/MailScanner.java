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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * @author Tom Baeyens
 */
public class MailScanner {
  
  private static Logger log = Logger.getLogger(MailScanner.class.getName());
  
  protected Thread thread = null;
  protected boolean isActive = false;
  protected CommandExecutor commandExecutor;
  protected boolean isAutoActivate = false;
  
  protected MailScanSchedulerThread mailScanSchedulerThread;
  protected BlockingQueue<Runnable> threadPoolQueue;
  protected ThreadPoolExecutor threadPoolExecutor;
  protected int queueSize = 1;
  protected int corePoolSize = 3;
  private int maxPoolSize = 10;
  
  public synchronized void start() {
    if(isActive) {
      // Already started, nothing to do
      log.info("Ignoring duplicate MailScanner start invocation");
      return;
    } else {
      isActive = true;
      
      if (mailScanSchedulerThread==null) {
        mailScanSchedulerThread = new MailScanSchedulerThread(this);
      }
      if (threadPoolQueue==null) {
        threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
      }
      if (threadPoolExecutor==null) {
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, threadPoolQueue);
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
      }
      
      // Create the mail scan scheduler
      log.fine("MailScanner is starting the "+MailScanSchedulerThread.class.getName());
      mailScanSchedulerThread.start();
    }
  }
  
  public void shutdown() {
    if(!isActive) {
      log.info("Ignoring request to shut down non-active MailScanner");
      return;
    }
    
    log.info("Shutting down the MailScanner");
    
    // Ask the thread pool to finish and exit
    threadPoolExecutor.shutdown();
    
    // Waits for 1 minute to finish all currently executing scans
    try {
      threadPoolExecutor.awaitTermination(60L, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
        throw new ActivitiException("Timeout during shutdown of mail scanner. The current running scans could not end withing 60 seconds after shutdown operation.", e);
    }
    
    // Close the pending scans task
    mailScanSchedulerThread.shutdown();
    
    isActive = false;

    // Clear references
    threadPoolExecutor = null;
    mailScanSchedulerThread = null;
  }
  
  public void addUser(String userId, String userPassword) {
    if (isActive && mailScanSchedulerThread != null) {
      mailScanSchedulerThread.addUser(userId, userPassword);
    }
  }

  public void removeUser(String userId) {
    if (mailScanSchedulerThread != null) {
      mailScanSchedulerThread.removeUser(userId);
    }
  }

  // getters and setters ////////////////////////////////////////////////////// 

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }
  
  public int getQueueSize() {
    return queueSize;
  }
  
  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }
  
  public int getCorePoolSize() {
    return corePoolSize;
  }
  
  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  public void setMaxPoolSize(int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }
  
  public BlockingQueue<Runnable> getThreadPoolQueue() {
    return threadPoolQueue;
  }

  public void setThreadPoolQueue(BlockingQueue<Runnable> threadPoolQueue) {
    this.threadPoolQueue = threadPoolQueue;
  }

  public ThreadPoolExecutor getThreadPoolExecutor() {
    return threadPoolExecutor;
  }
  
  public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
    this.threadPoolExecutor = threadPoolExecutor;
  }
  
  public boolean isActive() {
    return isActive;
  }
  
  public boolean isAutoActivate() {
    return isAutoActivate;
  }
  
  public void setAutoActivate(boolean isAutoActivate) {
    this.isAutoActivate = isAutoActivate;
  }

  public MailScanSchedulerThread getMailScanSchedulerThread() {
    return mailScanSchedulerThread;
  }

  
  public void setMailScanSchedulerThread(MailScanSchedulerThread mailScanScheduler) {
    this.mailScanSchedulerThread = mailScanScheduler;
  }

  
  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

  public void setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }
}
