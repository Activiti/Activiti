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
package com.activiti.repository.idm;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.activiti.domain.idm.Group;

/**
 * @author Joram Barrez
 */
public interface GroupRepository extends JpaRepository<Group, Long> {

	// Used for cache entry validation
	@Query("select count(g.id) from Group g where g.id = :groupId and g.lastUpdate = :lastUpdate")
	Long getGroupCountByGroupIdAndLastUpdateDate(@Param("groupId") Long groupId, @Param("lastUpdate") Date lastUpdate);
	
}
