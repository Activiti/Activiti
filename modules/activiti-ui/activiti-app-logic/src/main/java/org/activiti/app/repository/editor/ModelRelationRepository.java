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

import org.activiti.app.domain.editor.ModelInformation;
import org.activiti.app.domain.editor.ModelRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author jbarrez
 */
public interface ModelRelationRepository extends JpaRepository<ModelRelation, Long> {
	
	@Query("from ModelRelation mr where mr.parentModelId = :parentModelId")
	List<ModelRelation> findByParentModelId(@Param("parentModelId") String parentModelId);
	
	@Query("from ModelRelation mr where mr.parentModelId = :parentModelId and mr.type = :type")
	List<ModelRelation> findByParentModelIdAndType(@Param("parentModelId") String parentModelId, @Param("type") String type);
	
	@Query("from ModelRelation mr where mr.modelId = :modelId")
	List<ModelRelation> findByChildModelId(@Param("modelId") String modelId);
	
	@Query("from ModelRelation mr where mr.modelId = :modelId and mr.type = :type")
	List<ModelRelation> findByChildModelIdAndType(@Param("modelId") String modelId, @Param("type") String type);
	
	@Query("select m.id, m.name, m.modelType from ModelRelation mr inner join mr.model m where mr.parentModelId = :parentModelId")
	List<ModelInformation> findModelInformationByParentModelId(@Param("parentModelId") String parentModelId);
	
	@Query("select m.id, m.name, m.modelType from ModelRelation mr inner join mr.parentModel m where mr.modelId = :modelId")
	List<ModelInformation> findModelInformationByChildModelId(@Param("modelId") String modelId);
	
	@Modifying
	@Query("delete from ModelRelation mr where mr.parentModelId = :parentModelId")
	void deleteModelRelationsForParentModel(@Param("parentModelId") String parentModelId);

}
