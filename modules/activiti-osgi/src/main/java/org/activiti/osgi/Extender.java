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
package org.activiti.osgi;

import static org.activiti.osgi.Constants.BUNDLE_ACTIVITI_HEADER;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.osgi.HeaderParser.PathElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="gnodet@gmail.com">Guillaume Nodet</a>
 */
public class Extender implements BundleTrackerCustomizer, ServiceTrackerCustomizer {

  private static final Logger LOGGER = LoggerFactory.getLogger(Extender.class);
  private static final String META_INF_SERVICES_DIR = "META-INF/services";
  private static final String SCRIPT_ENGINE_SERVICE_FILE = "javax.script.ScriptEngineFactory";

  private static BundleContext context;
  private final BundleTracker bundleTracker;
  private final ServiceTracker engineServiceTracker;
  private long timeout = 5000;
  private Map<Long, List<BundleScriptEngineResolver>> resolvers = new ConcurrentHashMap<Long, List<BundleScriptEngineResolver>>();

  public Extender(BundleContext context) {
    Extender.context = context;
    this.engineServiceTracker = new ServiceTracker(context, ProcessEngine.class.getName(), this);
    this.bundleTracker = new BundleTracker(context, Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE, this);
  }
  
  public static BundleContext getBundleContext() {
    return context;
  }

  public void open() {
    engineServiceTracker.open();
  }

  public void close() {
    engineServiceTracker.close();
  }

  public Object addingService(ServiceReference reference) {
    new Thread() {
      public void run() {
          bundleTracker.open();
      }
    }.start();
    return context.getService(reference);
  }

  public void modifiedService(ServiceReference reference, Object service) {
  }

  public void removedService(ServiceReference reference, Object service) {
    context.ungetService(reference);
    if (engineServiceTracker.size() == 0) {
      bundleTracker.close();
    }
  }

  public Object addingBundle(Bundle bundle, BundleEvent event) {
    if (event == null) {
      // existing bundles first added to the tracker with no event change
      checkInitialBundle(bundle);
    } else {
      bundleChanged(event);
    }

    checkBundleScriptEngines(bundle);
    
    return bundle;
  }

  public void modifiedBundle(Bundle bundle, BundleEvent event, Object arg2) {
    if (event == null) {
      // cannot think of why we would be interested in a modified bundle with no bundle event
      return;
    }
    bundleChanged(event);

  }

  // don't think we would be interested in removedBundle, as that is
  // called when bundle is removed from the tracker
  public void removedBundle(Bundle bundle, BundleEvent event, Object arg2) {
    List<BundleScriptEngineResolver> r = resolvers.remove(bundle.getBundleId());
    if (r != null) {
      for (BundleScriptEngineResolver service : r) {
        service.unregister();
      }
    }
  }



  /**
   * this method checks the initial bundle that are installed/active before
   * bundle tracker is opened.
   *
   * @param b the bundle to check
   */
  private void checkInitialBundle(Bundle b) {
    // If the bundle is active, check it
    if (b.getState() == Bundle.RESOLVED || b.getState() == Bundle.STARTING
            || b.getState() == Bundle.ACTIVE) {
      checkBundle(b);
    }
  }

  public void bundleChanged(BundleEvent event) {
    Bundle bundle = event.getBundle();
    if (event.getType() == BundleEvent.RESOLVED) {
      checkBundle(bundle);
    } else if (event.getType() == BundleEvent.STARTED) {
      checkBundleScriptEngines(bundle);
    }
  }
  
  private void checkBundleScriptEngines(Bundle bundle) {
    List<BundleScriptEngineResolver> r = new ArrayList<BundleScriptEngineResolver>();
    registerScriptEngines(bundle, r);
    for (BundleScriptEngineResolver service : r) {
      service.register();
    }
    resolvers.put(bundle.getBundleId(), r);
  }

