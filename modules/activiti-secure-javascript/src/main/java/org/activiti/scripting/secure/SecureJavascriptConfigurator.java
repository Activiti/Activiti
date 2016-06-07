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
package org.activiti.scripting.secure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.cfg.AbstractProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.parse.BpmnParseHandler;
import org.activiti.scripting.secure.behavior.SecureJavascriptTaskParseHandler;
import org.activiti.scripting.secure.impl.SecureScriptClassShutter;
import org.activiti.scripting.secure.impl.SecureScriptContextFactory;
import org.mozilla.javascript.ContextFactory;

/**
 * @author Joram Barrez
 */
public class SecureJavascriptConfigurator extends AbstractProcessEngineConfigurator {

  /* Rhino's global context factory */
  public static SecureScriptContextFactory secureScriptContextFactory;
  public static SecureScriptClassShutter secureScriptClassShutter;

  /**
   * When true, by default all classes will be blacklisted and all classes that
   * want to be used will need to be whitelisted individually.
   */
  protected boolean enableClassWhiteListing;

  /**
   * Whitelisted classes for script execution.
   *
   * By default empty (i.e. everything is blacklisted)
   *
   * From the Rhino ClassShutter javadoc:
   *
   * Due to the fact that there is no package reflection in Java, this method
   * will also be called with package names. There is no way for Rhino to tell
   * if "Packages.a.b" is a package name or a class that doesn't exist. What
   * Rhino does is attempt to load each segment of "Packages.a.b.c": It first
   * attempts to load class "a", then attempts to load class "a.b", then finally
   * attempts to load class "a.b.c". On a Rhino installation without any
   * ClassShutter set, and without any of the above classes, the expression
   * "Packages.a.b.c" will result in a [JavaPackage a.b.c] and not an error.
   *
   * With ClassShutter supplied, Rhino will first call visibleToScripts before
   * attempting to look up the class name. If visibleToScripts returns false,
   * the class name lookup is not performed and subsequent Rhino execution
   * assumes the class is not present. So for "java.lang.System.out.println" the
   * lookup of "java.lang.System" is skipped and thus Rhino assumes that
   * "java.lang.System" doesn't exist. So then for "java.lang.System.out", Rhino
   * attempts to load the class "java.lang.System.out" because it assumes that
   * "java.lang.System" is a package name.
   */
  protected Set<String> whiteListedClasses;

  /**
   * The maximum time (in ms) that a script is allowed to execute before
   * stopping it.
   *
   * By default disabled.
   */
  protected long maxScriptExecutionTime = -1L;

  /**
   * Limits the stack depth while calling functions within the script.
   *
   * By default disabled.
   */
  protected int maxStackDepth = -1;

  /**
   * Limits the memory used by the script. If the memory limit is reached, an
   * exception will be thrown and the script will be stopped.
   */
  protected long maxMemoryUsed = -1L;

  /**
   * The maximum script execution time and memory usage is implemented using a
   * callback that is called every x instructions of the script. Note that these
   * are not script instructions, but java byte code instructions (which means
   * one script line can be thousands of byte code instructions!).
   */
  protected int nrOfInstructionsBeforeStateCheckCallback = 100;

  /**
   * By default, no script optimization is applied. Change this setting to
   * change the Rhino script optimization level. Note: some simple performance
   * tests seem to indicate that for basic scripts upping this value actually
   * has worse results ...
   *
   * Note: if using a maxStackDepth setting, the script optimization level will
   * always be -1.
   */
  protected int scriptOptimizationLevel = -1;
  
  @Override
  public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    
    // Initialize the Rhino context factory (needs to be done once)
    if (secureScriptContextFactory == null) {
      initSecureScriptContextFactory();
    }
    
