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
