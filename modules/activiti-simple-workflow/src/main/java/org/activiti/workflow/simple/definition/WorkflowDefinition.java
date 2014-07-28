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

import java.util.HashMap;
import java.util.Map;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.form.FormDefinition;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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

  protected String key;
  protected String name;
  protected String description;
  protected String category;
  protected FormDefinition startFormDefinition;
  protected ParallelStepsDefinition currentParallelStepsDefinition;
  protected ChoiceStepsDefinition currentChoiceStepsDefinition;
  
  protected Map<String, Object> parameters = new HashMap<String, Object>();

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
  
  public String getCategory() {
	return category;
  }

  public void setCategory(String category) {
	this.category = category;
  }
  
  public WorkflowDefinition category(String category) {
	  setCategory(category);
	  return this;
  }

  @JsonInclude(Include.NON_EMPTY)
  public Map<String, Object> getParameters() {
	  return parameters;
  }
  
  public void setParameters(Map<String, Object> parameters) {
	  this.parameters = parameters;
  }
  
  public ParallelStepsDefinition inParallel() {
    currentParallelStepsDefinition = new ParallelStepsDefinition(this);
    addStep(currentParallelStepsDefinition);
    return currentParallelStepsDefinition;
  }
  
  public ChoiceStepsDefinition inChoice() {
    currentChoiceStepsDefinition = new ChoiceStepsDefinition(this);
    addStep(currentChoiceStepsDefinition);
    return currentChoiceStepsDefinition;
  }

  public FormDefinition getStartFormDefinition() {
	  return startFormDefinition;
  }
  
  public void setStartFormDefinition(FormDefinition startFormDefinition) {
	  this.startFormDefinition = startFormDefinition;
  }
  
  public WorkflowDefinition startFormdefinition(FormDefinition startFormDefinition) {
  	this.startFormDefinition = startFormDefinition;
  	return this;
  }
}
