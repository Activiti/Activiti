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
package org.activiti.engine.impl.transformer;

import org.activiti.engine.ActivitiException;

/**
 * An exception that represents an error during the execution of a
 * Transformer
 * 
 * @author Esteban Robles Luna
 */
public class TransformationException extends ActivitiException {

  private static final long serialVersionUID = -7648269175831341968L;

  protected Transformer transformer;
  
  protected Object sourceObject;
  
  public TransformationException(String message, Transformer transformer, Object sourceObject) {
    super(message);
    
    this.transformer = transformer;
    this.sourceObject = sourceObject;
  }
  
  public Transformer getTransformer() {
    return this.transformer;
  }
  
  public Object getSourceObject() {
    return this.sourceObject;
  }
}
