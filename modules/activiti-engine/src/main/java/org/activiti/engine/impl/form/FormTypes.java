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

package org.activiti.engine.impl.form;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.util.xml.Element;


/**
 * @author Tom Baeyens
 */
public class FormTypes {

  protected Map<String, AbstractFormType> formTypes = new HashMap<String, AbstractFormType>();

  public void addFormType(AbstractFormType formType) {
    formTypes.put(formType.getName(), formType);
  }

  public AbstractFormType parseFormPropertyType(Element formPropertyElement, BpmnParse bpmnParse) {
    AbstractFormType formType = null;

    String typeText = formPropertyElement.attribute("type");
    String datePatternText = formPropertyElement.attribute("datePattern");
    
    if ("date".equals(typeText) && datePatternText!=null) {
      formType = new DateFormType(datePatternText);
      
    } else if ("enum".equals(typeText)) {
      Map<String, String> values = new HashMap<String, String>();
      for (Element valueElement: formPropertyElement.elementsNS(BpmnParser.BPMN_EXTENSIONS_NS,"value")) {
        String valueId = valueElement.attribute("id");
        String valueName = valueElement.attribute("name");
        values.put(valueId, valueName);
      }
      formType = new EnumFormType(values);
      
    } else if (typeText!=null) {
      formType = formTypes.get(typeText);
      if (formType==null) {
        bpmnParse.addError("unknown type '"+typeText+"'", formPropertyElement);
      }
    }
    return formType;
  }
}
