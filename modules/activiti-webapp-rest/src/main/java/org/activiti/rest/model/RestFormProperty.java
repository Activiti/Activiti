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

package org.activiti.rest.model;

import java.io.Serializable;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.form.FormPropertyHandler;
import org.activiti.engine.impl.form.FormPropertyImpl;

/**
 * 
 * @author Stefan Schröder
 */
public class RestFormProperty extends FormPropertyImpl implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private String formType;

  public RestFormProperty(FormProperty formProperty) {
    super(new FormPropertyHandler());
    this.id = formProperty.getId();
    this.name = formProperty.getName();
    this.value = formProperty.getValue();
    if (formProperty.getType() != null)
      this.formType = formProperty.getType().getName();
    else
      this.formType = null;
    this.isRequired = formProperty.isRequired();
    this.isReadable = formProperty.isReadable();
    this.isWritable = formProperty.isWritable();
  }

  public String getFormType() {
    return formType;
  }
}
