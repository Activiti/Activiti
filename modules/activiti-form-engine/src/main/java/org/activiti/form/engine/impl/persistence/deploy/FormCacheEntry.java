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
package org.activiti.form.engine.impl.persistence.deploy;

import java.io.Serializable;

import org.activiti.form.engine.impl.persistence.entity.FormEntity;
import org.activiti.form.model.FormDefinition;

/**
 * @author Tijs Rademakers
 */
public class FormCacheEntry implements Serializable {

  private static final long serialVersionUID = 1L;

  protected FormEntity formEntity;
  protected FormDefinition formDefinition;

  public FormCacheEntry(FormEntity formEntity, FormDefinition formDefinition) {
    this.formEntity = formEntity;
    this.formDefinition = formDefinition;
  }

  public FormEntity getFormEntity() {
    return formEntity;
  }

  public void setFormEntity(FormEntity formEntity) {
    this.formEntity = formEntity;
  }

  public FormDefinition getFormDefinition() {
    return formDefinition;
  }

  public void setFormDefinition(FormDefinition formDefinition) {
    this.formDefinition = formDefinition;
  }
}
