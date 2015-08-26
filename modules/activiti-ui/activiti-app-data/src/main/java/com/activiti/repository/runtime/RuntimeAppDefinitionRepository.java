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

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.RuntimeAppDefinition;

public interface RuntimeAppDefinitionRepository extends JpaRepository<RuntimeAppDefinition, Long>{

    RuntimeAppDefinition findByModelId(Long modelId);
    
    List<RuntimeAppDefinition> findByTenantId(Long tenantId);
    
    List<RuntimeAppDefinition> findByNameIgnoreCase(String name, Pageable pageable);
    
    @Query("select app.appDefinition from RuntimeApp app where app.user = :user")
    List<RuntimeAppDefinition> findByUser(@Param("user") User user);
    
    @Query("from RuntimeAppDefinition def where def.modelId = :modelId and def.createdBy = :user")
    RuntimeAppDefinition findByModelAndUser(@Param("modelId") Long modelId, @Param("user") User user);
    
}