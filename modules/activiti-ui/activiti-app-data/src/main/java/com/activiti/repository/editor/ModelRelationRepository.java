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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.activiti.domain.editor.ModelRelation;

/**
 * @author jbarrez
 */
public interface ModelRelationRepository extends JpaRepository<ModelRelation, Long> {
	
	@Query("from ModelRelation mr where mr.parentModelId = :parentModelId")
	List<ModelRelation> findByParentModelId(@Param("parentModelId") Long parentModelId);
	
	@Query("from ModelRelation mr where mr.parentModelId = :parentModelId and mr.type = :type")
	List<ModelRelation> findByParentModelIdAndType(@Param("parentModelId") Long parentModelId, @Param("type") String type);
	
	@Query("from ModelRelation mr where mr.modelId = :modelId")
	List<ModelRelation> findByChildModelId(@Param("modelId") Long modelId);
	
	@Query("from ModelRelation mr where mr.modelId = :modelId and mr.type = :type")
	List<ModelRelation> findByChildModelIdAndType(@Param("modelId") Long modelId, @Param("type") String type);
	
	@Query("select m.id, m.name, m.modelType from ModelRelation mr inner join mr.model m where mr.parentModelId = :parentModelId")
	List<Object[]> findModelInformationByParentModelId(@Param("parentModelId") Long parentModelId);
	
	@Query("select m.id, m.name, m.modelType from ModelRelation mr inner join mr.parentModel m where mr.modelId = :modelId")
	List<Object[]> findModelInformationByChildModelId(@Param("modelId") Long modelId);
	
	@Modifying
	@Query("delete from ModelRelation mr where mr.parentModelId = :parentModelId")
	void deleteModelRelationsForParentModel(@Param("parentModelId") Long parentModelId);

}
