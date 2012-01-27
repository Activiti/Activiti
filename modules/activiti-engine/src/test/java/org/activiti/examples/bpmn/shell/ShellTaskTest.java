package org.activiti.examples.bpmn.shell;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.junit.Assume;
import org.junit.Test;

public class ShellTaskTest extends PluggableActivitiTestCase {

  enum OsType {
    LINUX, WINDOWS, MAC, SOLARIS, UNKOWN
  }

  OsType osType;

  OsType getSystemOsType() {
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.indexOf("win") >= 0)
      return OsType.WINDOWS;
    else if (osName.indexOf("mac") >= 0)
      return OsType.MAC;
    else if ((osName.indexOf("nix") >= 0) || (osName.indexOf("nux") >= 0))
      return OsType.LINUX;
    else if (osName.indexOf("sunos") >= 0)
      return OsType.SOLARIS;
    else
      return OsType.UNKOWN;
  }

  protected void setUp() throws Exception {
    osType = getSystemOsType();
  }

  @Test
  public void testOsDetection() throws Exception {
    assertTrue(osType != OsType.UNKOWN);
  }

  @Deployment
  public void testEchoShellWindows() {
    if (osType == OsType.WINDOWS) {

      ProcessInstance pi = runtimeService.startProcessInstanceByKey("echoShellWindows");
  
      String st = (String) runtimeService.getVariable(pi.getId(), "resultVar");
      assertNotNull(st);
      assertTrue(st.startsWith("EchoTest"));
    }
  }

  @Deployment
  public void testEchoShellLinux() {
    if (osType == OsType.LINUX) {

      ProcessInstance pi = runtimeService.startProcessInstanceByKey("echoShellLinux");
  
      String st = (String) runtimeService.getVariable(pi.getId(), "resultVar");
      assertNotNull(st);
      assertTrue(st.startsWith("EchoTest"));
    }
  }
  
  @Deployment
  public void testEchoShellMac() {
    if (osType == OsType.MAC) {

      ProcessInstance pi = runtimeService.startProcessInstanceByKey("echoShellMac");
  
      String st = (String) runtimeService.getVariable(pi.getId(), "resultVar");
      assertNotNull(st);
      assertTrue(st.startsWith("EchoTest"));
    }
  }
}
