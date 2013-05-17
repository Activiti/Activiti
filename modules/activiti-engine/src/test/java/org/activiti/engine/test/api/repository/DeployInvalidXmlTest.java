package org.activiti.engine.test.api.repository;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

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

/**
 * @author Joram Barrez
 */
public class DeployInvalidXmlTest extends PluggableActivitiTestCase {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    processEngineConfiguration.setEnableSafeBpmnXml(true); // Needs to be enabled to test this
  }
  
  @Override
  protected void tearDown() throws Exception {
    processEngineConfiguration.setEnableSafeBpmnXml(false); // set back to default
    super.tearDown();
  }
  
  public void testDeployNonSchemaConformantXml() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/api/repository/nonSchemaConformantXml.bpmn20.xml")
        .deploy()
        .getId();
      fail();
    } catch (ActivitiException e) {
      e.printStackTrace();
      assertTextPresent("Could not validate XML with BPMN 2.0 XSD", e.getCause().getMessage());
    }
    
  }
  
  public void testDeployWithMissingWaypointsForSequenceflowInDiagramInterchange() {
    try {
      repositoryService.createDeployment()
            .addClasspathResource("org/activiti/engine/test/api/repository/noWayPointsForSequenceFlowInDiagramInterchange.bpmn20.xml")
            .deploy()
            .getId();
      fail();
    } catch (ActivitiException e) {
      e.printStackTrace();
      assertTextPresent("Could not validate XML with BPMN 2.0 XSD", e.getCause().getMessage());
    }
  }
  
  // Need to put this in a String here, if we use a separate file, the cpu usage
  // of Eclipse skyrockets, regardless of the file is opened or not
  
  private static String UNSAFE_XML = "<?xml version='1.0' encoding='UTF-8'?>" +
  "<!-- Billion Laugh attacks : http://portal.sliderocket.com/CJAKM/xml-attacks -->" +
  "<!DOCTYPE lols [" +
    "<!ENTITY lol 'lol'>" +
    "<!ENTITY lol1 '&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;'>" +
    "<!ENTITY lol2 '&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;'>" +
    "<!ENTITY lol3 '&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;'>" +
    "<!ENTITY lol4 '&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;'>" +
    "<!ENTITY lol5 '&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;'>" +
    "<!ENTITY lol6 '&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;'>" +
    "<!ENTITY lol7 '&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;'>" +
    "<!ENTITY lol8 '&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;'>" +
    "<!ENTITY lol9 '&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;'>" +
  "]>" +
  "<lolz>&lol9;</lolz>" +
  "<definitions" +
    "xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL'" +
    "xmlns:activiti='http://activiti.org/bpmn'" +
    "targetNamespace='Examples'>" +
    "<process id='oneTaskProcess' name='The One Task Process'>" +
    "  <documentation>This is a process for testing purposes</documentation>" +
    " <startEvent id='theStart' />" +
    " <sequenceFlow id='flow1' sourceRef='theStart' targetRef='theTask' />" +
    " <userTask id='theTask' name='my task' />" +
    " <sequenceFlow id='flow2' sourceRef='theTask' targetRef='theEnd' />" +
    " <endEvent id='theEnd' />" +
    "</process>" +
  "</definitions>";
  
  
  // See http://jira.codehaus.org/browse/ACT-1579?focusedCommentId=319886#comment-319886
  public void testProcessEngineDenialOfServiceAttackUsingUnsafeXmlTest() throws InterruptedException {

    MyRunnable runnable = new MyRunnable(repositoryService);
    Thread thread = new Thread(runnable);
    thread.start();
    
    long waitTime = 60000L;
    thread.join(waitTime);
    
    assertTrue(runnable.finished);

  }
  
  static class MyRunnable implements Runnable {
    
    public boolean finished = false;
    
    protected RepositoryService repositoryService;
    
    public MyRunnable(RepositoryService repositoryService) {
      this.repositoryService = repositoryService;
    }
    
    public void run() {
      try {
        String deploymentId = repositoryService.createDeployment().addString("test.bpmn20.xml", UNSAFE_XML).deploy().getId();
        repositoryService.deleteDeployment(deploymentId, true);
      } catch (Exception e) {
        // Exception is expected
      }
      finally {
        finished = true;
      }
    }
    
  }

}
