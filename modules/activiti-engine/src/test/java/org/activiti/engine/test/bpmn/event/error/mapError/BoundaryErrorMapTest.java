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
package org.activiti.engine.test.bpmn.event.error.mapError;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;
import org.activiti.standalone.testing.helpers.ServiceTaskTestMock;

/**
 * @author Saeid Mirzaei
 */
public class BoundaryErrorMapTest extends PluggableActivitiTestCase{
  
  
  // exception matches the only mapping, directly
  @Deployment
  public void testClassDelegateSingleDirectMap() {
    FlagDelegate.reset();
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("exceptionClass", BoundaryErrorParentException.class.getName());
    
    runtimeService.startProcessInstanceByKey("processWithSingleExceptionMap", vars);
    assertTrue(FlagDelegate.isVisited());
  }

  // exception does not match the single mapping
  @Deployment(resources="org/activiti/engine/test/bpmn/event/error/mapError/BoundaryErrorMapTest.testClassDelegateSingleDirectMap.bpmn20.xml")
  public void testClassDelegateSingleDirectMapNotMachingException() {
    FlagDelegate.reset();
    
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("exceptionClass", JAXBException.class.getName());
    assertEquals(0, ServiceTaskTestMock.CALL_COUNT.get());
    
    try {
      runtimeService.startProcessInstanceByKey("processWithSingleExceptionMap", vars);
      fail("exception expected, as there is no matching exceptio map");
    } catch (Exception e) {
      assertFalse(FlagDelegate.isVisited());
    }
  }

  // exception matches by inheritence
  @Deployment
  public void testClassDelegateSingleInheritedMap() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("exceptionClass", BoundaryEventChildException.class.getName());
    FlagDelegate.reset();
    
    runtimeService.startProcessInstanceByKey("processWithSingleExceptionMap", vars);
    assertTrue(FlagDelegate.isVisited());
  }
  
  // check the default map
  @Deployment
  public void tesClassDelegatetDefaultMap() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("exceptionClass", Exception.class.getName());
    FlagDelegate.reset();
    
    runtimeService.startProcessInstanceByKey("processWithSingleExceptionMap", vars);
    assertTrue(FlagDelegate.isVisited());
    
  }
  
  
  @Deployment
  public void testSeqMultInstanceSingleDirectMap() {
    FlagDelegate.reset();
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("exceptionClass", BoundaryErrorParentException.class.getName());
    
    runtimeService.startProcessInstanceByKey("processWithSingleExceptionMap", vars);
    assertTrue(FlagDelegate.isVisited());
  }
  
  @Deployment
  public void testSubProcessSingleDirectMap() {
    FlagDelegate.reset();
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("exceptionClass", BoundaryErrorParentException.class.getName());
    
    runtimeService.startProcessInstanceByKey("subprocssWithSingleExceptionMap", vars);
    assertTrue(FlagDelegate.isVisited());
  }

  @Deployment(resources= {"org/activiti/engine/test/bpmn/event/error/mapError/BoundaryErrorMapTest.testCallProcessSingleDirectMap.bpmn20.xml",
          "org/activiti/engine/test/bpmn/event/error/mapError/BoundaryErrorMapTest.testCallProcessCalee.bpmn20.xml"})

  public void testCallProcessSingleDirectMap() {
    FlagDelegate.reset();
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("exceptionClass", BoundaryErrorParentException.class.getName());
    
    runtimeService.startProcessInstanceByKey("callProcssWithSingleExceptionMap", vars);
    assertTrue(FlagDelegate.isVisited());
  }


}
