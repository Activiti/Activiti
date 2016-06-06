package org.activiti.impl.scripting.secure.rhino;

import java.lang.management.ManagementFactory;

/**
 * This class wraps access to the com.sun.management.ThreadMXBean and makes sure that
 * on JDK's without the class no ClassNotFoundException (or others) are thrown.
 *
 * This class should only be used if it is verified this class is on the classpath,
 * for example by checking the class through reflection.
 *
 * @author Joram Barrez
 */
public class SecureScriptThreadMxBeanWrapper {

    protected com.sun.management.ThreadMXBean threadMXBean; // Only Oracle / OpenJDK (only checked those)

    public SecureScriptThreadMxBeanWrapper() {
        this.threadMXBean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    }

    public long getThreadAllocatedBytes(long threadId) {
        return threadMXBean.getThreadAllocatedBytes(threadId);
    }

}
