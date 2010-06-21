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
package org.activiti.test.json;

import java.util.List;

import org.activiti.ProcessDefinition;
import org.activiti.ProcessService;
import org.activiti.json.JsonListConverter;
import org.activiti.json.JsonProcessDefinitionConverter;
import org.activiti.test.ActivitiTestCase;
import org.junit.Test;


/**
 * @author Tom Baeyens
 */
public class JsonTest extends ActivitiTestCase {

  @Test
  public void testJson() {
    deployProcessString(
      "<definitions xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL'" +
      "             targetNamespace='http://www.activiti.org/bpmn2.0'>" +
      "  <process id='LoanProcess' >" +
      "    <startEvent id='theStart' />" +
      "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='theTask' />" +
      "    <userTask id='theTask' name='my task' />" +
      "    <sequenceFlow id='flow2' sourceRef='theTask' targetRef='theEnd' />" +
      "    <endEvent id='theEnd' />" +
      "  </process>" +
      "</definitions>"
    );
    
    deployProcessString(
      "<definitions xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL'" +
      "             targetNamespace='http://www.activiti.org/bpmn2.0'>" +
      "  <process id='ExpenseNoteProcess' >" +
      "    <startEvent id='theStart' />" +
      "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='theTask' />" +
      "    <userTask id='theTask' name='my task' />" +
      "    <sequenceFlow id='flow2' sourceRef='theTask' targetRef='theEnd' />" +
      "    <endEvent id='theEnd' />" +
      "  </process>" +
      "</definitions>"
    );
          
    ProcessService processService = processEngine.getProcessService();
    List<ProcessDefinition> processDefinitions = processService.findProcessDefinitions();
    
    JsonListConverter<ProcessDefinition> jsonListConverter = new JsonListConverter<ProcessDefinition>(new JsonProcessDefinitionConverter());
    System.out.println(jsonListConverter.toJson(processDefinitions, 2));
  }
}
