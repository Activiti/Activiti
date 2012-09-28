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
package org.activiti.editor.language.bpmn.export;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class ScriptTaskExport extends BaseActivityExport {

  protected String getElementName() {
    return "scriptTask";
  }

  protected void writeAdditionalAttributes(ObjectNode objectNode, 
      IndentingXMLStreamWriter xtw, ObjectNode modelNode) throws Exception {
    
   String scriptFormat = getPropertyValueAsString(PROPERTY_SCRIPT_FORMAT, objectNode);
    
    if (StringUtils.isNotEmpty(scriptFormat)) {
      xtw.writeAttribute("scriptFormat", scriptFormat);
    }
    
    super.writeAdditionalAttributes(objectNode, xtw, modelNode);
  }
  
  protected void writeAdditionalChildElements(ObjectNode objectNode,
      IndentingXMLStreamWriter xtw, ObjectNode modelNode) throws Exception {
	  
  	String script = getPropertyValueAsString(PROPERTY_SCRIPT_TEXT, objectNode);
  	if (StringUtils.isNotEmpty(script)) {
			xtw.writeStartElement("script");
			xtw.writeCharacters(script);
			xtw.writeEndElement();
		}
  	
  	super.writeAdditionalChildElements(objectNode, xtw, modelNode);
  }
}
