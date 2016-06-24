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