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
package org.activiti.dmn.engine;

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

public abstract class DmnEngines {

    private static Logger log = LoggerFactory.getLogger(DmnEngines.class);

    public static final String NAME_DEFAULT = "default";

    protected static boolean isInitialized;
    protected static Map<String, DmnEngine> dmnEngines = new HashMap<String, DmnEngine>();
    protected static Map<String, DmnEngineInfo> dmnEngineInfosByName = new HashMap<String, DmnEngineInfo>();
    protected static Map<String, DmnEngineInfo> dmnEngineInfosByResourceUrl = new HashMap<String, DmnEngineInfo>();
    protected static List<DmnEngineInfo> dmnEngineInfos = new ArrayList<DmnEngineInfo>();

    /**
     * Initializes all dmn engines that can be found on the classpath for
     * resources <code>activiti.dmn.cfg.xml</code> and for resources
     * <code>activiti-dmn-context.xml</code> (Spring style configuration).
     */
    public synchronized static void init() {
        if (!isInitialized()) {
            if (dmnEngines == null) {
                // Create new map to store dmn engines if current map is null
                dmnEngines = new HashMap<String, DmnEngine>();
            }
            ClassLoader classLoader = DmnEngines.class.getClassLoader();
            Enumeration<URL> resources = null;
            try {
                resources = classLoader.getResources("activiti.dmn.cfg.xml");
            } catch (IOException e) {
                throw new ActivitiDmnException("problem retrieving activiti.dmn.cfg.xml resources on the classpath: " + System.getProperty("java.class.path"),
                        e);
            }

            // Remove duplicated configuration URL's using set. Some
            // classloaders may return identical URL's twice, causing duplicate startups
            Set<URL> configUrls = new HashSet<URL>();
            while (resources.hasMoreElements()) {
                configUrls.add(resources.nextElement());
            }
            for (Iterator<URL> iterator = configUrls.iterator(); iterator.hasNext();) {
                URL resource = iterator.next();
                log.info("Initializing dmn engine using configuration '{}'", resource.toString());
                initDmnEngineFromResource(resource);
            }

            /*try {
                resources = classLoader.getResources("activiti-dmn-context.xml");
            } catch (IOException e) {
                throw new ActivitiDmnException("problem retrieving activiti-dmn-context.xml resources on the classpath: "
                        + System.getProperty("java.class.path"), e);
            }
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                log.info("Initializing dmn engine using Spring configuration '{}'", resource.toString());
                initDmnEngineFromSpringResource(resource);
            }*/

            setInitialized(true);
        } else {
            log.info("DMN engines already initialized");
        }
    }

    /**
     * Registers the given dmn engine. No {@link DmnEngineInfo} will be
     * available for this dmn engine. An engine that is registered will be
     * closed when the {@link DmnEngines#destroy()} is called.
     */
    public static void registerDmnEngine(DmnEngine dmnEngine) {
        dmnEngines.put(dmnEngine.getName(), dmnEngine);
    }

    /**
     * Unregisters the given dmn engine.
     */
    public static void unregister(DmnEngine dmnEngine) {
        dmnEngines.remove(dmnEngine.getName());
    }

