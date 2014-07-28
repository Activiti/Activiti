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
package org.activiti.workflow.simple.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.activiti.workflow.simple.converter.listener.DefaultWorkflowDefinitionConversionListener;
import org.activiti.workflow.simple.converter.listener.WorkflowDefinitionConversionListener;
import org.activiti.workflow.simple.converter.step.ChoiceStepsDefinitionConverter;
import org.activiti.workflow.simple.converter.step.DelayStepDefinitionConverter;
import org.activiti.workflow.simple.converter.step.FeedbackStepDefinitionConverter;
import org.activiti.workflow.simple.converter.step.HumanStepDefinitionConverter;
import org.activiti.workflow.simple.converter.step.ParallelStepsDefinitionConverter;
import org.activiti.workflow.simple.converter.step.ScriptStepDefinitionConverter;
import org.activiti.workflow.simple.converter.step.StepDefinitionConverter;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.definition.WorkflowDefinition;

/**
 * Factory that is capable of creating {@link WorkflowDefinitionConversion} objects.
 * 
 * It is necessary for a correct conversion to set (or inject) {@link StepDefinition} converters
 * (instances of {@link StepDefinitionConverter}) and conversion life cycle listeners
 * (instance of {@link WorkflowDefinitionConversionListener}). 
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class WorkflowDefinitionConversionFactory {

  private static final long serialVersionUID = 229288964476630200L;
  
  protected HashMap<Class< ? >, StepDefinitionConverter> defaultStepConverters;
  protected HashMap<Class< ? >, StepDefinitionConverter> stepConverters;
  
  protected List<WorkflowDefinitionConversionListener> defaultWorkflowDefinitionConversionListeners;
  protected List<WorkflowDefinitionConversionListener> workflowDefinitionConversionListeners;
  protected List<WorkflowDefinitionConversionListener> allWorkflowDefinitionConversionListeners;
  
  public WorkflowDefinitionConversionFactory() {
    initDefaultStepConverters();
    initDefaultWorkflowDefinitionConversionListeners();
  }
  
  @SuppressWarnings("rawtypes")
  protected void initDefaultStepConverters() {
    List<StepDefinitionConverter> converters = new ArrayList<StepDefinitionConverter>();
    converters.add(new ParallelStepsDefinitionConverter());
    converters.add(new ChoiceStepsDefinitionConverter());
    converters.add(new HumanStepDefinitionConverter());
    converters.add(new FeedbackStepDefinitionConverter());
    converters.add(new ScriptStepDefinitionConverter());
    converters.add(new DelayStepDefinitionConverter());
    setDefaultStepDefinitionConverters(converters);
  }
  
  protected void initDefaultWorkflowDefinitionConversionListeners() {
    List<WorkflowDefinitionConversionListener> listeners = new ArrayList<WorkflowDefinitionConversionListener>();
    listeners.add(new DefaultWorkflowDefinitionConversionListener());
    setDefaultWorkflowDefinitionConversionListeners(listeners);
  }

  /**
   * @return a new, empty conversion to be used to store all converted
   *         artifacts.
   */
  public WorkflowDefinitionConversion createWorkflowDefinitionConversion() {
    return new WorkflowDefinitionConversion(this);
  }
  
  public WorkflowDefinitionConversion createWorkflowDefinitionConversion(WorkflowDefinition workflowDefinition) {
    return new WorkflowDefinitionConversion(this, workflowDefinition);
  }
  
  public void setDefaultStepDefinitionConverters(List<StepDefinitionConverter> stepConverters) {
    this.defaultStepConverters = new HashMap<Class<?>, StepDefinitionConverter>();
    for (StepDefinitionConverter converter : stepConverters) {
      this.defaultStepConverters.put(converter.getHandledClass(), converter);
    }
  }

  public void setStepDefinitionConverters(List<StepDefinitionConverter> stepConverters) {
    this.stepConverters = new HashMap<Class< ? >, StepDefinitionConverter>();
    for (StepDefinitionConverter converter : stepConverters) {
      this.stepConverters.put(converter.getHandledClass(), converter);
    }
  }

  /**
   * Returns all {@link WorkflowDefinitionConversionListener} instances, both the injected
   * and the default ones.
   */
  public List<WorkflowDefinitionConversionListener> getAllWorkflowDefinitionConversionListeners() {
    if (allWorkflowDefinitionConversionListeners == null) {
      allWorkflowDefinitionConversionListeners = new ArrayList<WorkflowDefinitionConversionListener>();
      
      if (defaultWorkflowDefinitionConversionListeners != null) {
        allWorkflowDefinitionConversionListeners.addAll(defaultWorkflowDefinitionConversionListeners);
      }
      
      if (workflowDefinitionConversionListeners != null) {
        allWorkflowDefinitionConversionListeners.addAll(workflowDefinitionConversionListeners);
      }
    }
    return allWorkflowDefinitionConversionListeners;
  }

  public void setDefaultWorkflowDefinitionConversionListeners(List<WorkflowDefinitionConversionListener> defaultWorkflowDefinitionConversionListeners) {
    this.defaultWorkflowDefinitionConversionListeners = defaultWorkflowDefinitionConversionListeners;
    this.allWorkflowDefinitionConversionListeners = null;
  }

  public void setWorkflowDefinitionConversionListeners(List<WorkflowDefinitionConversionListener> workflowDefinitionConversionListeners) {
    this.workflowDefinitionConversionListeners = workflowDefinitionConversionListeners;
    this.allWorkflowDefinitionConversionListeners = null;
  }

  /**
   * @param definition
   *          {@link StepDefinition} to get converter for. First, the injected list
   *          of {@link StepDefinitionConverter} is checked, before falling back 
   *          to the default list of {@link StepDefinitionConverter}.
   * @return Converter that can be used on the given definition.
   * @throws IllegalArgumentException
   *           when there is no converter known for the given definition.
   */
  public StepDefinitionConverter getStepConverterFor(StepDefinition definition) {
    StepDefinitionConverter converter = null; 
            
    if (stepConverters != null) {
      converter = stepConverters.get(definition.getClass());
    }
    
    if (converter == null && defaultStepConverters != null) {
      converter = defaultStepConverters.get(definition.getClass());
    }
    
    if (converter == null) {
      // TODO: i18n and error-handling
      throw new IllegalArgumentException("No converter found for step: " + definition.getClass());
    }
    
    return converter;
  }
}
