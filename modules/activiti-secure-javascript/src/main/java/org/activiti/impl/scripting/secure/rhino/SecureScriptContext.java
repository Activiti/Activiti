package org.activiti.impl.scripting.secure.rhino;

import org.mozilla.javascript.Context;

/**
 * @author Joram Barrez
 */
public class SecureScriptContext extends Context {

    private long startTime;
    private long threadId;
    private long startMemory;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public long getStartMemory() {
        return startMemory;
    }

    public void setStartMemory(long startMemory) {
        this.startMemory = startMemory;
    }
}