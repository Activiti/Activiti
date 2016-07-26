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

package org.activiti.form.engine.impl.persistence.entity;

import java.util.List;

import org.activiti.form.api.SubmittedForm;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.Page;
import org.activiti.form.engine.impl.SubmittedFormQueryImpl;
import org.activiti.form.engine.impl.persistence.entity.data.DataManager;
import org.activiti.form.engine.impl.persistence.entity.data.SubmittedFormDataManager;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class SubmittedFormEntityManagerImpl extends AbstractEntityManager<SubmittedFormEntity> implements SubmittedFormEntityManager {

  protected SubmittedFormDataManager submittedFormDataManager;
  
  public SubmittedFormEntityManagerImpl(FormEngineConfiguration formEngineConfiguration, SubmittedFormDataManager submittedFormDataManager) {
    super(formEngineConfiguration);
    this.submittedFormDataManager = submittedFormDataManager;
  }
  
  @Override
  public long findSubmittedFormCountByQueryCriteria(SubmittedFormQueryImpl submittedFormQuery) {
    return submittedFormDataManager.findSubmittedFormCountByQueryCriteria(submittedFormQuery);
  }

  @Override
  public List<SubmittedForm> findSubmittedFormsByQueryCriteria(SubmittedFormQueryImpl submittedFormQuery, Page page) {
    return submittedFormDataManager.findSubmittedFormsByQueryCriteria(submittedFormQuery, page);
  }

  @Override
  protected DataManager<SubmittedFormEntity> getDataManager() {
    return submittedFormDataManager;
  }

  public SubmittedFormDataManager getSubmittedFormDataManager() {
    return submittedFormDataManager;
  }

  public void setSubmittedFormDataManager(SubmittedFormDataManager submittedFormDataManager) {
    this.submittedFormDataManager = submittedFormDataManager;
  }
  
}
