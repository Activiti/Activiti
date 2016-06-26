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
package com.activiti.service.idm;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.activiti.domain.idm.Group;
import com.activiti.repository.idm.GroupRepository;
import com.activiti.service.api.GroupHierarchyCache;
import com.activiti.service.api.GroupService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;

/**
 * Cache containing {@link Group} objects to prevent too much DB-traffic,
 * especially when tasks are assigned to 'parent' groups and each of the users
 * in the 'child' groups need to see this. 
 * This would mean a lot of DB traffic if this needs to be done on each query.
 * 
 * !! NOTE that this cache DOES NOT store user/capability information about a group !!
 * 
 * @author Joram Barrez
 */
@Service
public class GroupHierarchyCacheImpl implements GroupHierarchyCache {
    
    private final Logger logger = LoggerFactory.getLogger(GroupHierarchyCacheImpl.class);
    
    @Inject
    private Environment environment;
    
    @Inject
    private GroupService groupService;
    
    @Inject
    private GroupRepository groupRepository;
    
    private LoadingCache<Long, Group> groupCache;
    
    @PostConstruct
	protected void initCache() {
		Long groupCacheMaxSize = environment.getProperty("cache.groups.max.size", Long.class);
		Long groupCacheMaxAge = environment.getProperty("cache.groups.max.age", Long.class);
		groupCache = CacheBuilder.newBuilder()
			.maximumSize(groupCacheMaxSize != null ? groupCacheMaxSize : 2048)
			.expireAfterAccess(groupCacheMaxAge != null ? groupCacheMaxAge : (24 * 60 * 60) , TimeUnit.SECONDS)
			.recordStats()
			.build(new CacheLoader<Long, Group>() {
				
				 public Group load(final Long groupId) throws Exception {
					 
					 // The two booleans at the end make sure all parents are loaded (to the root)
					 // and all children are loaded (to the leafs)
					 
					 // Note: we're NOT fetching children currently, cause there is currently no need for it yet
					 // (all task group query work the other way around)
					 
					 // Using GroupService so we get all the data in the same transaction
					 
					 return groupService.getGroup(groupId, false);
			     }

				
			});
	}

    @Override
    public Group getGroup(Long groupId) {
    	try {
    		Group group = groupCache.get(groupId);
    		
    		// Check validity
    		if (groupRepository.getGroupCountByGroupIdAndLastUpdateDate(groupId, group.getLastUpdate()) == 0) {
    			// Invalidating the group will cause a refetch from db on next get from cache
    			groupCache.invalidate(groupId);
    			group = groupCache.get(groupId);
    		}
    		
    		return group;
    	} catch (ExecutionException e) {
    		return null;
    	} catch (InvalidCacheLoadException e) {
    		return null;
    	}
    }
    
    @Override
    public void invalidate(Long groupId) {
    	groupCache.invalidate(groupId);
    }
    
	public long getCacheHitCount() {
		return groupCache.stats().hitCount();
	}
	
	public long getCacheLoadCount() {
		return groupCache.stats().loadCount();
	}
	
	public long getCacheMissCount() {
		return groupCache.stats().missCount();
	}
	
	public void reset() {
		groupCache.invalidateAll();
	}
}
