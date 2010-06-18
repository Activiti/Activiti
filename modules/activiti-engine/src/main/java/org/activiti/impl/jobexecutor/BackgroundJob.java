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

import java.util.Collection;

/**
 * An executable wrapper around one or more Jobs,
 *  which need to be executed in the background,
 *  one after another.
 * Can either be a single, non-exclusive or lonely
 *  Job, or a collection of exclusive Jobs.
 */
public interface BackgroundJob extends Runnable {
  public long getCurrentJobId();
  public Collection<Long> getAllJobIds();
  public String toString();
}
