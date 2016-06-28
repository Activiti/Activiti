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
