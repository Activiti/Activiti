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
package org.activiti.impl.cfg;

import org.activiti.impl.util.ReflectUtil;
import org.activiti.impl.xml.Element;


/**
 * @author Tom Baeyens
 */
public class ObjectBinding implements ConfigurationBinding {

  public boolean matches(Element element, ConfigurationParse configurationParse) {
    return "object".equals(element.getTagName());
  }

  public Object parse(Element element, ConfigurationParse configurationParse) {
    Object object = null;
    String className = element.attribute("class");
    if (className==null) {
      configurationParse.addProblem("no attribute 'class' in element 'object'", element);
    } else {
      object = ReflectUtil.instantiate(className);
      
      for (Element propertyElement: element.elements("property")) {
        Element valueElement = propertyElement.elements().get(0);
        Object value = configurationParse.parseObject(valueElement);
        String name = propertyElement.attribute("name");
        String setter = "set"+name.substring(0, 1).toUpperCase()+name.substring(1);
        ReflectUtil.invoke(object, setter, new Object[]{value});
      }
    }
    return object;
  }
}
