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
package org.activiti.scripting.secure.impl;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 */
public class SecureScriptContextFactory extends ContextFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecureScriptContextFactory.class);

    protected SecureScriptClassShutter classShutter;
    protected int observeInstructionCount = 10;
    protected long maxScriptExecutionTime = -1L;
    protected long maxMemoryUsed = -1L;
    protected int maxStackDepth = -1;
    protected int optimizationLevel = -1;
    protected SecureScriptThreadMxBeanWrapper threadMxBeanWrapper;

    protected Context makeContext() {
        SecureScriptContext context = new SecureScriptContext();

        // Setting this, as otherwise variables set seem to have the wrong type
        context.getWrapFactory().setJavaPrimitiveWrap(false);

        context.setOptimizationLevel(optimizationLevel);

        // Class white-listing
        if (classShutter != null) {
            context.setClassShutter(classShutter);
        }

        // Needed for both time and memory measurement
        if (maxScriptExecutionTime > 0L || maxMemoryUsed > 0L) {
            context.setGenerateObserverCount(true);
            context.setInstructionObserverThreshold(observeInstructionCount);
        }

        // Memory limit
        if (maxMemoryUsed > 0) {
            context.setThreadId(Thread.currentThread().getId());
        }

        // Max stack depth
        if (maxStackDepth > 0) {
            context.setOptimizationLevel(-1); // stack depth can only be set when no optimizations are applied
            context.setMaximumInterpreterStackDepth(maxStackDepth);
        }

        return context;
    }

    protected void observeInstructionCount(Context cx, int instructionCount) {
        SecureScriptContext context = (SecureScriptContext) cx;

        // Time limit
        if (maxScriptExecutionTime > 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - context.getStartTime() > maxScriptExecutionTime) {
                throw new Error("Maximum variableScope time of " + maxScriptExecutionTime + " ms exceeded");
            }
        }

        // Memory
        if (maxMemoryUsed > 0 && threadMxBeanWrapper != null) {

            if (context.getStartMemory() <= 0) {
                context.setStartMemory(threadMxBeanWrapper.getThreadAllocatedBytes(context.getThreadId()));
            } else {
                long currentAllocatedBytes = threadMxBeanWrapper.getThreadAllocatedBytes(context.getThreadId());
                if (currentAllocatedBytes - context.getStartMemory() >= maxMemoryUsed) {
                    throw new Error("Memory limit of " + maxMemoryUsed + " bytes reached");
                }
            }

        }
    }

    // Override {@link #doTopCall(Callable, Context, Scriptable, Scriptable, Object[])}
    protected Object doTopCall(Callable callable, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        SecureScriptContext mcx = (SecureScriptContext) cx;
        mcx.setStartTime(System.currentTimeMillis());
        return super.doTopCall(callable, cx, scope, thisObj, args);
    }

    public int getOptimizationLevel() {
        return optimizationLevel;
    }

    public void setOptimizationLevel(int optimizationLevel) {
        this.optimizationLevel = optimizationLevel;
    }

    public SecureScriptClassShutter getClassShutter() {
        return classShutter;
    }

    public void setClassShutter(SecureScriptClassShutter classShutter) {
        this.classShutter = classShutter;
    }

    public int getObserveInstructionCount() {
        return observeInstructionCount;
    }

    public void setObserveInstructionCount(int observeInstructionCount) {
        this.observeInstructionCount = observeInstructionCount;
    }

    public long getMaxScriptExecutionTime() {
        return maxScriptExecutionTime;
    }

    public void setMaxScriptExecutionTime(long maxScriptExecutionTime) {
        this.maxScriptExecutionTime = maxScriptExecutionTime;
    }

    public long getMaxMemoryUsed() {
        return maxMemoryUsed;
    }

    public void setMaxMemoryUsed(long maxMemoryUsed) {
        this.maxMemoryUsed = maxMemoryUsed;
        if (maxMemoryUsed > 0) {
            try {
                Class clazz = Class.forName("com.sun.management.ThreadMXBean");
                if (clazz != null) {
                    this.threadMxBeanWrapper = new SecureScriptThreadMxBeanWrapper();
                }
            } catch (ClassNotFoundException cnfe) {
                LOGGER.warn("com.sun.management.ThreadMXBean was not found on the classpath. " +
                        "This means that the limiting the memory usage for a script will NOT work.");
            }
        }
    }

    public int getMaxStackDepth() {
        return maxStackDepth;
    }

    public void setMaxStackDepth(int maxStackDepth) {
        this.maxStackDepth = maxStackDepth;
    }
}