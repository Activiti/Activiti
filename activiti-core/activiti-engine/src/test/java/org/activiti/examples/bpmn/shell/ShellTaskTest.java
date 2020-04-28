package org.activiti.examples.bpmn.shell;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

public class ShellTaskTest extends PluggableActivitiTestCase {

  enum OsType {
    LINUX, WINDOWS, MAC, SOLARIS, UNKOWN
  }

  OsType osType;

  OsType getSystemOsType() {
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.contains("win"))
      return OsType.WINDOWS;
    else if (osName.contains("mac"))
      return OsType.MAC;
    else if ((osName.contains("nix")) || (osName.contains("nux")))
      return OsType.LINUX;
    else if (osName.contains("sunos"))
      return OsType.SOLARIS;
    else
      return OsType.UNKOWN;
  }

  protected void setUp() throws Exception {
    osType = getSystemOsType();
  }

  public void testOsDetection() throws Exception {
    assertThat(osType != OsType.UNKOWN).isTrue();
  }

  @Deployment
  public void testEchoShellWindows() {
    if (osType == OsType.WINDOWS) {

      ProcessInstance pi = runtimeService.startProcessInstanceByKey("echoShellWindows");

      String st = (String) runtimeService.getVariable(pi.getId(), "resultVar");
      assertThat(st).isNotNull();
      assertThat(st.startsWith("EchoTest")).isTrue();
    }
  }

  @Deployment
  public void testEchoShellLinux() {
    if (osType == OsType.LINUX) {

      ProcessInstance pi = runtimeService.startProcessInstanceByKey("echoShellLinux");

      String st = (String) runtimeService.getVariable(pi.getId(), "resultVar");
      assertThat(st).isNotNull();
      assertThat(st.startsWith("EchoTest")).isTrue();
    }
  }

  @Deployment
  public void testEchoShellMac() {
    if (osType == OsType.MAC) {

      ProcessInstance pi = runtimeService.startProcessInstanceByKey("echoShellMac");

      String st = (String) runtimeService.getVariable(pi.getId(), "resultVar");
      assertThat(st).isNotNull();
      assertThat(st.startsWith("EchoTest")).isTrue();
    }
  }
}
