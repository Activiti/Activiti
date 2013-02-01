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
package org.activiti.workflow.simple.definition;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;

/**
 * Allows to create simple workflows through an easy, fluent Java API.
 * 
 * For example:
 * 
 *  WorkflowDefinition workflowDefinition = new WorkflowDefinition()
 *    .name("testWorkflow")
 *    .description("This is a test workflow")
 *    .inParallel()
 *      .addHumanStep("first task", "kermit")
 *      .addHumanStep("second step", "gonzo")
 *      .addHumanStep("thrid task", "mispiggy")
 *     .endParallel()
 *    .addHumanStep("Task in between", "kermit")
 *    .inParallel()
 *      .addHumanStep("fourth task", "gonzo")
 *      .addHumanStep("fifth step", "gonzo")
 *    .endParallel();
 *    
 * Feed this {@link WorkflowDefinition} instance to a {@link WorkflowDefinitionConversion}
 * and it will convert it a to a {@link BpmnModel}, which in it turn can be used
 * to generate valid BPMN 2.0 through the {@link BpmnXMLConverter}.
 * 
 * The reason why we're not just using the {@link BpmnModel} and it's related 
 * classes to generate bpmn 2.0 xml, is because this class and it's related classes 
 * are a layer on top of them, allowing to easily create patterns. Such patterns are 
 * for example a parallel block ({@link ParallelStepsDefinition}) or a choice step.
 * These can be expressed in their {@link BpmnModel} counterpart of course,
 * but these abstraction are much easier to read and use.
 * 
 * @author Joram Barrez
 */
public class WorkflowDefinition extends AbstractStepDefinitionContainer<WorkflowDefinition> {

  protected String id;
  protected String key;
  protected String name;
  protected String description;
  protected ParallelStepsDefinition currentParallelStepsDefinition;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public WorkflowDefinition name(String name) {
    setName(name);
    return this;
  }
  
  public WorkflowDefinition id(String id) {
    setId(id);
    return this;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getId() {
    return id;
  }
  
  public WorkflowDefinition key(String key) {
    setKey(key);
    return this;
  }
  
  public String getKey() {
    return key;
  }
  
  public void setKey(String key) {
    this.key = key;
  }
  
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
  
  public WorkflowDefinition description(String description) {
    setDescription(description);
    return this;
  }
  
  public ParallelStepsDefinition inParallel() {
    currentParallelStepsDefinition = new ParallelStepsDefinition(this);
    addStep(currentParallelStepsDefinition);
    return currentParallelStepsDefinition;
  }
  
}
