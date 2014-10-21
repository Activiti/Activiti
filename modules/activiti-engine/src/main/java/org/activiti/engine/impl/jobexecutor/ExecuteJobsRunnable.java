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

import java.util.List;

import org.activiti.engine.impl.cmd.ExecuteJobsCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class ExecuteJobsRunnable implements Runnable {

	private static Logger log = LoggerFactory.getLogger(ExecuteJobsRunnable.class);

	private JobEntity job;
	private List<String> jobIds;
	private JobExecutor jobExecutor;

	public ExecuteJobsRunnable(JobExecutor jobExecutor, JobEntity job) {
		this.jobExecutor = jobExecutor;
		this.job = job;
	}

	public ExecuteJobsRunnable(JobExecutor jobExecutor, List<String> jobIds) {
		this.jobExecutor = jobExecutor;
		this.jobIds = jobIds;
	}

	public void run() {
		if (jobIds != null) {
			handleMultipleJobs();
		}
		if (job != null) {
			handleSingleJob();
		}
	}

	protected void handleSingleJob() {
		final SingleJobExecutorContext jobExecutorContext = new SingleJobExecutorContext();
		final List<JobEntity> currentProcessorJobQueue = jobExecutorContext.getCurrentProcessorJobQueue();
		final CommandExecutor commandExecutor = jobExecutor.getCommandExecutor();

		currentProcessorJobQueue.add(job);

		Context.setJobExecutorContext(jobExecutorContext);
		try {
			while (!currentProcessorJobQueue.isEmpty()) {

				JobEntity currentJob = currentProcessorJobQueue.remove(0);
				try {
					commandExecutor.execute(new ExecuteJobsCmd(currentJob));
				} catch (Throwable e) {
					log.error("exception during job execution: {}", e.getMessage(), e);
				} finally {
					jobExecutor.jobDone(currentJob);
				}
			}
		} finally {
			Context.removeJobExecutorContext();
		}
	}

	protected void handleMultipleJobs() {
		final MultipleJobsExecutorContext jobExecutorContext = new MultipleJobsExecutorContext();
		final List<String> currentProcessorJobQueue = jobExecutorContext.getCurrentProcessorJobQueue();
		final CommandExecutor commandExecutor = jobExecutor.getCommandExecutor();

		currentProcessorJobQueue.addAll(jobIds);

		Context.setJobExecutorContext(jobExecutorContext);
		try {
			while (!currentProcessorJobQueue.isEmpty()) {

				String currentJobId = currentProcessorJobQueue.remove(0);
				try {
					commandExecutor.execute(new ExecuteJobsCmd(currentJobId));
				} catch (Throwable e) {
					log.error("exception during job execution: {}", e.getMessage(), e);
				} finally {
					jobExecutor.jobDone(currentJobId);
				}
			}
		} finally {
			Context.removeJobExecutorContext();
		}
	}
}
