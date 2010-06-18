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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.Configuration;
import org.activiti.impl.xml.Element;


/**
 * @author Tom Baeyens
 */
public class TransactionalObjectDescriptorsBinding implements ConfigurationBinding {

  public boolean matches(Element element, ConfigurationParse configurationParse) {
    return "transactional-object-descriptors".equals(element.getTagName());
  }

  public Object parse(Element element, ConfigurationParse configurationParse) {

    for (Element listElement: element.elements()) {
      Object descriptor = configurationParse.parseObject(listElement);
      
      Map<String, Object> configurationsMap = configurationParse
          .getConfiguration()
          .getConfigurations();
      
      List<Object> descriptors = (List<Object>) configurationsMap.get(Configuration.NAME_TRANSACTIONALOBJECTDESCRIPTORS);
      
      if (descriptors==null) {
        descriptors = new ArrayList<Object>();
        configurationsMap.put(Configuration.NAME_TRANSACTIONALOBJECTDESCRIPTORS, descriptors);
      }
      
      descriptors.add(descriptor);
    }
    
    return null;
  }
}
