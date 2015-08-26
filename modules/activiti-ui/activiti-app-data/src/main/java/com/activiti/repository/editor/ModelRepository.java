/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
