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

package org.activiti.spring;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.runtime.Job;

/**
 * <p>
 * Strategy for handling jobs that were acquired but cannot be executed at this point (queue-size exceeded).
 * </p>
 * 
 * @deprecated we recommend against the Job Executor
 */
@Deprecated
@Internal
public interface SpringRejectedJobsHandler {

  public void jobRejected(AsyncExecutor asyncExecutor, Job job);
}
