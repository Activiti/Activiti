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
package org.activiti.engine.impl.asyncexecutor;

import org.activiti.engine.impl.cmd.ExecuteAsyncJobCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class ExecuteAsyncRunnable implements Runnable {

	private static Logger log = LoggerFactory.getLogger(ExecuteAsyncRunnable.class);

	protected JobEntity job;
	protected CommandExecutor commandExecutor;

	public ExecuteAsyncRunnable(JobEntity job, CommandExecutor commandExecutor) {
		this.job = job;
		this.commandExecutor = commandExecutor;
	}

	public void run() {
		try {
			commandExecutor.execute(new ExecuteAsyncJobCmd(job));
		} catch (Throwable e) {
			log.error("exception during job execution: {}", e.getMessage(), e);
		}
	}
}
