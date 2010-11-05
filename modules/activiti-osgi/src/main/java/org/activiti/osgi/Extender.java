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

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.osgi.framework.*;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.activiti.osgi.HeaderParser.PathElement;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.activiti.osgi.Constants.*;
import static org.osgi.framework.Constants.*;

/**
 * @author <a href="gnodet@gmail.com">Guillaume Nodet</a>
 */
public class Extender implements BundleTrackerCustomizer, ServiceTrackerCustomizer {

    private static final Logger LOGGER = Logger.getLogger(Extender.class.getName());

    private final BundleContext context;
    private final BundleTracker bundleTracker;
    private final ServiceTracker engineServiceTracker;
    private long timeout = 5000;

    public Extender(BundleContext context) {
        this.context = context;
        this.engineServiceTracker = new ServiceTracker(context, ProcessEngine.class.getName(), this);
        this.bundleTracker = new BundleTracker(context, Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE, this);
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
    public void removedBundle(Bundle b, BundleEvent event, Object arg2) {
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
        }
    }

    private void checkBundle(Bundle bundle) {
        LOGGER.log(Level.FINE, "Scanning bundle {} for activiti process", bundle.getSymbolicName());
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
                LOGGER.log(Level.FINE, "Found activiti process in bundle " + bundle.getSymbolicName()
                        + " with paths: " +  pathList);

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
                        builder.addInputStream(url.toExternalForm(), is);
                    } finally {
                        is.close();
                    }
                }
                builder.enableDuplicateFiltering();
                builder.deploy();
            } else {
                LOGGER.log(Level.FINE, "No activiti process found in bundle {}", bundle.getSymbolicName());
            }
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "Unable to deploy activiti bundle", t);
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

    private String cachePath(Bundle bundle, String filePath)
    {
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
                LOGGER.log(Level.SEVERE, "Unexpected URL Conversion Issue", e);
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

}
