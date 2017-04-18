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
package org.activiti.test.serviceTask.unsecure;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.test.serviceTask.ServiceTaskBaseTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vasile Dirla
 */
public class UnsecureShellTaskTest extends ServiceTaskBaseTest {


    @Test
    public void testClassWhiteListingAllowed1() {
        if (osType == OsType.WINDOWS) {
            deployProcessDefinition("ShellTaskTest.testEchoShellWindows.bpmn20.xml");
        } else if (osType == OsType.LINUX) {
            deployProcessDefinition("ShellTaskTest.testEchoShellLinux.bpmn20.xml");
        } else if (osType == OsType.MAC) {
            deployProcessDefinition("ShellTaskTest.testEchoShellMac.bpmn20.xml");
        } else {
            fail("Unknown OS Type " + osType);
        }


        ProcessInstance pi = runtimeService.startProcessInstanceByKey("shellCommandEcho");
        String st = (String) runtimeService.getVariable(pi.getId(), "resultVar");
        assertNotNull(st);
        assertTrue(st.startsWith("EchoTest"));
    }

    @Test
    public void testClassWhiteListingAllowed2() {
        if (osType == OsType.WINDOWS) {
            deployProcessDefinition("ShellTaskTest.testLsShellWindows.bpmn20.xml");
        } else if (osType == OsType.LINUX) {
            deployProcessDefinition("ShellTaskTest.testLsShellLinux.bpmn20.xml");
        } else if (osType == OsType.MAC) {
            deployProcessDefinition("ShellTaskTest.testLsShellMac.bpmn20.xml");
        } else {
            fail("Unknown OS Type " + osType);
        }

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("shellCommand");
        String st = (String) runtimeService.getVariable(pi.getId(), "resultVar");
        assertNotNull(st);
        assertTrue(st.contains("pom.xml"));
    }

}
