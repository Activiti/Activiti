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
package org.activiti.test.mule;

import org.activiti.engine.impl.webservice.SyncWebServiceClient;
import org.mule.tck.FunctionalTestCase;

/**
 * An abstract test class to validate that SyncWebServiceClient subclasses
 * behave correctly using a Counter WS
 * 
 * @author Esteban Robles Luna
 */
public abstract class AbstractSyncWebServiceClientIntegrationTest extends FunctionalTestCase {

  protected SyncWebServiceClient client;

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();
    this.client = this.getClient();
  }
  
  @Override
  protected String getConfigResources() {
    return "org/activiti/test/mule/mule-cxf-webservice-config.xml";
  }

  protected abstract SyncWebServiceClient getClient();

  public void testDefaultValue() throws Exception {
    assertEquals(-1, this.getCount());
  }

  public void testInitialize() throws Exception {
    this.reset();
    assertEquals(0, this.getCount());
  }

  public void testInc() throws Exception {
    this.reset();
    this.inc();
    this.inc();
    assertEquals(2, this.getCount());
  }

  public void testInitializeTo() throws Exception {
    this.reset();
    this.inc();
    this.inc();
    this.setTo(24);
    assertEquals(24, this.getCount());
    this.inc();
    assertEquals(25, this.getCount());
  }

  public void testPrettyPrint() throws Exception {
    this.reset();
    this.inc();
    String result = this.prettyPrintCount("The counter has a value of ", " hits");
    assertEquals("The counter has a value of 1 hits", result);
  }

  private String prettyPrintCount(String prefix, String suffix) throws Exception {
    Object[] results = this.client.send("prettyPrintCount", new Object[] { prefix, suffix });
    return (String) results[0];
  }

  private void inc() throws Exception {
    this.client.send("inc", new Object[] {});
  }

  private int getCount() throws Exception {
    Object[] results = this.client.send("getCount", new Object[] {});
    return (Integer) results[0];
  }

  private void reset() throws Exception {
    this.client.send("reset", new Object[] {});
  }

  private void setTo(Integer value) throws Exception {
    this.client.send("setTo", new Object[] { value });
  }
}
