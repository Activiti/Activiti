package org.activiti.engine.impl.asyncexecutor;

import org.apache.commons.lang.StringUtils;
import org.activiti.engine.impl.persistence.entity.JobEntityImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ProcessMemoryJobManager {
    private static final ConcurrentHashMap<String, ConcurrentLinkedQueue<JobEntityImpl>> MEMORY_JOB_QUEUE_MAP
        = new ConcurrentHashMap<>(64);

    private static final ConcurrentHashMap<String, ReentrantReadWriteLock> MEMORY_JOB_LOCK_MAP
        = new ConcurrentHashMap<>(64);

    public static void registerLockedProcessInstance(String processInstanceId) {
        MEMORY_JOB_QUEUE_MAP.putIfAbsent(processInstanceId, new ConcurrentLinkedQueue<>());
        MEMORY_JOB_LOCK_MAP.putIfAbsent(processInstanceId, new ReentrantReadWriteLock());
    }

    public static boolean tryAppendToMemoryJobManager(JobEntityImpl job) {
        ReentrantReadWriteLock lock = MEMORY_JOB_LOCK_MAP.get(job.getProcessInstanceId());
        if (lock != null) {
            ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
            try {
                readLock.lock();
                ConcurrentLinkedQueue<JobEntityImpl> concurrentLinkedQueue = MEMORY_JOB_QUEUE_MAP.get(
                    job.getProcessInstanceId());
                if (concurrentLinkedQueue != null) {
                    return concurrentLinkedQueue.offer(job);
                }
            } finally {
                readLock.unlock();
            }
        }
        return false;
    }

    public static List<JobEntityImpl> findMemoryJobList(String processInstanceId) {
        ConcurrentLinkedQueue<JobEntityImpl> queue = MEMORY_JOB_QUEUE_MAP.get(processInstanceId);
        if (queue == null || queue.isEmpty()) {
            return new ArrayList<>();
        }
        List<JobEntityImpl> list = new ArrayList<>();
        JobEntityImpl job = queue.poll();
        while (job != null) {
            list.add(job);
            job = queue.poll();
        }
        return list;
    }

    public static boolean unRegisterLockedProcessInstance(String processInstanceId) {
        if (StringUtils.isEmpty(processInstanceId)) {
            return true;
        }
        ReentrantReadWriteLock lock = MEMORY_JOB_LOCK_MAP.get(processInstanceId);
        if (lock == null) {
            return true;
        }
        try {
            lock.writeLock().lock();
            ConcurrentLinkedQueue<JobEntityImpl> concurrentLinkedQueue = MEMORY_JOB_QUEUE_MAP.get(processInstanceId);
            if (concurrentLinkedQueue != null && !concurrentLinkedQueue.isEmpty()) {
                return false;
            }
            MEMORY_JOB_QUEUE_MAP.remove(processInstanceId);
            MEMORY_JOB_LOCK_MAP.remove(processInstanceId);
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

}
