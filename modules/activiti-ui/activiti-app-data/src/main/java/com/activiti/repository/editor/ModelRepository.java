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
package com.activiti.repository.editor;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelShareInfo;
import com.activiti.domain.idm.User;

/**
 * Spring Data JPA repository for the Model entity.
 */
public interface ModelRepository extends JpaRepository<Model, Long> {

    @Query("from Model as model where model.createdBy.id = :user and (model.modelType is null or model.modelType = 0 or model.modelType = 1) and model.referenceId is null")
	List<Model> findProcessesCreatedBy(@Param("user") Long createdBy, Sort sort);
    
    @Query("from Model as model where model.createdBy.id = :user and "
            + "(lower(model.name) like :filter or lower(model.description) like :filter) and (model.modelType is null or model.modelType = 0 or model.modelType = 1) and model.referenceId is null")
    List<Model> findProcessesCreatedBy(@Param("user") Long createdBy, @Param("filter") String filter, Sort sort);
    
    @Query("from Model as model where model.createdBy.id = :user and model.modelType = :modelType and model.referenceId is null")
    List<Model> findModelsCreatedBy(@Param("user") Long createdBy, @Param("modelType") Integer modelType, Sort sort);
    
    @Query("from Model as model where model.createdBy.id = :user and "
            + "(lower(model.name) like :filter or lower(model.description) like :filter) and model.modelType = :modelType and model.referenceId is null")
    List<Model> findModelsCreatedBy(@Param("user") Long createdBy, @Param("modelType") Integer modelType, @Param("filter") String filter, Sort sort);
    
    @Query("from Model as model where model.referenceId = :referenceId")
    List<Model> findModelsByReferenceId(@Param("referenceId") Long referenceId);
    
    @Query("from Model as model where model.modelType = :modelType and model.referenceId = :referenceId")
    List<Model> findModelsByModelTypeAndReferenceId(@Param("modelType") Integer modelType, @Param("referenceId") Long referenceId);
    
    @Query("from Model as model where (lower(model.name) like :filter or lower(model.description) like :filter) " +
            "and model.modelType = :modelType and model.referenceId = :referenceId")
    List<Model> findModelsByModelTypeAndReferenceId(@Param("modelType") Integer modelType, 
            @Param("filter") String filter, @Param("referenceId") Long referenceId);
    
    @Query("from Model as model where model.modelType = :modelType and (model.referenceId = :referenceId or model.referenceId is null)")
    List<Model> findModelsByModelTypeAndReferenceIdOrNullReferenceId(@Param("modelType") Integer modelType, @Param("referenceId") Long referenceId);
	
	@Query("select pm from Model pm, ModelShareInfo info where pm = info.model and info.sharedBy.id =:user and "
	        + "pm.modelType = :modelType and pm.referenceId is null")
    List<Model> findModelsSharedBy(@Param("user") Long sharedBy, @Param("modelType") Integer modelType, Sort sort);
    
    @Query("select pm from Model pm, ModelShareInfo info where pm = info.model and info.sharedBy.id =:user and "
            + "(lower(info.model.name) like :filter or lower(info.model.description) like :filter) and pm.modelType = :modelType and pm.referenceId is null")
    List<Model> findModelsSharedBy(@Param("user") Long sharedBy, @Param("modelType") Integer modelType, @Param("filter") String filter,  Sort sort);
	
	@Query("select info from ModelShareInfo info join fetch info.model where info.user.id =:user and "
	        + "info.model.modelType = :modelType and info.model.referenceId is null")
    List<ModelShareInfo> findModelsSharedWithUser(@Param("user") Long sharedWith, @Param("modelType") Integer modelType, Sort sort);
	
	@Query("select info from ModelShareInfo info join fetch info.model where info.user.id =:user and "
	        + "(lower(info.model.name) like :filter or lower(info.model.description) like :filter) and info.model.modelType = :modelType and info.model.referenceId is null")
	List<ModelShareInfo> findModelsSharedWithUser(@Param("user") Long sharedWith, @Param("modelType") Integer modelType, @Param("filter") String filter,  Sort sort);

	@Query("select count(m.id) from Model m where m.createdBy = :user and m.modelType = :modelType")
    Long countByModelTypeAndUser(@Param("modelType") int modelType, @Param("user") User user);
}
