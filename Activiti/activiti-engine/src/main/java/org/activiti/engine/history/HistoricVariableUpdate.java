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

package org.activiti.engine.history;

import org.activiti.engine.api.internal.Internal;

/**
 * Update of a process variable. This is only available if history level is configured to FULL.
 *
 */
@Internal
public interface HistoricVariableUpdate extends HistoricDetail {

  String getVariableName();

  String getVariableTypeName();

  Object getValue();

  int getRevision();
}
