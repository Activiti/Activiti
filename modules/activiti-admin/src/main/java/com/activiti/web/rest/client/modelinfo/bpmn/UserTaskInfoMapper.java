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
package com.activiti.web.rest.client.modelinfo.bpmn;

import org.activiti.bpmn.model.UserTask;

import com.activiti.web.rest.client.modelinfo.AbstractInfoMapper;

public class UserTaskInfoMapper extends AbstractInfoMapper {

	protected void mapProperties(Object element) {
		UserTask userTask = (UserTask) element;
		createPropertyNode("Assignee", userTask.getAssignee());
		createPropertyNode("Candidate users", userTask.getCandidateUsers());
		createPropertyNode("Candidate groups", userTask.getCandidateGroups());
		createPropertyNode("Due date", userTask.getDueDate());
		createPropertyNode("Form key", userTask.getFormKey());
		createPropertyNode("Priority", userTask.getPriority());
	}
}
