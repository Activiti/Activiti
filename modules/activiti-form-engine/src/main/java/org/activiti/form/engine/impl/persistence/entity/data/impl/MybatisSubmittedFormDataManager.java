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
package org.activiti.form.engine.impl.persistence.entity.data.impl;

import java.util.List;

import org.activiti.form.api.SubmittedForm;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.Page;
import org.activiti.form.engine.impl.SubmittedFormQueryImpl;
import org.activiti.form.engine.impl.persistence.entity.SubmittedFormEntity;
import org.activiti.form.engine.impl.persistence.entity.SubmittedFormEntityImpl;
import org.activiti.form.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.form.engine.impl.persistence.entity.data.SubmittedFormDataManager;

/**
 * @author Tijs Rademakers
 */
public class MybatisSubmittedFormDataManager extends AbstractDataManager<SubmittedFormEntity> implements SubmittedFormDataManager {
  
  public MybatisSubmittedFormDataManager(FormEngineConfiguration formEngineConfiguration) {
    super(formEngineConfiguration);
  }

  @Override
  public Class<? extends SubmittedFormEntity> getManagedEntityClass() {
    return SubmittedFormEntityImpl.class;
  }
  
  @Override
  public SubmittedFormEntity create() {
    return new SubmittedFormEntityImpl();
  }
  
  @Override
  public long findSubmittedFormCountByQueryCriteria(SubmittedFormQueryImpl submittedFormQuery) {
    return (Long) getDbSqlSession().selectOne("selectSubmittedFormCountByQueryCriteria", submittedFormQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<SubmittedForm> findSubmittedFormsByQueryCriteria(SubmittedFormQueryImpl submittedFormQuery, Page page) {
    final String query = "selectSubmittedFormsByQueryCriteria";
    return getDbSqlSession().selectList(query, submittedFormQuery, page);
  }
}
