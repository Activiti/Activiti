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
package org.activiti.test;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.activiti.impl.jobexecutor.PendingJobsFetcher;

/**
 * A snapshot of what Threads were running at
 *  a given moment.
 * Used typically in multi-threaded tests to keep
 *  track of what is going on.
 */
public class ThreadSnapshot {
  public static final String PENDING_JOBS_FETCHER = "PendingJobsFetcher"; 
  
  /** Non system, non thread pool threads */ 
  private ArrayList<String> running = new ArrayList<String>();
  private ArrayList<ThreadPoolSnapshot> pools = new ArrayList<ThreadPoolSnapshot>();
  
  private Pattern threadPoolPattern = Pattern.compile("pool-(\\d+)-thread-(\\d+)"); 
  
  public ThreadSnapshot() {
    Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
    
    // For each thread, decide if it's one of interest
    // If so, record what it's working on
    for(Thread t : threads.keySet()) {
      String name = t.getName();
      if("Signal Dispatcher".equals(name) ||
          "Reference Handler".equals(name) ||
          "main".equals(name) ||
          "Finalizer".equals(name)) {
        // Ignore these system threads
        continue;
      }
      if("ReaderThread".equals(name)) {
        // Ignore these unit testing related threads
        continue;
      }
      
      StackTraceElement[] st = threads.get(t);
      
      // Is it a thread pool thread?
      Matcher tpm = threadPoolPattern.matcher(name);
      if(tpm.find()) {
        int tpn = Integer.parseInt(
            tpm.group(1)
        );
        
        // Get a snapshot
        ThreadPoolSnapshot tps = null;
        for(ThreadPoolSnapshot s : pools) {
          if(s.threadPoolNumber == tpn) {
            tps = s;
            break;
          }
        }
        if(tps == null) {
          tps = new ThreadPoolSnapshot(tpn);
          pools.add(tps);
        }
        
        tps.threadCount++;
        
        // Parked or running?
        if(st.length == 0 || st[0].toString().indexOf(".park(") > -1) {
          // Parked / Starting up
        } else {
          tps.threadsActive++;
        }
        continue;
      }
     
      if(st.length == 0) {
        System.err.println(t);
        continue;
      }
      StackTraceElement lastST = st[st.length-1];
      
      // Check for certain key Activiti threads
      if(lastST.getClassName().equals( PendingJobsFetcher.class.getName() )) {
        running.add(PENDING_JOBS_FETCHER);
        continue;
      }
      
      // If we get here, it's something else
      running.add(name + " " + st[st.length-1].getClassName());
      
      // Report that for now for debugging
      System.out.println(name);
      for(StackTraceElement e : st) {
        System.out.println("\t" + e);
      }
    }
  }
  
  public boolean hasPendingJobsFetcher() {
    for(String s : running) {
      if(s.equals(PENDING_JOBS_FETCHER)) {
        return true;
      }
    }
    return false;
  }
  
  public int getNumberOfRegularThreads() {
    return running.size();
  }
  public String[] getRegularThreads() {
    return running.toArray(new String[running.size()]);
  }
  
  public int getNumberOfThreadPools() {
    return pools.size();
  }
  public ThreadPoolSnapshot[] getThreadPools() {
    return pools.toArray(new ThreadPoolSnapshot[pools.size()]);
  }
  
  public static class ThreadPoolSnapshot {
    private int threadPoolNumber;
    private int threadCount;
    private int threadsActive;

    private ThreadPoolSnapshot(int threadPoolNumber) {
      this.threadPoolNumber = threadPoolNumber;
      this.threadCount = 0;
      this.threadsActive = 0;
    }
    
    public int getThreadPoolNumber() {
      return threadPoolNumber;
    }
    public int getThreadCount() {
      return threadCount;
    }
    public int getThreadsActive() {
      return threadsActive;
    }
  }
}
