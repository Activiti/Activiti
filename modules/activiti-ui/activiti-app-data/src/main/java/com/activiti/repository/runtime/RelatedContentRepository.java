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
package com.activiti.repository.runtime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.RelatedContent;

/**
 * @author Frederik Heremans
 * @author Erik Winlof
 */
public interface RelatedContentRepository extends JpaRepository<RelatedContent, Long> {

    Page<RelatedContent> findAllRelatedBySourceAndSourceId(@Param("source")String source, @Param("sourceId")String sourceId, Pageable paging);

    @Query("from RelatedContent r where r.taskId = :taskId and r.relatedContent = true")
    Page<RelatedContent> findAllRelatedByTaskId(@Param("taskId") String taskId, Pageable paging);
    
    @Query("from RelatedContent r where r.taskId = :taskId and r.relatedContent = false")
    Page<RelatedContent> findAllFieldBasedContentByTaskId(@Param("taskId") String taskId, Pageable paging);
    
    Page<RelatedContent> findAllByTaskIdAndField(@Param("taskId") String taskId, @Param("field") String field, Pageable paging);
    
    @Query("from RelatedContent r where r.processInstanceId = :processInstanceId and r.relatedContent = true")
    Page<RelatedContent> findAllRelatedByProcessInstanceId(@Param("processInstanceId")String processId, Pageable paging);
    
    @Query("from RelatedContent r where r.processInstanceId = :processInstanceId and r.relatedContent = false")
    Page<RelatedContent> findAllFieldBasedContentByProcessInstanceId(@Param("processInstanceId")String processId, Pageable paging);
    
    @Query("from RelatedContent r where r.processInstanceId = :processInstanceId")
    Page<RelatedContent> findAllContentByProcessInstanceId(@Param("processInstanceId")String processId, Pageable paging);

    @Query("from RelatedContent r where r.processInstanceId = :processInstanceId and r.field = :field")
    Page<RelatedContent> findAllByProcessInstanceIdAndField(@Param("processInstanceId")String processId, @Param("field") String field, Pageable paging);

    @Modifying
    @Query("delete from RelatedContent r where r.processInstanceId = :processInstanceId")
    void deleteAllContentByProcessInstanceId(@Param("processInstanceId") String processInstanceId);
    
    @Query("select sum(r.contentSize) from RelatedContent r where r.createdBy = :user")
    Long getTotalContentSizeForUser(@Param("user") User user);
}
