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
package org.activiti.impl.jobexecutor;

import java.util.logging.Logger;

/**
 * Holds the recent list of Jobs that
 *  have been run by the {@link JobExecutor}.
 * Works on a FIFO basis.
 * Ordered by the time that the {@link JobExecutor}
 *  finished with them.
 * Note that it only applies to this {@link JobExecutor},
 *  and in a clustered environment, other
 *  instances will have their own independent
 *  history lists.
 */
public class HistoricJobsList {
  private static Logger log = Logger.getLogger(HistoricJobsList.class.getName());

  private HistoricJob[] list;
  
  private volatile int currentSize;
  private volatile int currentPos;
  
  private Object rolloverLock = new Object();
  
  public HistoricJobsList(int limit) {
    list = new HistoricJob[limit];
    currentSize = 0;
    currentPos = -1;
  }
  
  /**
   * How many entries are on the list?
   * Will grow up to the limit as jobs are
   *  run, then remain there.
   */
  public int size() {
    return currentSize;
  }
  public int limit() {
    return list.length;
  }
  
  public void record(HistoricJob job) {
    int recordPos = ++currentPos;
    
    // Handle rollover
    if(recordPos >= list.length) {
      // Rollover!
      synchronized (rolloverLock) {
        // In the lock, check someone else didn't already 
        //  fix it for us while we waited
        if(currentPos >= list.length) {
          currentPos %= list.length;
        }
      }
      recordPos %= list.length;
    }
    
    // Record
    list[recordPos] = job;
    
    // Sizing
    if(currentSize < list.length) {
      // Increment the size atomically
      currentSize++;
      
      // Now check for overshoot
      if(currentSize >= list.length) {
        currentSize = list.length;
      }
    }
  }
  
  /**
   * Gets the last job to be
   *  finished with.
   */
  public HistoricJob getLastJob() {
    return list[currentPos];
  }
  /**
   * Get the nth job ago to finish.
   * @param jobsAgo The nth job ago (0=latest)
   */
  public HistoricJob getPreviousJob(int jobsAgo) {
    // Note - returns based on the position at the start
    //  of the call, which may be different by the end!
    int pos = currentPos; 
    if(jobsAgo >= currentSize) {
      throw new IllegalArgumentException("Asked for " + jobsAgo + " jobs ago, but there haven't been that many!");
    }
    
    pos -= jobsAgo;
    if(pos < 0) {
      // Wrap around will only occur when full!
      pos += list.length;
    }
    return list[pos];
  }
}
