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

package org.activiti.engine.form;

import java.io.Serializable;

import org.activiti.engine.FormService;


/**
 * Represents a single property on a form.
 * 
 * @author Tom Baeyens
 */
public interface FormProperty extends Serializable {
  
  /** The key used to submit the property in {@link FormService#submitStartFormData(String, java.util.Map)} 
   * or {@link FormService#submitTaskFormData(String, java.util.Map)} */
  String getId();
  
  /** The display label */
  String getName();
  
  /** Type of the property. */
  FormType getType();

  /** Optional value that should be used to display in this property */
  String getValue();
  
  /** Is this property read to be displayed in the form and made accessible with the methods 
   * {@link FormService#getStartFormData(String)} and {@link FormService#getTaskFormData(String)}. */
  boolean isReadable();

  /** Is this property expected when a user submits the form? */
  boolean isWritable();

  /** Is this property a required input field */
  boolean isRequired();
}
