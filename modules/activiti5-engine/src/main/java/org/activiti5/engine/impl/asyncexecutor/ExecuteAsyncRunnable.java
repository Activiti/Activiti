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
package org.activiti5.engine.impl.asyncexecutor;

import org.activiti5.engine.impl.interceptor.CommandExecutor;
import org.activiti5.engine.impl.persistence.entity.JobEntity;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class ExecuteAsyncRunnable implements Runnable {

	protected JobEntity job;
	protected CommandExecutor commandExecutor;

	public ExecuteAsyncRunnable(JobEntity job, CommandExecutor commandExecutor) {
		this.job = job;
		this.commandExecutor = commandExecutor;
	}

	public void run() {
	  AsyncJobUtil.executeJob(job, commandExecutor);
	}
}