  private void checkBundle(Bundle bundle) {
    LOGGER.debug("Scanning bundle {} for activiti process", bundle.getSymbolicName());
    try {
      List<URL> pathList = new ArrayList<URL>();
      String activitiHeader = (String) bundle.getHeaders().get(BUNDLE_ACTIVITI_HEADER);
      if (activitiHeader == null) {
        activitiHeader = "OSGI-INF/activiti/";
      }
      List<PathElement> paths = HeaderParser.parseHeader(activitiHeader);
      for (PathElement path : paths) {
        String name = path.getName();
        if (name.endsWith("/")) {
          addEntries(bundle, name, "*.*", pathList);
        } else {
          String baseName;
          String filePattern;
          int pos = name.lastIndexOf('/');
          if (pos < 0) {
            baseName = "/";
            filePattern = name;
          } else {
            baseName = name.substring(0, pos + 1);
            filePattern = name.substring(pos + 1);
          }
          if (hasWildcards(filePattern)) {
            addEntries(bundle, baseName, filePattern, pathList);
          } else {
            addEntry(bundle, name, pathList);
          }
        }
      }

      if (!pathList.isEmpty()) {
        LOGGER.debug("Found activiti process in bundle {} with paths: {}", bundle.getSymbolicName(),  pathList);

        ProcessEngine engine = (ProcessEngine) engineServiceTracker.waitForService(timeout);
        if (engine == null) {
          throw new IllegalStateException("Unable to find a ProcessEngine service");
        }

        RepositoryService service = engine.getRepositoryService();
        DeploymentBuilder builder = service.createDeployment();
        builder.name(bundle.getSymbolicName());
        for (URL url : pathList) {
          InputStream is = url.openStream();
          if (is == null) {
              throw new IOException("Error opening url: " + url);
          }
          try {
              builder.addInputStream(getPath(url), is);
          } finally {
              is.close();
          }
        }
        builder.enableDuplicateFiltering();
        builder.deploy();
      } else {
        LOGGER.debug("No activiti process found in bundle {}", bundle.getSymbolicName());
      }
    } catch (Throwable t) {
      LOGGER.error("Unable to deploy activiti bundle", t);
    }
  }

  private void addEntry(Bundle bundle, String path, List<URL> pathList) {
    URL override = getOverrideURL(bundle, path);
    if(override == null) {
      URL url = bundle.getEntry(path);
      pathList.add(url);
    } else {
      pathList.add(override);
    }
  }

  @SuppressWarnings({ "rawtypes" })
  private void addEntries(Bundle bundle, String path, String filePattern, List<URL> pathList) {
    Enumeration e = bundle.findEntries(path, filePattern, false);
    while (e != null && e.hasMoreElements()) {
      URL u = (URL) e.nextElement();
      URL override = getOverrideURL(bundle, u, path);
      if(override == null) {
          pathList.add(u);
      } else {
          pathList.add(override);
      }
    }
  }

  private boolean hasWildcards(String path) {
      return path.indexOf("*") >= 0;
  }

  private String getFilePart(URL url) {
      String path = url.getPath();
      int index = path.lastIndexOf('/');
      return path.substring(index + 1);
  }

  private String cachePath(Bundle bundle, String filePath) {
    return Integer.toHexString(bundle.hashCode()) + "/" + filePath;
  }

  private URL getOverrideURLForCachePath(String privatePath){
    URL override = null;
    File privateDataVersion = context.getDataFile(privatePath);
    if (privateDataVersion != null
            && privateDataVersion.exists()) {
      try {
          override = privateDataVersion.toURI().toURL();
      } catch (MalformedURLException e) {
          LOGGER.error("Unexpected URL Conversion Issue", e);
      }
    }
    return override;
  }

  private URL getOverrideURL(Bundle bundle, String path){
      String cachePath = cachePath(bundle, path);
      return getOverrideURLForCachePath(cachePath);
  }

  private URL getOverrideURL(Bundle bundle, URL path, String basePath){
      String cachePath = cachePath(bundle, basePath + getFilePart(path));
      return getOverrideURLForCachePath(cachePath);
  }

