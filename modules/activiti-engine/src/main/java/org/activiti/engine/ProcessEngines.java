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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.ProcessEngineInfoImpl;
import org.activiti.engine.impl.util.ReflectUtil;


/** Helper for initializing and closing process engines in server environments.
 * <br>
 * The activiti-webapp-init webapp will
 * call the {@link #init()} method when the webapp is deployed and it will call the 
 * {@link #destroy()} method when the webapp is destroyed, using a context-listener 
 * (<code>org.activiti.impl.servlet.listener.ProcessEnginesServletContextListener</code>).  That way, 
 * all applications can just use the {@link #getProcessEngines()} to 
 * obtain pre-initialized and cached process engines. <br>
 * <br>
 * Please note that there is <b>no lazy initialization</b> of process engines, so make sure the 
 * context-listener is configured.<br>
 * <br>
 * The {@link #init()} method will try to build one {@link ProcessEngine} for 
 * each activiti.properties file found on the classpath.  If you have more then one,
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

  /** is called when a server boots by the activiti-rest webapp. */
  public synchronized static void init() {
    if (!isInitialized) {
      ClassLoader classLoader = ReflectUtil.getClassLoader();
      Enumeration<URL> resources = null;
      try {
        resources = classLoader.getResources("activiti.properties");
      } catch (IOException e) {
        throw new ActivitiException("can't find activiti.properties resources on the classpath: "+System.getProperty("java.class.path"), e);
      }
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        initProcessEnginFromResource(resource);
      }
      isInitialized = true;
    } else {
      log.info("Process engines already initialized");
    }
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
      log.info("Exception while initializing process engine :" + e.getMessage());
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

  private static ProcessEngine buildProcessEngine(URL resource) {
    try {
      InputStream inputStream = resource.openStream();
      ProcessEngine processEngine = new ProcessEngineBuilder()
          .configureFromPropertiesInputStream(inputStream)
          .buildProcessEngine();
      return processEngine;
      
    } catch (IOException e) {
      throw new ActivitiException("couldn't open resource stream: "+e.getMessage(), e);
    }
  }
  
  /** get initialization results. */
  public static List<ProcessEngineInfo> getProcessEngineInfos() {
    return processEngineInfos;
  }

  /** get initialization results. */
  public static ProcessEngineInfo getProcessEngineInfo(String processEngineName) {
    return processEngineInfosByName.get(processEngineName);
  }

  public static ProcessEngine getDefaultProcessEngine() {
    return getProcessEngine(NAME_DEFAULT);
  }

  /** obtain a process engine by name.  
   * @param processEngineName is the name of the process engine or null for the default process engine.  */
  public static ProcessEngine getProcessEngine(String processEngineName) {
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
  @SuppressWarnings("unchecked")
  public synchronized static void destroy() {
    if (isInitialized) {
      Map<String, ProcessEngine> engines = new HashMap<String, ProcessEngine>(processEngines);
      processEngines = Collections.unmodifiableMap(Collections.EMPTY_MAP);
      
      for (String processEngineName: engines.keySet()) {
        ProcessEngine processEngine = engines.get(processEngineName);
        try {
          processEngine.close();
        } catch (Exception e) {
          log.log(Level.SEVERE, "exception while closing "+(processEngineName==null ? "the default process engine" : "process engine "+processEngineName), e);
        }
      }
      isInitialized = false;
    }
  }
}
