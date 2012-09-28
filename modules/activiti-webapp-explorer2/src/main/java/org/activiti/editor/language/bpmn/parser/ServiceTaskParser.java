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
package org.activiti.editor.language.bpmn.parser;

import org.activiti.editor.language.bpmn.model.BaseElement;
import org.activiti.editor.language.bpmn.model.ServiceTask;
import org.apache.commons.lang.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class ServiceTaskParser extends BaseBpmnElementParser {
  
  public static String getElementName() {
    return "serviceTask";
  }

  protected BaseElement parseElement() {
		ServiceTask serviceTask = new ServiceTask();
		if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "class"))) {
			serviceTask.setImplementationType(CLASS_TYPE);
			serviceTask.setImplementation(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "class"));
			
		} else if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "expression"))) {
			serviceTask.setImplementationType(EXPRESSION_TYPE);
			serviceTask.setImplementation(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "expression"));
		} else if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "delegateExpression"))) {
			serviceTask.setImplementationType(DELEGATE_EXPRESSION_TYPE);
			serviceTask.setImplementation(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "delegateExpression"));
		}
	
		serviceTask.setResultVariableName(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "resultVariableName"));
	
		parseChildElements(getElementName(), serviceTask);
		
		return serviceTask;
  }
}
