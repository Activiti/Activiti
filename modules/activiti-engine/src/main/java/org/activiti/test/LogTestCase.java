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
package org.activiti.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.activiti.impl.util.LogUtil;


/**
 * @author Tom Baeyens
 */
public class LogTestCase extends TestCase {

  private static final String EMPTY_LINE = "                                                                                           ";

  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }
  
  private static Logger log = Logger.getLogger(LogTestCase.class.getName());

  @Override
  protected void runTest() throws Throwable {
    try {
      super.runTest();
    } catch (AssertionFailedError e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "ASSERTION FAILED: "+e, e);
      throw e;
    } catch (Throwable e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "EXCEPTION: "+e, e);
      throw e;
    }
  }

  @Override
  protected void setUp() throws Exception {
    log.fine(EMPTY_LINE);
    log.fine("---- START "+getClass().getName()+"."+getName()+" ------------------------------------------------------");
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    log.fine("---- END "+getClass().getName()+"."+getName()+" ------------------------------------------------------");
  }
  
  public void assertTextPresent(String expected, String actual) {
    if ( (actual==null)
         || (actual.indexOf(expected)==-1)
       ) {
      throw new AssertionFailedError("expected presence of '"+expected+"' but was '"+actual+"'");
    }
  }

  public void assertExceptionMessage(String expected, Throwable exception) {
    if ( (exception==null)
         || (exception.getMessage()==null)
         || (exception.getMessage().indexOf(expected)==-1)
       ) {
      log.log(Level.SEVERE, "wrong exception: ", exception);
      throw new AssertionFailedError("expected presence of '"+expected+"' but was '"+exception+"' see logs for the original exception");
    }
  }
}