    private static DmnEngineInfo initDmnEngineFromResource(URL resourceUrl) {
        DmnEngineInfo dmnEngineInfo = dmnEngineInfosByResourceUrl.get(resourceUrl.toString());
        // if there is an existing dmn engine info
        if (dmnEngineInfo != null) {
            // remove that dmn engine from the member fields
            dmnEngineInfos.remove(dmnEngineInfo);
            if (dmnEngineInfo.getException() == null) {
                String dmnEngineName = dmnEngineInfo.getName();
                dmnEngines.remove(dmnEngineName);
                dmnEngineInfosByName.remove(dmnEngineName);
            }
            dmnEngineInfosByResourceUrl.remove(dmnEngineInfo.getResourceUrl());
        }

        String resourceUrlString = resourceUrl.toString();
        try {
            log.info("initializing dmn engine for resource {}", resourceUrl);
            DmnEngine dmnEngine = buildDmnEngine(resourceUrl);
            String dmnEngineName = dmnEngine.getName();
            log.info("initialised dmn engine {}", dmnEngineName);
            dmnEngineInfo = new DmnEngineInfo(dmnEngineName, resourceUrlString, null);
            dmnEngines.put(dmnEngineName, dmnEngine);
            dmnEngineInfosByName.put(dmnEngineName, dmnEngineInfo);
        } catch (Throwable e) {
            log.error("Exception while initializing dmn engine: {}", e.getMessage(), e);
            dmnEngineInfo = new DmnEngineInfo(null, resourceUrlString, getExceptionString(e));
        }
        dmnEngineInfosByResourceUrl.put(resourceUrlString, dmnEngineInfo);
        dmnEngineInfos.add(dmnEngineInfo);
        return dmnEngineInfo;
    }

    private static String getExceptionString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    protected static DmnEngine buildDmnEngine(URL resource) {
        InputStream inputStream = null;
        try {
            inputStream = resource.openStream();
            DmnEngineConfiguration dmnEngineConfiguration = DmnEngineConfiguration.createDmnEngineConfigurationFromInputStream(inputStream);
            return dmnEngineConfiguration.buildDmnEngine();

        } catch (IOException e) {
            throw new ActivitiDmnException("couldn't open resource stream: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /** Get initialization results. */
    public static List<DmnEngineInfo> getDmnEngineInfos() {
        return dmnEngineInfos;
    }

    /**
     * Get initialization results. Only info will we available for dmn
     * engines which were added in the {@link DmnEngines#init()}. No
     * {@link DmnEngineInfo} is available for engines which were registered
     * programmatically.
     */
    public static DmnEngineInfo getDmnEngineInfo(String dmnEngineName) {
        return dmnEngineInfosByName.get(dmnEngineName);
    }

    public static DmnEngine getDefaultDmnEngine() {
        return getDmnEngine(NAME_DEFAULT);
    }

    /**
     * obtain a dmn engine by name.
     * 
     * @param dmnEngineName is the name of the dmn engine or null for the default dmn engine.
     */
    public static DmnEngine getDmnEngine(String dmnEngineName) {
        if (!isInitialized()) {
            init();
        }
        return dmnEngines.get(dmnEngineName);
    }

    /**
     * retries to initialize a dmn engine that previously failed.
     */
    public static DmnEngineInfo retry(String resourceUrl) {
        log.debug("retying initializing of resource {}", resourceUrl);
        try {
            return initDmnEngineFromResource(new URL(resourceUrl));
        } catch (MalformedURLException e) {
            throw new ActivitiDmnException("invalid url: " + resourceUrl, e);
        }
    }

    /**
     * provides access to dmn engine to application clients in a managed server environment.
     */
    public static Map<String, DmnEngine> getDmnEngines() {
        return dmnEngines;
    }

    /**
     * closes all dmn engines. This method should be called when the server shuts down.
     */
    public synchronized static void destroy() {
        if (isInitialized()) {
            Map<String, DmnEngine> engines = new HashMap<String, DmnEngine>(dmnEngines);
            dmnEngines = new HashMap<String, DmnEngine>();

            for (String dmnEngineName : engines.keySet()) {
                DmnEngine dmnEngine = engines.get(dmnEngineName);
                try {
                    dmnEngine.close();
                } catch (Exception e) {
                    log.error("exception while closing {}", (dmnEngineName == null ? "the default dmn engine" : "dmn engine " + dmnEngineName), e);
                }
            }

            dmnEngineInfosByName.clear();
            dmnEngineInfosByResourceUrl.clear();
            dmnEngineInfos.clear();

            setInitialized(false);
        }
    }

    public static boolean isInitialized() {
        return isInitialized;
    }

    public static void setInitialized(boolean isInitialized) {
        DmnEngines.isInitialized = isInitialized;
    }
}
