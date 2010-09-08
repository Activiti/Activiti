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
import org.activiti.engine.impl.transformer.Identity;
import org.activiti.engine.impl.transformer.Transformer;
import org.activiti.engine.impl.webservice.SyncWebServiceClient;
import org.activiti.pvm.activity.ActivityExecution;


/**
 * An activity that allows calling Web services
 * 
 * @author Esteban Robles Luna
 */
public class WebServiceActivity extends BpmnActivityBehavior {

  protected Operation operation;
  
  protected SyncWebServiceClient client;
  
  protected List<Transformer> inTransformers;
  
  protected List<Transformer> outTransformers;
  
  public WebServiceActivity(SyncWebServiceClient client, Operation operation) {
    this.client = client;
    this.operation = operation;
    this.inTransformers = new ArrayList<Transformer>();
    this.outTransformers = new ArrayList<Transformer>();
  }
  
  /**
   * {@inheritDoc}
   */
  public void execute(ActivityExecution execution) throws Exception {
    this.fillTransformersIfEmpty();
    this.verifySameSizeOfTransformersAndArguments();
    
    Object[] arguments = this.getArguments(execution);
    Object[] transformedArguments = this.transform(arguments);
    Object[] results = this.send(transformedArguments);
    Object[] transformedResults = this.transformResults(results);
    this.store(transformedResults, execution);
  }
  
  private void fillTransformersIfEmpty() {
    if (this.inTransformers.isEmpty() && this.operation.getInArgumentsSize() > 0) {
      for (int i = 0; i < this.operation.getInArgumentsSize(); i++) {
        this.inTransformers.add(Identity.getInstance());
      }
    }

    if (this.outTransformers.isEmpty() && this.operation.getOutArgumentsSize() > 0) {
      for (int i = 0; i < this.operation.getOutArgumentsSize(); i++) {
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
  
  private void verifySameSizeOfTransformersAndArguments() {
    if (this.operation.getInArgumentsSize() != this.inTransformers.size()) {
      throw new ActivitiException("The size of IN arguments and transformers does not match");
    }
    
    if (this.operation.getOutArgumentsSize() != this.outTransformers.size()) {
      throw new ActivitiException("The size of OUT arguments and transformers does not match");
    }
  }

  private void store(Object[] transformedResults, ActivityExecution execution) {
    for (int i = 0; i < transformedResults.length; i++) {
      Object transformedResult = transformedResults[i];
      String outVariable = this.operation.getOutArgument(i);
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

  private Object[] send(Object[] transformedArguments) throws Exception {
    return this.client.send(this.operation.getName(), transformedArguments);
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
    Object[] arguments = new Object[this.operation.getInArgumentsSize()];
    for (int i = 0; i < arguments.length; i++) {
      String inVariable = this.operation.getInArgument(i);
      arguments[i] = execution.getVariable(inVariable);
    }
    return arguments;
  }
}
