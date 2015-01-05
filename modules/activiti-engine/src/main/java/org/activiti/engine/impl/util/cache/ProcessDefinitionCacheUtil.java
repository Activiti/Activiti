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
package org.activiti.engine.impl.util.cache;

import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

/**
 * @author Joram Barrez
 */
public class ProcessDefinitionCacheUtil {
	
	public static ProcessDefinitionEntity getCachedProcessDefinitionEntity(String processDefinitionId) {
		ProcessDefinitionCacheEntry cacheEntry = getCacheEntry(processDefinitionId);
		if (cacheEntry != null) {
			return cacheEntry.getProcessDefinitionEntity();
		} 
		return null;
	}

	public static Process getCachedProcess(String processDefinitionId) {
		ProcessDefinitionCacheEntry cacheEntry = getCacheEntry(processDefinitionId);
		if (cacheEntry != null) {
			return cacheEntry.getProcess();
		} 
		return null;
	}
	
	public static ProcessDefinitionCacheEntry getCacheEntry(String processDefinitionId) {
	    ProcessDefinitionCacheEntry cacheEntry = Context.getProcessEngineConfiguration()
	    		.getProcessDefinitionCache().get(processDefinitionId);
	    return cacheEntry;
    }

}
