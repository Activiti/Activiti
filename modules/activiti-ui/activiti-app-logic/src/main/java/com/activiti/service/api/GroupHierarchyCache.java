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
package com.activiti.service.api;

import com.activiti.domain.idm.Group;

/**
 * A cache of {@link Group} objects.
 * 
 * @author Joram Barrez
 */
public interface GroupHierarchyCache {

	/**
	 * Fetches one {@link Group} from the cache.
	 */
	Group getGroup(Long groupId);
	
	/**
	 * Invalidates a cached entry. 
	 */
	void invalidate(Long groupId);

	long getCacheHitCount();

	long getCacheLoadCount();
	
	long getCacheMissCount();
	
	void reset();
	
}
