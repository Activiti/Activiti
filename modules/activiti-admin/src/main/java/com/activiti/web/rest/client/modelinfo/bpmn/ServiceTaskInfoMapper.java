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

import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;

import com.activiti.web.rest.client.modelinfo.AbstractInfoMapper;

public class ServiceTaskInfoMapper extends AbstractInfoMapper {

	protected void mapProperties(Object element) {
		ServiceTask serviceTask = (ServiceTask) element;
		if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(serviceTask.getImplementationType())) {
			createPropertyNode("Class", serviceTask.getImplementation());
		} else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(serviceTask.getImplementationType())) {
			createPropertyNode("Expression", serviceTask.getImplementation());
		} else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(serviceTask.getImplementationType())) {
			createPropertyNode("Delegate expression", serviceTask.getImplementation());
		}
		createPropertyNode("Result variable name", serviceTask.getResultVariableName());
	}
}
