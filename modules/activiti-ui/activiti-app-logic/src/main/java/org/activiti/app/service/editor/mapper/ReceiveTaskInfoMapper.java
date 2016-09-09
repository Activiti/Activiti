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
package org.activiti.app.service.editor.mapper;

import org.activiti.bpmn.model.ReceiveTask;

public class ReceiveTaskInfoMapper extends AbstractInfoMapper {

	protected void mapProperties(Object element) {
		ReceiveTask receiveTask = (ReceiveTask) element;
		createListenerPropertyNodes("Execution listeners", receiveTask.getExecutionListeners());
	}
}
