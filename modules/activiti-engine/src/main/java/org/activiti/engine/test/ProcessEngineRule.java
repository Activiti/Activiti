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

package org.activiti.engine.test;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.AssertionFailedError;

import org.activiti.engine.DbProcessEngineBuilder;
import org.activiti.engine.ProcessEngine;
import org.activiti.impl.time.Clock;
import org.activiti.impl.util.LogUtil;
import org.activiti.impl.util.LogUtil.ThreadLogMode;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;


/**
 * @author Tom Baeyens
 */
public class ProcessEngineRule implements MethodRule {

  private static final String EMPTY_LINE = "                                                                                           ";

  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }
  
  private static Logger log = Logger.getLogger(ProcessEngineRule.class.getName());

  private static final ThreadLogMode DEFAULT_THREAD_LOG_MODE = ThreadLogMode.INDENT;
  private static final String DEFAULT_CONFIGURATION_RESOURCE = "activiti.properties";
  private static Map<String, ProcessEngine> processEngines = new HashMap<String, ProcessEngine>(); 
  
  protected ThreadLogMode threadRenderingMode;
  protected String configurationResource;
  protected ProcessEngine processEngine;
  
  public ProcessEngineRule() {
    this(DEFAULT_CONFIGURATION_RESOURCE, DEFAULT_THREAD_LOG_MODE);
  }
  
  public ProcessEngineRule(String configurationResource) {
    this(configurationResource, DEFAULT_THREAD_LOG_MODE);
  }
  
  public ProcessEngineRule(ThreadLogMode threadRenderingMode) {
    this(DEFAULT_CONFIGURATION_RESOURCE, threadRenderingMode);
  }
  
  public ProcessEngineRule(String configurationResource, ThreadLogMode threadRenderingMode) {
    this.configurationResource = configurationResource;
    this.threadRenderingMode = threadRenderingMode;
  }

  public Statement apply(Statement base, FrameworkMethod method, Object target) {
    return new ProcessEngineStatement(base, method, threadRenderingMode);
  }
 
  private class ProcessEngineStatement extends Statement {
    private final Statement base;
    private final FrameworkMethod method;
    private final ThreadLogMode threadRenderingMode;

    public ProcessEngineStatement(Statement base, FrameworkMethod method, ThreadLogMode threadRenderingMode) {
      this.base= base;
      this.method = method;
      this.threadRenderingMode = threadRenderingMode;
    }

    @Override
    public void evaluate() throws Throwable {

      LogUtil.resetThreadIndents();
      ThreadLogMode oldThreadRenderingMode = LogUtil.setThreadLogMode(threadRenderingMode);
      
      if (processEngine==null) {
        processEngine = processEngines.get(configurationResource);
        if (processEngine==null) {
          log.fine("#### BUILDING PROCESS ENGINE ########################################################################");
          processEngine = new DbProcessEngineBuilder()
            .configureFromPropertiesResource(configurationResource)
            .buildProcessEngine();
          log.fine("#### PROCESS ENGINE CREATED #########################################################################");
        }
      }

      log.fine(EMPTY_LINE);
      log.fine("---- START "+method.getMethod().getDeclaringClass().getName()+"."+method.getName()+" ------------------------------------------------------");

      try {
        
        base.evaluate();

      }  catch (AssertionFailedError e) {
        log.severe(EMPTY_LINE);
        log.log(Level.SEVERE, "ASSERTION FAILED: "+e, e);
        throw e;
      } catch (Throwable e) {
        log.severe(EMPTY_LINE);
        log.log(Level.SEVERE, "EXCEPTION: "+e, e);
        throw e;
      } finally {
        Clock.reset();
        log.fine("---- END "+method.getMethod().getDeclaringClass().getName()+"."+method.getName()+" ------------------------------------------------------");
        LogUtil.setThreadLogMode(oldThreadRenderingMode);
      }
    }

  }

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }
}
