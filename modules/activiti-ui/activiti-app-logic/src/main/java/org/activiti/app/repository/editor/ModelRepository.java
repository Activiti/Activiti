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
package org.activiti.app.repository.editor;

import java.util.List;

import org.activiti.app.domain.editor.Model;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the Model entity.
 */
public interface ModelRepository extends JpaRepository<Model, String> {

  @Query("from Model as model where model.createdBy = :user and model.modelType = :modelType")
  List<Model> findModelsCreatedBy(@Param("user") String createdBy, @Param("modelType") Integer modelType, Sort sort);

  @Query("from Model as model where model.createdBy = :user and "
      + "(lower(model.name) like :filter or lower(model.description) like :filter) and model.modelType = :modelType")
  List<Model> findModelsCreatedBy(@Param("user") String createdBy, @Param("modelType") Integer modelType, @Param("filter") String filter, Sort sort);

  @Query("from Model as model where model.key = :key and model.modelType = :modelType")
  List<Model> findModelsByKeyAndType(@Param("key") String key, @Param("modelType") Integer modelType);
  
  @Query("from Model as model where (lower(model.name) like :filter or lower(model.description) like :filter) " + "and model.modelType = :modelType")
  List<Model> findModelsByModelType(@Param("modelType") Integer modelType, @Param("filter") String filter);

  @Query("from Model as model where model.modelType = :modelType")
  List<Model> findModelsByModelType(@Param("modelType") Integer modelType);

  @Query("select count(m.id) from Model m where m.createdBy = :user and m.modelType = :modelType")
  Long countByModelTypeAndUser(@Param("modelType") int modelType, @Param("user") String user);
  
  @Query("select m from ModelRelation mr inner join mr.model m where mr.parentModelId = :parentModelId")
  List<Model> findModelsByParentModelId(@Param("parentModelId") String parentModelId);
  
  @Query("select m from ModelRelation mr inner join mr.model m where mr.parentModelId = :parentModelId and m.modelType = :modelType")
  List<Model> findModelsByParentModelIdAndType(@Param("parentModelId") String parentModelId, @Param("modelType") Integer modelType);
  
  @Query("select m.id, m.name, m.modelType from ModelRelation mr inner join mr.parentModel m where mr.modelId = :modelId")
  List<Model> findModelsByChildModelId(@Param("modelId") String modelId);
  
  @Query("select model.key from Model as model where model.id = :modelId and model.createdBy = :user")
  String appDefinitionIdByModelAndUser(@Param("modelId") String modelId, @Param("user") String user);


}
