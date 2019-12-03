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

package org.activiti.engine.impl.bpmn.behavior;

/**
 * Implementation of the BPMN 2.0 'manual task': a task that is external to the BPMS and to which there is no reference to IT systems whatsoever.
 * 
 * Given this definition, this activity will behave simply as a pass-though step in the process.
 * 

 */
public class ManualTaskActivityBehavior extends TaskActivityBehavior {

  private static final long serialVersionUID = 1L;

}
