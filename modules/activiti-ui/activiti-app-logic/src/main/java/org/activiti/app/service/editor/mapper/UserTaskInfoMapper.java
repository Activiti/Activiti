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

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class UserTaskInfoMapper extends AbstractInfoMapper {

	protected void mapProperties(Object element) {
		UserTask userTask = (UserTask) element;
		createPropertyNode("Assignee", userTask.getAssignee());
		createPropertyNode("Candidate users", userTask.getCandidateUsers());
		createPropertyNode("Candidate groups", userTask.getCandidateGroups());
		createPropertyNode("Due date", userTask.getDueDate());
		createPropertyNode("Form key", userTask.getFormKey());
		createPropertyNode("Priority", userTask.getPriority());
		if (CollectionUtils.isNotEmpty(userTask.getFormProperties())) {
		    List<String> formPropertyValues = new ArrayList<String>();
		    for (FormProperty formProperty : userTask.getFormProperties()) {
		        StringBuilder propertyBuilder = new StringBuilder();
		        if (StringUtils.isNotEmpty(formProperty.getName())) {
		            propertyBuilder.append(formProperty.getName());
		        } else {
		            propertyBuilder.append(formProperty.getId());
		        }
		        if (StringUtils.isNotEmpty(formProperty.getType())) {
		            propertyBuilder.append(" - ");
		            propertyBuilder.append(formProperty.getType());
		        }
		        if (formProperty.isRequired()) {
		            propertyBuilder.append(" (required)");
		        } else {
		            propertyBuilder.append(" (not required)");
		        }
                formPropertyValues.add(propertyBuilder.toString());
            }
		    createPropertyNode("Form properties", formPropertyValues);
		}
		createListenerPropertyNodes("Task listeners", userTask.getTaskListeners());
		createListenerPropertyNodes("Execution listeners", userTask.getExecutionListeners());
	}
}
