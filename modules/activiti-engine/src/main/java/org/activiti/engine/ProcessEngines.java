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
package org.activiti.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.ProcessEngineInfoImpl;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;



/** Helper for initializing and closing process engines in server environments.
 * <br>
 * All created {@link ProcessEngine}s will be registered with this class.
 * <br>
 * The activiti-webapp-init webapp will
 * call the {@link #init()} method when the webapp is deployed and it will call the 
 * {@link #destroy()} method when the webapp is destroyed, using a context-listener 
 * (<code>org.activiti.impl.servlet.listener.ProcessEnginesServletContextListener</code>).  That way, 
 * all applications can just use the {@link #getProcessEngines()} to 
 * obtain pre-initialized and cached process engines. <br>
 * <br>
 * Please note that there is <b>no lazy initialization</b> of process engines, so make sure the 
 * context-listener is configured or {@link ProcessEngine}s are already created so they were registered
 * on this class.<br>
 * <br>
 * The {@link #init()} method will try to build one {@link ProcessEngine} for 
 * each activiti.cfg.xml file found on the classpath.  If you have more then one,
 * make sure you specify different process.engine.name values.
 *  
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public abstract class ProcessEngines {
  
  private static Logger log = Logger.getLogger(ProcessEngines.class.getName());
  
  public static final String NAME_DEFAULT = "default";
  
  protected static boolean isInitialized = false; 
  protected static Map<String, ProcessEngine> processEngines = new HashMap<String, ProcessEngine>();
  protected static Map<String, ProcessEngineInfo> processEngineInfosByName = new HashMap<String, ProcessEngineInfo>();
  protected static Map<String, ProcessEngineInfo> processEngineInfosByResourceUrl = new HashMap<String, ProcessEngineInfo>();
  protected static List<ProcessEngineInfo> processEngineInfos = new ArrayList<ProcessEngineInfo>();
  
  /** Initializes all process engines that can be found on the classpath for 
   * resources <code>activiti.cfg.xml</code> (plain Activiti style configuration)
   * and for resources <code>activiti-context.xml</code> (Spring style configuration). */
  public synchronized static void init() {
    if (!isInitialized) {
      if(processEngines == null) {
        // Create new map to store process-engines if current map is null
        processEngines = new HashMap<String, ProcessEngine>();        
      }
      ClassLoader classLoader = ReflectUtil.getClassLoader();
      Enumeration<URL> resources = null;
      try {
        resources = classLoader.getResources("activiti.cfg.xml");
      } catch (IOException e) {
        throw new ActivitiException("problem retrieving activiti.cfg.xml resources on the classpath: "+System.getProperty("java.class.path"), e);
      }
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        initProcessEnginFromResource(resource);
      }
      
      try {
        resources = classLoader.getResources("activiti-context.xml");
      } catch (IOException e) {
        throw new ActivitiException("problem retrieving activiti-context.xml resources on the classpath: "+System.getProperty("java.class.path"), e);
      }
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        initProcessEngineFromSpringResource(resource);
      }

      isInitialized = true;
    } else {
      log.info("Process engines already initialized");
    }
  }

  protected static void initProcessEngineFromSpringResource(URL resource) {
    try {
      Class< ? > springConfigurationHelperClass = ReflectUtil.loadClass("org.activiti.spring.SpringConfigurationHelper");
      Method method = springConfigurationHelperClass.getMethod("buildProcessEngine", new Class<?>[]{URL.class});
      method.invoke(null, new Object[]{resource});
    } catch (Exception e) {
      throw new ActivitiException("couldn't initialize process engine from spring configuration resource "+resource.toString()+": "+e.getMessage(), e);
    } 
  }
 
  /**
   * Registers the given process engine. No {@link ProcessEngineInfo} will be 
   * available for this process engine. An engine that is registered will be closed
   * when the {@link ProcessEngines#destroy()} is called.
   */
  public static void registerProcessEngine(ProcessEngine processEngine) {
    processEngines.put(processEngine.getName(), processEngine);
  }
  
  /**
   * Unregisters the given process engine.
   */
  public static void unregister(ProcessEngine processEngine) {
    processEngines.remove(processEngine.getName());
  }

  private static ProcessEngineInfo initProcessEnginFromResource(URL resourceUrl) {
    ProcessEngineInfo processEngineInfo = processEngineInfosByResourceUrl.get(resourceUrl);
    // if there is an existing process engine info
    if (processEngineInfo!=null) {
      // remove that process engine from the member fields
      processEngineInfos.remove(processEngineInfo);
      if (processEngineInfo.getException()==null) {
        String processEngineName = processEngineInfo.getName();
        processEngines.remove(processEngineName);
        processEngineInfosByName.remove(processEngineName);
      }
      processEngineInfosByResourceUrl.remove(processEngineInfo.getResourceUrl());
    }

    String resourceUrlString = resourceUrl.toString();
    try {
      log.info("initializing process engine for resource " + resourceUrl);
      ProcessEngine processEngine = buildProcessEngine(resourceUrl);
      String processEngineName = processEngine.getName();
      log.info("initialised process engine " + processEngineName);
      processEngineInfo = new ProcessEngineInfoImpl(processEngineName, resourceUrlString, null);
      processEngines.put(processEngineName, processEngine);
      processEngineInfosByName.put(processEngineName, processEngineInfo);
    } catch (Throwable e) {
      log.log(Level.SEVERE, "Exception while initializing process engine :" + e.getMessage(), e);
      processEngineInfo = new ProcessEngineInfoImpl(null, resourceUrlString, getExceptionString(e));
    }
    processEngineInfosByResourceUrl.put(resourceUrlString, processEngineInfo);
    processEngineInfos.add(processEngineInfo);
    return processEngineInfo;
  }

  private static String getExceptionString(Throwable e) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    return sw.toString();
  }

  private static  ProcessEngine buildProcessEngine(URL resource) {
    InputStream inputStream = null;
    try {
      inputStream = resource.openStream();
      ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createProcessEngineConfigurationFromInputStream(inputStream);
      return processEngineConfiguration.buildProcessEngine();
      
    } catch (IOException e) {
      throw new ActivitiException("couldn't open resource stream: "+e.getMessage(), e);
    } finally {
      IoUtil.closeSilently(inputStream);
    }
  }
  
  /** Get initialization results. */
  public static List<ProcessEngineInfo> getProcessEngineInfos() {
    return processEngineInfos;
  }

  /** Get initialization results. Only info will we available for process engines
   * which were added in the {@link ProcessEngines#init()}. No {@link ProcessEngineInfo}
   * is available for engines which were registered programatically.
  */
  public static ProcessEngineInfo getProcessEngineInfo(String processEngineName) {
    return processEngineInfosByName.get(processEngineName);
  }

  public static ProcessEngine getDefaultProcessEngine() {
    return getProcessEngine(NAME_DEFAULT);
  }

  /** obtain a process engine by name.  
   * @param processEngineName is the name of the process engine or null for the default process engine.  */
  public static ProcessEngine getProcessEngine(String processEngineName) {
    if (!isInitialized) {
      init();
    }
    return processEngines.get(processEngineName);
  }
  
  /** retries to initialize a process engine that previously failed.
   */
  public static ProcessEngineInfo retry(String resourceUrl) {
    log.fine("retying initializing of resource " + resourceUrl);
    try {
      return initProcessEnginFromResource(new URL(resourceUrl));
    } catch (MalformedURLException e) {
      throw new ActivitiException("invalid url: "+resourceUrl, e);
    }
  }
  
  /** provides access to process engine to application clients in a 
   * managed server environment.  
   */
  public static Map<String, ProcessEngine> getProcessEngines() {
    return processEngines;
  }
  
  /** closes all process engines.  This method is called when a server shutsdown by the activiti-rest webapp. */
  public synchronized static void destroy() {
    if (isInitialized) {
      Map<String, ProcessEngine> engines = new HashMap<String, ProcessEngine>(processEngines);
      processEngines = new HashMap<String, ProcessEngine>();
      
      for (String processEngineName: engines.keySet()) {
        ProcessEngine processEngine = engines.get(processEngineName);
        try {
          processEngine.close();
        } catch (Exception e) {
          log.log(Level.SEVERE, "exception while closing "+(processEngineName==null ? "the default process engine" : "process engine "+processEngineName), e);
        }
      }
      
      processEngineInfosByName.clear();
      processEngineInfosByResourceUrl.clear();
      processEngineInfos.clear();
      
      isInitialized = false;
    }
  }
}
