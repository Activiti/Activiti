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
package org.activiti.engine.impl.persistence.deploy;

import static java.util.Collections.synchronizedMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Default cache: keep everything in memory, unless a limit is set.
 *

 */
public class ProcessDefinitionInfoCache {

  private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionInfoCache.class);

  protected Map<String, ProcessDefinitionInfoCacheObject> cache;
  protected CommandExecutor commandExecutor;

  /** Cache with no limit */
  public ProcessDefinitionInfoCache(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
    this.cache = synchronizedMap(new HashMap<String, ProcessDefinitionInfoCacheObject>());
  }

  /** Cache which has a hard limit: no more elements will be cached than the limit. */
  public ProcessDefinitionInfoCache(CommandExecutor commandExecutor, final int limit) {
    this.commandExecutor = commandExecutor;
    this.cache = synchronizedMap(new LinkedHashMap<String, ProcessDefinitionInfoCacheObject>(limit + 1, 0.75f, true) {
          // +1 is needed, because the entry is inserted first, before it is removed
          // 0.75 is the default (see javadocs)
          // true will keep the 'access-order', which is needed to have a real LRU cache
      private static final long serialVersionUID = 1L;

      protected boolean removeEldestEntry(Map.Entry<String, ProcessDefinitionInfoCacheObject> eldest) {
        boolean removeEldest = size() > limit;
        if (removeEldest) {
          logger.trace("Cache limit is reached, {} will be evicted",  eldest.getKey());
        }
        return removeEldest;
      }

    });
  }

  public ProcessDefinitionInfoCacheObject get(final String processDefinitionId) {
    ProcessDefinitionInfoCacheObject infoCacheObject = null;
    Command<ProcessDefinitionInfoCacheObject> cacheCommand = new Command<ProcessDefinitionInfoCacheObject>() {

      @Override
      public ProcessDefinitionInfoCacheObject execute(CommandContext commandContext) {
        return retrieveProcessDefinitionInfoCacheObject(processDefinitionId, commandContext);
      }
    };

    if (Context.getCommandContext() != null) {
      infoCacheObject = retrieveProcessDefinitionInfoCacheObject(processDefinitionId, Context.getCommandContext());
    } else {
      infoCacheObject = commandExecutor.execute(cacheCommand);
    }

    return infoCacheObject;
  }

  public void add(String id, ProcessDefinitionInfoCacheObject obj) {
    cache.put(id, obj);
  }

  public void remove(String id) {
    cache.remove(id);
  }

  public void clear() {
    cache.clear();
  }

  // For testing purposes only
  public int size() {
    return cache.size();
  }

  protected ProcessDefinitionInfoCacheObject retrieveProcessDefinitionInfoCacheObject(String processDefinitionId, CommandContext commandContext) {
    ProcessDefinitionInfoEntityManager infoEntityManager = commandContext.getProcessDefinitionInfoEntityManager();
    ObjectMapper objectMapper = commandContext.getProcessEngineConfiguration().getObjectMapper();

    ProcessDefinitionInfoCacheObject cacheObject = null;
    if (cache.containsKey(processDefinitionId)) {
      cacheObject = cache.get(processDefinitionId);
    } else {
      cacheObject = new ProcessDefinitionInfoCacheObject();
      cacheObject.setRevision(0);
      cacheObject.setInfoNode(objectMapper.createObjectNode());
    }

    ProcessDefinitionInfoEntity infoEntity = infoEntityManager.findProcessDefinitionInfoByProcessDefinitionId(processDefinitionId);
    if (infoEntity != null && infoEntity.getRevision() != cacheObject.getRevision()) {
      cacheObject.setRevision(infoEntity.getRevision());
      if (infoEntity.getInfoJsonId() != null) {
        byte[] infoBytes = infoEntityManager.findInfoJsonById(infoEntity.getInfoJsonId());
        try {
          ObjectNode infoNode = (ObjectNode) objectMapper.readTree(infoBytes);
          cacheObject.setInfoNode(infoNode);
        } catch (Exception e) {
          throw new ActivitiException("Error reading json info node for process definition " + processDefinitionId, e);
        }
      }
    } else if (infoEntity == null) {
      cacheObject.setRevision(0);
      cacheObject.setInfoNode(objectMapper.createObjectNode());
    }

    return cacheObject;
  }

}
