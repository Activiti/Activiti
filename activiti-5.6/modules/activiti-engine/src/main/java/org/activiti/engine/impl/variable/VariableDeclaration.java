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
package org.activiti.engine.impl.variable;

import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;


/**
 * @author Tom Baeyens
 */
public class VariableDeclaration implements Serializable {

  private static final long serialVersionUID = 1L;
  
  protected String name;
  protected String type;
  protected String sourceVariableName;
  protected Expression sourceExpression;
  protected String destinationVariableName;
  protected Expression destinationExpression;
  protected String link;
  protected Expression linkExpression;
  

  public void initialize(VariableScope innerScopeInstance, VariableScope outerScopeInstance) {
    if (sourceVariableName!=null) {
      if (outerScopeInstance.hasVariable(sourceVariableName)) {
        Object value = outerScopeInstance.getVariable(sourceVariableName);
        innerScopeInstance.setVariable(destinationVariableName, value);      
      } else {
        throw new ActivitiException("Couldn't create variable '" 
                + destinationVariableName + "', since the source variable '"
                + sourceVariableName + "does not exist");
      }
    }
    
    if (sourceExpression!=null) {
      Object value = sourceExpression.getValue(outerScopeInstance);
      innerScopeInstance.setVariable(destinationVariableName, value);
    }
    
    if (link!=null) {
      if (outerScopeInstance.hasVariable(sourceVariableName)) {
        Object value = outerScopeInstance.getVariable(sourceVariableName);
        innerScopeInstance.setVariable(destinationVariableName, value);
      } else {
        throw new ActivitiException("Couldn't create variable '" + destinationVariableName + "', since the source variable '" + sourceVariableName
                + "does not exist");
      }
    }

    if (linkExpression!=null) {
      Object value = sourceExpression.getValue(outerScopeInstance);
      innerScopeInstance.setVariable(destinationVariableName, value);
    }

  }
  
  public void destroy(VariableScope innerScopeInstance, VariableScope outerScopeInstance) {
    
    if (destinationVariableName!=null) {
      if (innerScopeInstance.hasVariable(sourceVariableName)) {
        Object value = innerScopeInstance.getVariable(sourceVariableName);
        outerScopeInstance.setVariable(destinationVariableName, value);
      } else {
        throw new ActivitiException("Couldn't destroy variable " + sourceVariableName + ", since it does not exist");
      }
    }

    if (destinationExpression!=null) {
      Object value = destinationExpression.getValue(innerScopeInstance);
      outerScopeInstance.setVariable(destinationVariableName, value);
    }
    
    if (link!=null) {
      if (innerScopeInstance.hasVariable(sourceVariableName)) {
        Object value = innerScopeInstance.getVariable(sourceVariableName);
        outerScopeInstance.setVariable(destinationVariableName, value);
      } else {
        throw new ActivitiException("Couldn't destroy variable " + sourceVariableName + ", since it does not exist");
      }
    }

    if (linkExpression!=null) {
      Object value = sourceExpression.getValue(innerScopeInstance);
      outerScopeInstance.setVariable(destinationVariableName, value);
    }
  }
  
  public VariableDeclaration(String name, String type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public String toString() {
    return "VariableDeclaration[" + name + ":" + type + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getSourceVariableName() {
    return sourceVariableName;
  }
  
  public void setSourceVariableName(String sourceVariableName) {
    this.sourceVariableName = sourceVariableName;
  }

  public Expression getSourceExpression() {
    return sourceExpression;
  }
  
  public void setSourceExpression(Expression sourceExpression) {
    this.sourceExpression = sourceExpression;
  }
  
  public String getDestinationVariableName() {
    return destinationVariableName;
  }
  
  public void setDestinationVariableName(String destinationVariableName) {
    this.destinationVariableName = destinationVariableName;
  }

  public Expression getDestinationExpression() {
    return destinationExpression;
  }
  
  public void setDestinationExpression(Expression destinationExpression) {
    this.destinationExpression = destinationExpression;
  }
  
  public String getLink() {
    return link;
  }
  
  public void setLink(String link) {
    this.link = link;
  }
  
  public Expression getLinkExpression() {
    return linkExpression;
  }

  public void setLinkExpression(Expression linkExpression) {
    this.linkExpression = linkExpression;
  }
}
