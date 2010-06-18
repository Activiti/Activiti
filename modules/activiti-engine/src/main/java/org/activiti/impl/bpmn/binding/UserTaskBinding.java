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
package org.activiti.impl.bpmn.binding;

import org.activiti.impl.bpmn.UserTaskActivity;
import org.activiti.impl.bpmn.parser.BpmnParse;
import org.activiti.impl.xml.Element;


/**
 * @author Joram Barrez
 */
public class UserTaskBinding extends TaskBinding {
  
  protected static final String TAG_NAME  = "userTask";

  public boolean matches(Element element, BpmnParse bpmnParse) {
    return TAG_NAME.equals(element.getTagName());
  }

  public Object parse(Element element, BpmnParse bpmnParse) {
    UserTaskActivity userTaskActivity = new UserTaskActivity();
    userTaskActivity.setTaskDefinition(parseTaskDefinition(element, bpmnParse));
    return userTaskActivity;
  }
  
  // Resource assig

}
