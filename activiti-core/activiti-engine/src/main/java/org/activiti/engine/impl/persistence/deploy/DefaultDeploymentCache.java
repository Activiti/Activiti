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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default cache: keep everything in memory, unless a limit is set.
 *

 */
public class DefaultDeploymentCache<T> implements DeploymentCache<T> {

  private static final Logger logger = LoggerFactory.getLogger(DefaultDeploymentCache.class);

  protected Map<String, T> cache;

  /** Cache with no limit */
  public DefaultDeploymentCache() {
    this.cache = synchronizedMap(new HashMap<String, T>());
  }

  /**
   * Cache which has a hard limit: no more elements will be cached than the limit.
   */
  public DefaultDeploymentCache(final int limit) {
    this.cache = synchronizedMap(new LinkedHashMap<String, T>(limit + 1, 0.75f, true) { // +1 is needed, because the entry is inserted first, before it is removed
          // 0.75 is the default (see javadocs)
          // true will keep the 'access-order', which is needed to have a real LRU cache
          private static final long serialVersionUID = 1L;

          protected boolean removeEldestEntry(Map.Entry<String, T> eldest) {
            boolean removeEldest = size() > limit;
            if (removeEldest && logger.isTraceEnabled()) {
              logger.trace("Cache limit is reached, {} will be evicted", eldest.getKey());
            }
            return removeEldest;
          }

        });
  }

  public T get(String id) {
    return cache.get(id);
  }

  public void add(String id, T obj) {
    cache.put(id, obj);
  }

  public void remove(String id) {
    cache.remove(id);
  }

  @Override
  public boolean contains(String id) {
    return cache.containsKey(id);
  }

  public void clear() {
    cache.clear();
  }

  // For testing purposes only
  public int size() {
    return cache.size();
  }

}
