/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.core.el.juel.tree.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.activiti.core.el.juel.tree.Tree;
import org.activiti.core.el.juel.tree.TreeCache;

/**
 * Concurrent (thread-safe) FIFO tree cache (using classes from
 * <code>java.util.concurrent</code>). After the cache size reached a certain
 * limit, some least recently used entry are removed, when adding a new entry.
 *
 * @author Christoph Beck
 */
public final class Cache implements TreeCache {

    private final ConcurrentMap<String, Tree> map;
    private final ConcurrentLinkedQueue<String> queue;
    private final AtomicInteger size;
    private final int capacity;

    /**
     * Creates a new cache with the specified capacity
     * and default concurrency level (16).
     *
     * @param capacity
     *            Cache size. The actual size may exceed it temporarily.
     */
    public Cache(int capacity) {
        this(capacity, 16);
    }

    /**
     * Creates a new cache with the specified capacity and concurrency level.
     *
     * @param capacity
     *            Cache size. The actual map size may exceed it temporarily.
     * @param concurrencyLevel
     *            The estimated number of concurrently updating threads. The
     *            implementation performs internal sizing to try to accommodate
     *            this many threads.
     */
    public Cache(int capacity, int concurrencyLevel) {
        this.map =
            new ConcurrentHashMap<String, Tree>(16, 0.75f, concurrencyLevel);
        this.queue = new ConcurrentLinkedQueue<String>();
        this.size = new AtomicInteger();
        this.capacity = capacity;
    }

    public int size() {
        return size.get();
    }

    public Tree get(String expression) {
        return map.get(expression);
    }

    public void put(String expression, Tree tree) {
        if (map.putIfAbsent(expression, tree) == null) {
            queue.offer(expression);
            if (size.incrementAndGet() > capacity) {
                size.decrementAndGet();
                map.remove(queue.poll());
            }
        }
    }
}