  //remove bundle protocol specific part, so that resource can be accessed by path relative to bundle root
  private static String getPath(URL url) {
      String path = url.toExternalForm();
      return path.replaceAll(url.getProtocol() + "://[^/]*/", "");
  }

  // script engine part
  
  public static ScriptEngine resolveScriptEngine(String scriptEngineName) throws InvalidSyntaxException {
    ServiceReference[] refs = context.getServiceReferences(ScriptEngineResolver.class.getName(), null);
    if (refs == null) {
      LOGGER.info("No OSGi script engine resolvers available!");
      return null;
    }
    
    LOGGER.debug("Found {} OSGi ScriptEngineResolver services", refs.length);
    
    for (ServiceReference ref : refs) {
      ScriptEngineResolver resolver = (ScriptEngineResolver) context.getService(ref);
      ScriptEngine engine = resolver.resolveScriptEngine(scriptEngineName);
      context.ungetService(ref);
      LOGGER.debug("OSGi resolver {} produced {} engine {}", resolver, scriptEngineName, engine);
      if (engine != null) {
        return engine;
      }
    }
    return null;
  }

  @SuppressWarnings({ "rawtypes" })
  protected void registerScriptEngines(Bundle bundle, List<BundleScriptEngineResolver> resolvers) {
    URL configURL = null;
    for (Enumeration e = bundle.findEntries(META_INF_SERVICES_DIR, SCRIPT_ENGINE_SERVICE_FILE, false); e != null && e.hasMoreElements();) {
      configURL = (URL) e.nextElement();
    }
    if (configURL != null) {
      LOGGER.info("Found ScriptEngineFactory in {}",bundle.getSymbolicName());
      resolvers.add(new BundleScriptEngineResolver(bundle, configURL));
    }
  } 

  public static interface ScriptEngineResolver {
    ScriptEngine resolveScriptEngine(String name);
  }

  protected static class BundleScriptEngineResolver implements ScriptEngineResolver {
    protected final Bundle bundle;
    private ServiceRegistration reg;
    private final URL configFile;

    public BundleScriptEngineResolver(Bundle bundle, URL configFile) {
      this.bundle = bundle;
      this.configFile = configFile;
    }
    public void register() {
      if(bundle.getBundleContext() != null) {
        reg = bundle.getBundleContext().registerService(ScriptEngineResolver.class.getName(), 
                                                        this, null);
      }
    }
    public void unregister() {
      if(reg != null) {
        reg.unregister();
      }
    }
    
    @SuppressWarnings({ "rawtypes" })
    public ScriptEngine resolveScriptEngine(String name) {
      try {
        BufferedReader in = new BufferedReader(new InputStreamReader(configFile.openStream()));
        String className = in.readLine();
        in.close();
        Class cls = bundle.loadClass(className);
        if (!ScriptEngineFactory.class.isAssignableFrom(cls)) {
            throw new IllegalStateException("Invalid ScriptEngineFactory: " + cls.getName());
        }
        ScriptEngineFactory factory = (ScriptEngineFactory) cls.newInstance();
        List<String> names = factory.getNames();
        for (String test : names) {
          if (test.equals(name)) {
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            ScriptEngine engine;
            try {
              // JRuby seems to require the correct TCCL to call getScriptEngine
              Thread.currentThread().setContextClassLoader(factory.getClass().getClassLoader());
              engine = factory.getScriptEngine();
            } finally {
              Thread.currentThread().setContextClassLoader(old);
            }
            LOGGER.trace("Resolved ScriptEngineFactory: {} for expected name: {}", engine, name);
            return engine;
          }
        }
        LOGGER.debug("ScriptEngineFactory: {} does not match expected name: {}", factory.getEngineName(), name);
        return null;
      } catch (Exception e) {
        LOGGER.warn("Cannot create ScriptEngineFactory: {}", e.getClass().getName(), e);
        return null;
      }
    }

    @Override
    public String toString() {
      return "OSGi script engine resolver for " + bundle.getSymbolicName();
    }
  }

}