    // Init parse handler that will set the secure javascript task to the activity
    List<BpmnParseHandler> customDefaultBpmnParseHandlers = processEngineConfiguration.getCustomDefaultBpmnParseHandlers();
    if (customDefaultBpmnParseHandlers == null) {
      customDefaultBpmnParseHandlers = new ArrayList<BpmnParseHandler>();
      processEngineConfiguration.setCustomDefaultBpmnParseHandlers(customDefaultBpmnParseHandlers);
    }
    customDefaultBpmnParseHandlers.add(new SecureJavascriptTaskParseHandler());
  }

  protected synchronized void initSecureScriptContextFactory() {
    if (secureScriptContextFactory == null) {
      secureScriptContextFactory = new SecureScriptContextFactory();

      secureScriptContextFactory.setOptimizationLevel(getScriptOptimizationLevel());

      if (isEnableClassWhiteListing() || getWhiteListedClasses() != null) {
        secureScriptClassShutter = new SecureScriptClassShutter();
        if (getWhiteListedClasses() != null && getWhiteListedClasses().size() > 0) {
          secureScriptClassShutter .setWhiteListedClasses(getWhiteListedClasses());
        }
        secureScriptContextFactory.setClassShutter(secureScriptClassShutter);
      }

      if (getMaxScriptExecutionTime() > 0L) {
        secureScriptContextFactory.setMaxScriptExecutionTime(getMaxScriptExecutionTime());
      }

      if (getMaxMemoryUsed() > 0L) {
        secureScriptContextFactory.setMaxMemoryUsed(getMaxMemoryUsed());
      }

      if (getMaxStackDepth() > 0) {
        secureScriptContextFactory.setMaxStackDepth(getMaxStackDepth());
      }

      if (getMaxScriptExecutionTime() > 0L || getMaxMemoryUsed() > 0L) {
        secureScriptContextFactory.setObserveInstructionCount(getNrOfInstructionsBeforeStateCheckCallback());
      }

      ContextFactory.initGlobal(secureScriptContextFactory);
    }
  }

  public boolean isEnableClassWhiteListing() {
    return enableClassWhiteListing;
  }

  public SecureJavascriptConfigurator setEnableClassWhiteListing(boolean enableClassWhiteListing) {
    this.enableClassWhiteListing = enableClassWhiteListing;
    return this;
  }

  public Set<String> getWhiteListedClasses() {
    return whiteListedClasses;
  }

  public SecureJavascriptConfigurator setWhiteListedClasses(Set<String> whiteListedClasses) {
    this.whiteListedClasses = whiteListedClasses;
    return this;
  }

  public SecureJavascriptConfigurator addWhiteListedClass(String whiteListedClass) {
    if (this.whiteListedClasses == null) {
      this.whiteListedClasses = new HashSet<String>();
    }
    this.whiteListedClasses.add(whiteListedClass);
    return this;
  }

  public long getMaxScriptExecutionTime() {
    return maxScriptExecutionTime;
  }

  public SecureJavascriptConfigurator setMaxScriptExecutionTime(long maxScriptExecutionTime) {
    this.maxScriptExecutionTime = maxScriptExecutionTime;
    return this;
  }

  public int getNrOfInstructionsBeforeStateCheckCallback() {
    return nrOfInstructionsBeforeStateCheckCallback;
  }

  public SecureJavascriptConfigurator setNrOfInstructionsBeforeStateCheckCallback(int nrOfInstructionsBeforeStateCheckCallback) {
    this.nrOfInstructionsBeforeStateCheckCallback = nrOfInstructionsBeforeStateCheckCallback;
    return this;
  }

  public int getMaxStackDepth() {
    return maxStackDepth;
  }

  public SecureJavascriptConfigurator setMaxStackDepth(int maxStackDepth) {
    this.maxStackDepth = maxStackDepth;
    return this;
  }

  public long getMaxMemoryUsed() {
    return maxMemoryUsed;
  }

  public SecureJavascriptConfigurator setMaxMemoryUsed(long maxMemoryUsed) {
    this.maxMemoryUsed = maxMemoryUsed;
    return this;
  }

  public int getScriptOptimizationLevel() {
    return scriptOptimizationLevel;
  }

  public SecureJavascriptConfigurator setScriptOptimizationLevel(int scriptOptimizationLevel) {
    this.scriptOptimizationLevel = scriptOptimizationLevel;
    return this;
  }

  public SecureScriptContextFactory getSecureScriptContextFactory() {
    return secureScriptContextFactory;
  }

  public static SecureScriptClassShutter getSecureScriptClassShutter() {
    return secureScriptClassShutter;
  }

}
