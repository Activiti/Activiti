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
package org.activiti.cdi.annotation.event;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Can be used to qualify events fired when a task is created
 * 
 * <pre>
 * public void beforeCreditAccount(@Observes @CreateTask(&quot;creditAccount&quot;) BusinessProcessEvent evt) {
 *   // ...
 * }
 * </pre>
 * 
 * @author Dimitris Mandalidis
 */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface CreateTask {
  /** the id of the task that has been created */
  public String value();

}
