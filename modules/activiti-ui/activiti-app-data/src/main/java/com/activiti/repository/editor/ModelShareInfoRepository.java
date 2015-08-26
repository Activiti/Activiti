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

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.activiti.domain.editor.ModelShareInfo;
import com.activiti.domain.idm.User;

public interface ModelShareInfoRepository extends JpaRepository<ModelShareInfo, Long> {

    List<ModelShareInfo> findByUserIdOrderByShareDateAsc(Long userId, Pageable paging);
    
    List<ModelShareInfo> findByModelIdOrderByShareDateAsc(Long modelId, Pageable paging);
    
    @Query(value="select info from ModelShareInfo info left outer join info.group as group where info.model.id =:modelId and (info.user.id =:userId or group.id in (:groupIds))")
    List<ModelShareInfo> findByModelIdWithUserIdOrGroups(@Param("modelId") Long modelId, @Param("userId") Long userId, @Param("groupIds") List<Long> groupIds);
    
    @Query(value="select info from ModelShareInfo info where info.model.id =:modelId and info.user.id =:userId")
    List<ModelShareInfo> findByModelIdWithUserId(@Param("modelId") Long modelId, @Param("userId") Long userId);
    
    ModelShareInfo findByModelIdAndUserId(Long modelId, Long userId);
    
    ModelShareInfo findByModelIdAndGroupId(Long modelId, Long groupId);
    
    ModelShareInfo findByModelIdAndId(Long modelId, Long id);
    
    ModelShareInfo findByModelIdAndId(Long modelId, String id);
    
    @Modifying
    @Query(value="delete from ModelShareInfo info where info.model.id = ?")
    void deleteInBatchByModelId(Long modelId);
    
    /**
     * Connects all models that are shared by email-address matching the address of the given user.
     */
    @Modifying
    @Query(value="update ModelShareInfo info set info.email = null, info.user = :user where info.email = :email")
    void connectSharedModelsByEmail(@Param("email") String email, @Param("user") User user);
    
}
