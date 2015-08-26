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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.domain.runtime.RuntimeAppDeployment;

public interface RuntimeAppDeploymentRepository extends JpaRepository<RuntimeAppDeployment, Long>{

    RuntimeAppDeployment findByDeploymentId(String deploymentId);
    
    List<RuntimeAppDeployment> findByAppDefinition(RuntimeAppDefinition definition);
    
    @Query(value="select app from RuntimeAppDeployment app where app.appDefinition.id = ?")
    List<RuntimeAppDeployment> findByAppDefinitionId(Long appId);
    
    @Modifying
    @Query(value="delete from RuntimeAppDeployment app where app.appDefinition.id = ?")
    void deleteInBatchByAppId(Long appId);
}