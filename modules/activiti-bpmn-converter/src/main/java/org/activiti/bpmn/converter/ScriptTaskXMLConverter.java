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
package org.activiti.bpmn.converter;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.child.ScriptTextParser;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ScriptTask;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class ScriptTaskXMLConverter extends BaseBpmnXMLConverter {
  
	public ScriptTaskXMLConverter() {
		ScriptTextParser scriptTextParser = new ScriptTextParser();
		childElementParsers.put(scriptTextParser.getElementName(), scriptTextParser);
	}
	
	public static String getXMLType() {
    return ELEMENT_TASK_SCRIPT;
  }
  
  public static Class<? extends BaseElement> getBpmnElementType() {
    return ScriptTask.class;
  }
  
  @Override
  protected String getXMLElementName() {
    return ELEMENT_TASK_SCRIPT;
  }
  
  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr) throws Exception {
    ScriptTask scriptTask = new ScriptTask();
    BpmnXMLUtil.addXMLLocation(scriptTask, xtr);
    scriptTask.setScriptFormat(xtr.getAttributeValue(null, ATTRIBUTE_TASK_SCRIPT_FORMAT));
    scriptTask.setResultVariable(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SCRIPT_RESULTVARIABLE));
    if (StringUtils.isEmpty(scriptTask.getResultVariable())) {
      scriptTask.setResultVariable(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_RESULTVARIABLE));
    }
    String autoStoreVariables = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SCRIPT_AUTO_STORE_VARIABLE);
    if (StringUtils.isNotEmpty(autoStoreVariables)) {
      scriptTask.setAutoStoreVariables(Boolean.valueOf(autoStoreVariables));
    }
    parseChildElements(getXMLElementName(), scriptTask, xtr);
    return scriptTask;
  }

  @Override
  protected void writeAdditionalAttributes(BaseElement element, XMLStreamWriter xtw) throws Exception {
    ScriptTask scriptTask = (ScriptTask) element;
    writeDefaultAttribute(ATTRIBUTE_TASK_SCRIPT_FORMAT, scriptTask.getScriptFormat(), xtw);
    writeQualifiedAttribute(ATTRIBUTE_TASK_SCRIPT_RESULTVARIABLE, scriptTask.getResultVariable(), xtw);
    writeQualifiedAttribute(ATTRIBUTE_TASK_SCRIPT_AUTO_STORE_VARIABLE, String.valueOf(scriptTask.isAutoStoreVariables()), xtw);
  }
  
  @Override
  protected void writeExtensionChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
  }

  @Override
  protected void writeAdditionalChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
    ScriptTask scriptTask = (ScriptTask) element;
    if (StringUtils.isNotEmpty(scriptTask.getScript())) {
      xtw.writeStartElement(ATTRIBUTE_TASK_SCRIPT_TEXT);
      xtw.writeCharacters(scriptTask.getScript());
      xtw.writeEndElement();
    }
  }
}
