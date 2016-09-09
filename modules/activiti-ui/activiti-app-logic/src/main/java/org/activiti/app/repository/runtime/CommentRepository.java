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
package org.activiti.app.repository.runtime;

import java.util.List;

import org.activiti.app.domain.runtime.Comment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>{

	List<Comment> findByTaskId(String taskId, Sort sort);
	
	List<Comment> findByProcessInstanceId(String processInstanceId, Sort sort);
	
	long countByTaskId(String taskId);
	
	long countByProcessInstanceId(String processInstanceId);

	@Modifying
    @Query(value="delete from Comment c where c.processInstanceId = :processInstanceId")
    void deleteAllByProcessInstanceId(@Param("processInstanceId") String processInstanceId);
}
