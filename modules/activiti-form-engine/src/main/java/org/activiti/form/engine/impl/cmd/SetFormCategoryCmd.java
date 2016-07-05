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
package org.activiti.form.engine.impl.cmd;

import org.activiti.form.engine.ActivitiFormIllegalArgumentException;
import org.activiti.form.engine.ActivitiFormObjectNotFoundException;
import org.activiti.form.engine.impl.interceptor.Command;
import org.activiti.form.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.form.engine.impl.persistence.deploy.FormCacheEntry;
import org.activiti.form.engine.impl.persistence.entity.FormEntity;

/**
 * @author Joram Barrez
 */
public class SetFormCategoryCmd implements Command<Void> {

  protected String formId;
  protected String category;

  public SetFormCategoryCmd(String formId, String category) {
    this.formId = formId;
    this.category = category;
  }

  public Void execute(org.activiti.form.engine.impl.interceptor.CommandContext commandContext) {

    if (formId == null) {
      throw new ActivitiFormIllegalArgumentException("Form id is null");
    }

    FormEntity form = commandContext.getFormEntityManager().findById(formId);

    if (form == null) {
      throw new ActivitiFormObjectNotFoundException("No form found for id = '" + formId + "'");
    }

    // Update category
    form.setCategory(category);

    // Remove form from cache, it will be refetched later
    DeploymentCache<FormCacheEntry> formCache = commandContext.getFormEngineConfiguration().getFormCache();
    if (formCache != null) {
      formCache.remove(formId);
    }
    
    commandContext.getFormEntityManager().update(form);

    return null;
  }

  public String getFormId() {
    return formId;
  }

  public void setFormId(String formId) {
    this.formId = formId;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

}
