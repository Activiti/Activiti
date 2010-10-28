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
package org.activiti.engine.impl.bpmn;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.transformer.Identity;
import org.activiti.engine.impl.transformer.Transformer;
import org.activiti.engine.impl.webservice.SyncWebServiceClient;


/**
 * An activity behavior that allows calling Web services
 * 
 * @author Esteban Robles Luna
 */
public class WebServiceActivityBehavior implements ActivityBehavior {

  protected Operation operation;
  
  protected List<String> inVariableNames;
  
  protected List<String> outVariableNames;
  
  protected List<Transformer> inTransformers;
  
  protected List<Transformer> outTransformers;
  
  public WebServiceActivityBehavior(Operation operation) {
    this.operation = operation;
    this.inTransformers = new ArrayList<Transformer>();
    this.outTransformers = new ArrayList<Transformer>();
    this.inVariableNames = new ArrayList<String>();
    this.outVariableNames = new ArrayList<String>();
  }
  
  /**
   * {@inheritDoc}
   */
  public void execute(ActivityExecution execution) throws Exception {
    this.fillTransformersIfEmpty();
    this.verifySameSizeOfTransformersAndArguments();
    
    Object[] arguments = this.getArguments(execution);
    Object[] transformedArguments = this.transform(arguments);
    Object[] results = this.execute(transformedArguments);
    
    this.verifyResultsAccordingToOutMessage(results);
    
    Object[] transformedResults = this.transformResults(results);
    this.store(transformedResults, execution);
  }
  
  private void fillTransformersIfEmpty() {
    if (this.inTransformers.isEmpty() && this.getInStructure().getFieldSize() > 0) {
      for (int i = 0; i < this.getInStructure().getFieldSize(); i++) {
        this.inTransformers.add(Identity.getInstance());
      }
    }

    if (this.getOutStructure() != null && this.outTransformers.isEmpty() 
            && this.getOutStructure().getFieldSize() > 0) {
      for (int i = 0; i < this.getOutStructure().getFieldSize(); i++) {
        this.outTransformers.add(Identity.getInstance());
      }
    }
  }

  public void addInTransformer(Transformer transformer) {
    this.inTransformers.add(transformer);
  }

  public void addOutTransformer(Transformer transformer) {
    this.outTransformers.add(transformer);
  }
  
  public void addInVariable(String variableName) {
    this.inVariableNames.add(variableName);
  }

  public void addOutVariable(String variableName) {
    this.outVariableNames.add(variableName);
  }

  private void verifySameSizeOfTransformersAndArguments() {
    if (this.getInStructure().getFieldSize() != this.inTransformers.size()) {
      throw new ActivitiException("The size of IN arguments and transformers does not match");
    }
    
    if (this.getOutStructure() != null && this.getOutStructure().getFieldSize() != this.outTransformers.size()) {
      throw new ActivitiException("The size of OUT arguments and transformers does not match");
    }
  }
  
  private void verifyResultsAccordingToOutMessage(Object[] results) {
    if (this.getOutStructure() != null && results.length != this.getOutStructure().getFieldSize()) {
      throw new ActivitiException("The result of the Web service call has different size according to the expected message");
    }
  }

  private void store(Object[] transformedResults, ActivityExecution execution) {
    for (int i = 0; i < transformedResults.length; i++) {
      Object transformedResult = transformedResults[i];
      String outVariable = this.outVariableNames.get(i);
      execution.setVariable(outVariable, transformedResult);
    }
  }

  private Object[] transformResults(Object[] results) {
    Object[] transformedResults = new Object[results.length];
    for (int i = 0; i < transformedResults.length; i++) {
      Transformer transformer = this.outTransformers.get(i);
      transformedResults[i] = (Object) transformer.transform(results[i]);
    }
    return transformedResults;
  }

  private Object[] execute(Object[] transformedArguments) throws Exception {
    return this.operation.execute(transformedArguments);
  }

  private Object[] transform(Object[] arguments) {
    Object[] transformedArguments = new Object[arguments.length];
    for (int i = 0; i < transformedArguments.length; i++) {
      Transformer transformer = this.inTransformers.get(i);
      transformedArguments[i] = transformer.transform(arguments[i]);
    }
    return transformedArguments;
  }

  private Object[] getArguments(ActivityExecution execution) {
    Object[] arguments = new Object[this.getInStructure().getFieldSize()];
    for (int i = 0; i < arguments.length; i++) {
      String inVariable = this.inVariableNames.get(i);
      arguments[i] = execution.getVariable(inVariable);
    }
    return arguments;
  }
  
  private Structure getInStructure() {
    return this.operation.getInMessage().getStructure();
  }

  private Structure getOutStructure() {
    return this.operation.getOutMessage() == null
      ? null
      : this.operation.getOutMessage().getStructure();
  }
}
