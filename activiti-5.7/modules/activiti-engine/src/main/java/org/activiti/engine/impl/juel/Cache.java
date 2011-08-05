/*
 * Based on JUEL 2.2.1 code, 2006-2009 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.activiti.engine.impl.juel;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;



/**
 * Simple (thread-safe) LRU cache.
 * After the cache size reached a certain limit, the least recently used entry is removed,
 * when adding a new entry.
 *
 * @author Christoph Beck
 */
public final class Cache implements TreeCache {
  private final Map<String,Tree> primary;
  private final Map<String,Tree> secondary;

  /**
   * Constructor.
   * Use a {@link WeakHashMap} as secondary map.
   * @param size maximum primary cache size
   */
	public Cache(int size) {
		this(size, new WeakHashMap<String,Tree>());
	}

	/**
	 * Constructor.
	 * If the least recently used entry is removed from the primary cache, it is added to
	 * the secondary map.
   * @param size maximum primary cache size
	 * @param secondary the secondary map (may be <code>null</code>)
	 */
	@SuppressWarnings("serial")
	public Cache(final int size, Map<String,Tree> secondary) {
		this.primary = Collections.synchronizedMap(new LinkedHashMap<String,Tree>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Entry<String,Tree> eldest) {
				if (size() > size) {
					if (Cache.this.secondary != null) { // move to secondary cache
						Cache.this.secondary.put(eldest.getKey(), eldest.getValue());
					}
					return true;
				}
				return false;
			}
		});
		this.secondary = secondary == null ? null : Collections.synchronizedMap(secondary);
	}

	public Tree get(String expression) {
		if (secondary == null) {
			return primary.get(expression);
		} else {
			Tree tree = primary.get(expression);
			if (tree == null) {
				tree = secondary.get(expression);
			}
			return tree;
		}
	}

	public void put(String expression, Tree tree) {
		primary.put(expression, tree);
	}
}
