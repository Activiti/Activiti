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

import java.util.List;

import org.activiti.impl.CmdExecutor;
import org.activiti.impl.interceptor.Interceptor;
import org.activiti.impl.xml.Element;


/**
 * @author Tom Baeyens
 */
public class CommandExecutorBinding implements ConfigurationBinding {

  private static final ListBinding listBinding = new ListBinding();
  
  public boolean matches(Element element, ConfigurationParse configurationParse) {
    return "command-executor".equals(element.getTagName());
  }

  public Object parse(Element element, ConfigurationParse configurationParse) {
    List<Object> list = (List<Object>) listBinding.parse(element, configurationParse);
    for (int i=0; i<list.size()-1; i++) {
      Interceptor interceptor = (Interceptor) list.get(i);
      CmdExecutor next = (CmdExecutor) list.get(i+1);
      interceptor.setNext(next);
    }
    return list.get(0);
  }
}
