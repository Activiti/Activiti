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

package org.activiti.upgrade.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.AssertionFailedError;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.test.AbstractActivitiTestCase;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.impl.util.ClockUtil;


/**
 * @author Tom Baeyens
 */
public class UpgradeTestCase extends AbstractActivitiTestCase {

  private static Logger log = Logger.getLogger(UpgradeTestCase.class.getName());
  
  protected static ProcessEngine cachedProcessEngine = null;
  
  protected void initializeProcessEngine() {
    if (cachedProcessEngine==null) {
      processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResourceDefault();
      cachedProcessEngine = processEngineConfiguration.buildProcessEngine();
    }
    processEngine = cachedProcessEngine; 
  }
  
  @Override
  public void runBare() throws Throwable {
    initializeProcessEngine();
    if (repositoryService==null) {
      initializeServices();
    }

    log.severe(EMPTY_LINE);

    try {

      if (isBeforeTest()) {
        deploymentId = TestHelper.annotationDeploymentSetUp(processEngine, getClass(), getName());
      }
      
      Throwable exception= null;
      setUp();
      try {
        runTest();
      } catch (Throwable running) {
        exception= running;
      }
      finally {
        try {
          tearDown();
        } catch (Throwable tearingDown) {
          if (exception == null) exception= tearingDown;
        }
      }
      if (exception != null) throw exception;

    }  catch (AssertionFailedError e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "ASSERTION FAILED: "+e, e);
      exception = e;
      throw e;
      
    } catch (Throwable e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "EXCEPTION: "+e, e);
      exception = e;
      throw e;
      
    } finally {
      ClockUtil.reset();
    }
  }

  protected boolean isBeforeTest() {
    return getClass().getName().endsWith("BeforeTest");
  }
}
