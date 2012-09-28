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

import javax.xml.stream.XMLStreamReader;

import org.activiti.editor.language.bpmn.model.ActivitiListener;
import org.activiti.editor.language.bpmn.model.BaseElement;
import org.apache.commons.lang.StringUtils;

/**
 * @author Tijs Rademakers
 */
public abstract class ActivitiListenerParser extends BaseChildElementParser {
  
	protected ActivitiListener listener;
	
  public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement) throws Exception {
    
    listener = new ActivitiListener();
    if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, "class"))) {
      listener.setImplementation(xtr.getAttributeValue(null, "class"));
      listener.setImplementationType(CLASS_TYPE);
    } else if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, "expression"))) {
      listener.setImplementation(xtr.getAttributeValue(null, "expression"));
      listener.setImplementationType(EXPRESSION_TYPE);
    } else if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, "delegateExpression"))) {
      listener.setImplementation(xtr.getAttributeValue(null, "delegateExpression"));
      listener.setImplementationType(DELEGATE_EXPRESSION_TYPE);
    }
    listener.setEvent(xtr.getAttributeValue(null, "event"));
    
    parseChildElements(xtr, listener, new FieldExtensionParser());
  }
}
