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
package com.activiti.repository.runtime;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.activiti.domain.runtime.Form;

public interface FormRepository extends JpaRepository<Form, Long> {
	
	Page<Form> findAllByNameLike(String nameLike, Pageable pageRequest);

	List<Form> findByAppDeploymentId(Long appDeploymentId);
	
	@Modifying
    @Query(value="delete from Form form where form.appDefinitionId = ?")
    void deleteInBatchByAppId(Long appId);
}
