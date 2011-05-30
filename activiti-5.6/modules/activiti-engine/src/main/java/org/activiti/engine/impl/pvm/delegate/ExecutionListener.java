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
package org.activiti.engine.impl.pvm.delegate;



/**
 * @deprecated use {@link org.activiti.delegate.ExecutionListener} instead.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
@Deprecated
public interface ExecutionListener {

  String EVENTNAME_START = "start";
  String EVENTNAME_END = "end";
  String EVENTNAME_TAKE = "take";

  void notify(ExecutionListenerExecution execution) throws Exception;
}
