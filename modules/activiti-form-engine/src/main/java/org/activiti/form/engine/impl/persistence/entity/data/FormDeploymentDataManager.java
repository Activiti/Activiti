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
package org.activiti.form.engine.impl.persistence.entity.data;

import java.util.List;
import java.util.Map;

import org.activiti.form.api.FormDeployment;
import org.activiti.form.engine.impl.FormDeploymentQueryImpl;
import org.activiti.form.engine.impl.Page;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntity;

/**
 * @author Joram Barrez
 */
public interface FormDeploymentDataManager extends DataManager<FormDeploymentEntity> {
  
  FormDeploymentEntity findLatestDeploymentByName(String deploymentName);

  long findDeploymentCountByQueryCriteria(FormDeploymentQueryImpl deploymentQuery);

  List<FormDeployment> findDeploymentsByQueryCriteria(FormDeploymentQueryImpl deploymentQuery, Page page);

  List<String> getDeploymentResourceNames(String deploymentId);

  List<FormDeployment> findDeploymentsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults);

  long findDeploymentCountByNativeQuery(Map<String, Object> parameterMap);

}
