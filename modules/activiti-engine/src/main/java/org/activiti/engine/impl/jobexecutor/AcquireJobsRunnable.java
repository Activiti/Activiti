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


/**
 * @author Joram Barrez 
 */
public interface AcquireJobsRunnable extends Runnable {

	/** 
   * Called by the job executor when a new job was added by the 
   * process engine to which this job executor belongs. 
   * This is a hint, that for example the acquiring needs to start again 
   * when it would be sleeping.
   */
	void jobWasAdded();
	
	/**
	 * Called when the job executor is shutting down and the job
	 * acquiring needs to be shut down too.
	 */
	void stop();
 
}
