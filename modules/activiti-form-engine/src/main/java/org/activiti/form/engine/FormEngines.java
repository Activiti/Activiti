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
package org.activiti.form.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FormEngines {

  private static Logger log = LoggerFactory.getLogger(FormEngines.class);

  public static final String NAME_DEFAULT = "default";

  protected static boolean isInitialized;
  protected static Map<String, FormEngine> formEngines = new HashMap<String, FormEngine>();
  protected static Map<String, FormEngineInfo> formEngineInfosByName = new HashMap<String, FormEngineInfo>();
  protected static Map<String, FormEngineInfo> formEngineInfosByResourceUrl = new HashMap<String, FormEngineInfo>();
  protected static List<FormEngineInfo> formEngineInfos = new ArrayList<FormEngineInfo>();

  /**
   * Initializes all dmn engines that can be found on the classpath for resources <code>activiti.dmn.cfg.xml</code> and for resources <code>activiti-dmn-context.xml</code> (Spring style
   * configuration).
   */
  public synchronized static void init() {
    if (!isInitialized()) {
      if (formEngines == null) {
        // Create new map to store dmn engines if current map is null
        formEngines = new HashMap<String, FormEngine>();
      }
      ClassLoader classLoader = FormEngines.class.getClassLoader();
      Enumeration<URL> resources = null;
      try {
        resources = classLoader.getResources("activiti.form.cfg.xml");
      } catch (IOException e) {
        throw new ActivitiFormException("problem retrieving activiti.form.cfg.xml resources on the classpath: " + System.getProperty("java.class.path"), e);
      }

      // Remove duplicated configuration URL's using set. Some
      // classloaders may return identical URL's twice, causing duplicate startups
      Set<URL> configUrls = new HashSet<URL>();
      while (resources.hasMoreElements()) {
        configUrls.add(resources.nextElement());
      }
      for (Iterator<URL> iterator = configUrls.iterator(); iterator.hasNext();) {
        URL resource = iterator.next();
        log.info("Initializing form engine using configuration '{}'", resource.toString());
        initFormEngineFromResource(resource);
      }

      /*
       * try { resources = classLoader.getResources("activiti-form-context.xml"); } catch (IOException e) { throw new ActivitiDmnException(
       * "problem retrieving activiti-dmn-context.xml resources on the classpath: " + System.getProperty("java.class.path"), e); } while (resources.hasMoreElements()) { URL resource =
       * resources.nextElement(); log.info("Initializing dmn engine using Spring configuration '{}'", resource.toString()); initDmnEngineFromSpringResource(resource); }
       */

      setInitialized(true);
    } else {
      log.info("Form engines already initialized");
    }
  }

  /**
   * Registers the given dmn engine. No {@link FormEngineInfo} will be available for this dmn engine. An engine that is registered will be closed when the {@link FormEngines#destroy()} is called.
   */
  public static void registerDmnEngine(FormEngine formEngine) {
    formEngines.put(formEngine.getName(), formEngine);
  }

  /**
   * Unregisters the given dmn engine.
   */
  public static void unregister(FormEngine formEngine) {
    formEngines.remove(formEngine.getName());
  }

  private static FormEngineInfo initFormEngineFromResource(URL resourceUrl) {
    FormEngineInfo formEngineInfo = formEngineInfosByResourceUrl.get(resourceUrl.toString());
    // if there is an existing dmn engine info
    if (formEngineInfo != null) {
      // remove that dmn engine from the member fields
      formEngineInfos.remove(formEngineInfo);
      if (formEngineInfo.getException() == null) {
        String formEngineName = formEngineInfo.getName();
        formEngines.remove(formEngineName);
        formEngineInfosByName.remove(formEngineName);
      }
      formEngineInfosByResourceUrl.remove(formEngineInfo.getResourceUrl());
    }

    String resourceUrlString = resourceUrl.toString();
    try {
      log.info("initializing dmn engine for resource {}", resourceUrl);
      FormEngine formEngine = buildFormEngine(resourceUrl);
      String formEngineName = formEngine.getName();
      log.info("initialised form engine {}", formEngineName);
      formEngineInfo = new FormEngineInfo(formEngineName, resourceUrlString, null);
      formEngines.put(formEngineName, formEngine);
      formEngineInfosByName.put(formEngineName, formEngineInfo);
    } catch (Throwable e) {
      log.error("Exception while initializing form engine: {}", e.getMessage(), e);
      formEngineInfo = new FormEngineInfo(null, resourceUrlString, getExceptionString(e));
    }
    formEngineInfosByResourceUrl.put(resourceUrlString, formEngineInfo);
    formEngineInfos.add(formEngineInfo);
    return formEngineInfo;
  }

  private static String getExceptionString(Throwable e) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    return sw.toString();
  }

  protected static FormEngine buildFormEngine(URL resource) {
    InputStream inputStream = null;
    try {
      inputStream = resource.openStream();
      FormEngineConfiguration formEngineConfiguration = FormEngineConfiguration.createFormEngineConfigurationFromInputStream(inputStream);
      return formEngineConfiguration.buildFormEngine();

    } catch (IOException e) {
      throw new ActivitiFormException("couldn't open resource stream: " + e.getMessage(), e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /** Get initialization results. */
  public static List<FormEngineInfo> getFormEngineInfos() {
    return formEngineInfos;
  }

  /**
   * Get initialization results. Only info will we available for form engines which were added in the {@link FormEngines#init()}. No {@link FormEngineInfo} is available for engines which were
   * registered programmatically.
   */
  public static FormEngineInfo getFormEngineInfo(String formEngineName) {
    return formEngineInfosByName.get(formEngineName);
  }

  public static FormEngine getDefaultFormEngine() {
    return getFormEngine(NAME_DEFAULT);
  }

  /**
   * obtain a dmn engine by name.
   * 
   * @param dmnEngineName
   *          is the name of the dmn engine or null for the default dmn engine.
   */
  public static FormEngine getFormEngine(String formEngineName) {
    if (!isInitialized()) {
      init();
    }
    return formEngines.get(formEngineName);
  }

  /**
   * retries to initialize a dmn engine that previously failed.
   */
  public static FormEngineInfo retry(String resourceUrl) {
    log.debug("retying initializing of resource {}", resourceUrl);
    try {
      return initFormEngineFromResource(new URL(resourceUrl));
    } catch (MalformedURLException e) {
      throw new ActivitiFormException("invalid url: " + resourceUrl, e);
    }
  }

  /**
   * provides access to dmn engine to application clients in a managed server environment.
   */
  public static Map<String, FormEngine> getFormEngines() {
    return formEngines;
  }

  /**
   * closes all dmn engines. This method should be called when the server shuts down.
   */
  public synchronized static void destroy() {
    if (isInitialized()) {
      Map<String, FormEngine> engines = new HashMap<String, FormEngine>(formEngines);
      formEngines = new HashMap<String, FormEngine>();

      for (String formEngineName : engines.keySet()) {
        FormEngine formEngine = engines.get(formEngineName);
        try {
          formEngine.close();
        } catch (Exception e) {
          log.error("exception while closing {}", (formEngineName == null ? "the default form engine" : "form engine " + formEngineName), e);
        }
      }

      formEngineInfosByName.clear();
      formEngineInfosByResourceUrl.clear();
      formEngineInfos.clear();

      setInitialized(false);
    }
  }

  public static boolean isInitialized() {
    return isInitialized;
  }

  public static void setInitialized(boolean isInitialized) {
    FormEngines.isInitialized = isInitialized;
  }
}
