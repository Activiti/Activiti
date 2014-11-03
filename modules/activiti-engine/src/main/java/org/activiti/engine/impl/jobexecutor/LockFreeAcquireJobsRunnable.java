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
package org.activiti.engine.impl.jobexecutor;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AcquireJobsRunnable} implementation with sole purpose
 * of 'acquiring' jobs, which means setting the current job executor
 * name into the lock owner colum to 'acquire' it.
 * 
 * Later on, these jobs will be fetched and put on the work queue.
 * 
 * @author jbarrez
 */
public class LockFreeAcquireJobsRunnable implements AcquireJobsRunnable {

  private static Logger log = LoggerFactory.getLogger(LockFreeAcquireJobsRunnable.class);

  /** The job executor. Only works with the {@link LockFreeJobExecutor} implementation */
  protected final LockFreeJobExecutor jobExecutor;
  
  /** Flag to indicate the runnable was interrupted */ 
  protected volatile boolean isInterrupted = false;
  
  /** Flag to indicate a job was added (used when the runnable was sleeping, but a job was created by the engine) */
  protected volatile boolean isJobAdded = false;
  
  /** Monitor object to synchronize on. */
  protected final Object MONITOR = new Object();
  
  /** Flag to indicate whether the runnable is currently waiting. */
  protected final AtomicBoolean isWaiting = new AtomicBoolean(false);
  
  public LockFreeAcquireJobsRunnable(LockFreeJobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
  }

  public synchronized void run() {
    log.info("{} starting to acquire jobs", jobExecutor.getName());

    final CommandExecutor commandExecutor = jobExecutor.getCommandExecutor();

    while (!isInterrupted) {
      isJobAdded = false;
      
      Integer nrOfAquiredJobs = 0;
      try {
      	
	      // Acquire jobs in transaction
	      nrOfAquiredJobs = commandExecutor.execute(new Command<Integer>() {
	    		
	    		public Integer execute(CommandContext commandContext) {
	    			return commandContext.getJobEntityManager().updateJobLockForAllJobs(
	    					jobExecutor.getLockOwner(), 
	    					getLockExpirationTime(commandContext, jobExecutor.getLockTimeInMillis()));
	    		}
	    		
	    		protected Date getLockExpirationTime(CommandContext commandContext, int lockTimeInMillis) {    
	    			GregorianCalendar gregorianCalendar = new GregorianCalendar();
	    		  gregorianCalendar.setTime(commandContext.getProcessEngineConfiguration().getClock().getCurrentTime());
	    		  gregorianCalendar.add(Calendar.MILLISECOND, lockTimeInMillis);
	    		  return gregorianCalendar.getTime(); 
	    		}
	    		
				});
      } catch (Exception e) {
      	log.warn("Error while acquiring job", e); // Cannot do anything more than logging it
      }
      
      
      if (nrOfAquiredJobs == 0) {
      	sleep();
      } else {
      	log.debug("Wrote lock owner to {} jobs. Putting them on the queue now.", nrOfAquiredJobs);
      }
      
      // To avoid that one node in a job executor cluster acquires all jobs,
      // we always process the acquired jobs first before acquiring new ones.
      // This way, other nodes can start acquiring jobs now while this one
      // is still executing them.
      putAcquiredJobsOnQueue();

    }
    
    log.info("{} stopped job acquisition", jobExecutor.getName());
  }

	protected void sleep() {
	  // TODO: needs to be other property?
	  long millisToWait = jobExecutor.getWaitTimeInMillis();
    if ((millisToWait > 0) && (!isJobAdded)) {
	    try {
	      log.debug("job acquisition thread sleeping for {} millis", millisToWait);
	      synchronized (MONITOR) {
	        if(!isInterrupted) {
	          isWaiting.set(true);
	          MONITOR.wait(millisToWait);
	        }
	      }
	      
	      if (log.isDebugEnabled()) {
	        log.debug("job acquisition thread woke up");
	      }
	    } catch (InterruptedException e) {
	      if (log.isDebugEnabled()) {
	        log.debug("job acquisition wait interrupted");
	      }
	    } finally {
	      isWaiting.set(false);
	    }
	  }
  }
	
	protected void putAcquiredJobsOnQueue() {
		
		// Fetch jobs
		int start = 0;
		List<JobEntity> jobs = fetchJobs(start);
		
		boolean refetchJobs = false; // Optimization: when doing the refetch at the end of the while,  avoid doing it to much by keeping this boolean
		while (!jobs.isEmpty()) {
		
			// Put on queue
			
			// TODO: divide the jobs by configurable number
			
			int newJobCount = 0;
			for (JobEntity job : jobs) {
				try {
					if (!jobExecutor.isJobScheduledForExecution(job)) {
						
						jobExecutor.jobScheduledForExecution(job);
						jobExecutor.getThreadPoolExecutor().execute(new ExecuteJobsRunnable(jobExecutor, job));
						
						newJobCount++;
					}
				} catch (RejectedExecutionException e) {
					// If the queue is full, the rejection handler will execute it in the current thread (ie: this thread)
					// This takes care of throttling the load when the queue is full
					jobExecutor.getRejectedJobsHandler().jobsRejected(jobExecutor, Arrays.asList(job.getId()));
				}
			}
			
			if (newJobCount == 0 && refetchJobs) {
				break; // If no new jobs were found, and we were doing a refect anyway, just stop
			}
			
			// The idea is here that we ask for different pages while fetching the jobs
			// The reasoning here, is that it could very well be the threadpool is executing
			// the previous batch of jobs, and the fetch would simply give the same results
			// back all the time (fetching is often quicker than executing the job).
			// To avoid this, we use paging (+ order by clause in the sql query to make 
			// sure the paging is consistent)
			
			start += jobExecutor.getJobFetchBatchSize();
			jobs = fetchJobs(start);

			// Of course, since the threadpool is executing in the meantime, it is most likely
			// some jobs will be deleted when a new batch is fetched, thus missing a certain
			// amount of jobs. Hence the re-fetch at the end. 
			if (jobs.isEmpty()) {
				start = 0;
				jobs = fetchJobs(start);
				refetchJobs = true;
			}
			
		}
		
	}

	protected List<JobEntity> fetchJobs(final int start) {
		
		// The start + max is really important here, combined with an order by in the query to get consistent results.
		// If it wouldn't be there, the fetch would always refecth the same jobs, and put the same on the queue.
		// Now, even if the jobs get processed faster by the threadpool, there will be no overlap
		// (the jobs will be fetched in the next go)
		
		// Fetch jobs in transaction
	  return jobExecutor.getCommandExecutor().execute(new Command<List<JobEntity>>() {
			public List<JobEntity> execute(CommandContext commandContext) {
				return commandContext.getJobEntityManager().findJobsByLockOwner(
						jobExecutor.getLockOwner(), start, jobExecutor.getJobFetchBatchSize());
			}
		});
  }

  public void stop() {
    synchronized (MONITOR) {
      isInterrupted = true; 
      if(isWaiting.compareAndSet(true, false)) { 
          MONITOR.notifyAll();
        }
      }
  }

  public void jobWasAdded() {    
    isJobAdded = true;
    if(isWaiting.compareAndSet(true, false)) { 
      // ensures we only notify once
      // I am OK with the race condition      
      synchronized (MONITOR) {
        MONITOR.notifyAll();
      }
    }    
  }
  
  
}
