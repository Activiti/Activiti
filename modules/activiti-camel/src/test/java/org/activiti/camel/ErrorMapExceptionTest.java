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

package org.activiti.camel;

import org.activiti.camel.util.FlagJavaDelegate;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Saeid Mirzaei
 */
@ContextConfiguration("classpath:generic-camel-activiti-context.xml")
public class ErrorMapExceptionTest extends SpringActivitiTestCase {

  @Autowired
  protected CamelContext camelContext;

  @Deployment(resources = { "process/mapExceptionSingleMap.bpmn20.xml" })
  public void testCamelSingleDirectMap() throws Exception {
    camelContext.addRoutes(new RouteBuilder() {

      @Override
      public void configure() throws Exception {
        from("activiti:mapExceptionProcess:exceptionRoute").throwException(new MapExceptionParent("test exception"));
      }
    });

    FlagJavaDelegate.reset();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("mapExceptionProcess");
    assertTrue(FlagJavaDelegate.isFlagSet());
  }

  @Deployment(resources = { "process/mapExceptionDefaultMap.bpmn20.xml" })
  public void testCamelDefaultMap() throws Exception {
    camelContext.addRoutes(new RouteBuilder() {

      @Override
      public void configure() throws Exception {
        from("activiti:mapExceptionDefaultProcess:exceptionRoute").throwException(new NullPointerException("test exception"));
      }
    });
    FlagJavaDelegate.reset();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("mapExceptionDefaultProcess");
    assertTrue(FlagJavaDelegate.isFlagSet());
  }

  @Deployment(resources = { "process/mapExceptionParentMap.bpmn20.xml" })
  public void testCamelParentMap() throws Exception {
    camelContext.addRoutes(new RouteBuilder() {

      @Override
      public void configure() throws Exception {
        from("activiti:mapExceptionParentProcess:exceptionRoute").throwException(new MapExceptionChild("test exception"));
      }
    });
    FlagJavaDelegate.reset();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("mapExceptionParentProcess");
    assertTrue(FlagJavaDelegate.isFlagSet());
  }

}
