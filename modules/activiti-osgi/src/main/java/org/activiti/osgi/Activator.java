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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.felix.fileinstall.ArtifactListener;
import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLStreamHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi Activator
 * @author <a href="gnodet@gmail.com">Guillaume Nodet</a>
 */
public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private List<Runnable> callbacks = new ArrayList<Runnable>();

    public void start(BundleContext context) throws Exception {
        callbacks.add(new Service(
                context,
                URLStreamHandlerService.class.getName(),
                new BpmnURLHandler(),
                props("url.handler.protocol", "bpmn")));
        callbacks.add(new Service(
                context,
                URLStreamHandlerService.class.getName(),
                new BarURLHandler(),
                props("url.handler.protocol", "bar")));
        try {
            callbacks.add(new Service(
                    context,
                    new String[] { ArtifactUrlTransformer.class.getName(), ArtifactListener.class.getName() },
                    new BpmnDeploymentListener(),
                    null));
            callbacks.add(new Service(
                    context,
                    new String[] { ArtifactUrlTransformer.class.getName(), ArtifactListener.class.getName() },
                    new BarDeploymentListener(),
                    null));
        } catch (NoClassDefFoundError e) {
            LOGGER.warn("FileInstall package is not available, disabling fileinstall support" );
            LOGGER.debug("FileInstall package is not available, disabling fileinstall support", e );
        }
        callbacks.add(new Tracker(new Extender(context)));
    }

    public void stop(BundleContext context) throws Exception {
        for (Runnable r : callbacks) {
            r.run();
        }
    }

    private static Dictionary<String,String> props(String... args) {
        Dictionary<String, String> props = new Hashtable<String, String>();
        for (int i = 0; i < args.length / 2; i++) {
            props.put(args[2*i], args[2*i+1]);
        }
        return props;
    }

    @SuppressWarnings({ "rawtypes" })
    private static class Service implements Runnable {

        private final ServiceRegistration registration;

        public Service(BundleContext context, String clazz, Object service, Dictionary props) {
            this.registration = context.registerService(clazz, service, props);
        }

        public Service(BundleContext context, String[] clazz, Object service, Dictionary props) {
            this.registration = context.registerService(clazz, service, props);
        }

        public void run() {
            registration.unregister();
        }
    }

    private static class Tracker implements Runnable {

        private final Extender extender;

        private Tracker(Extender extender) {
            this.extender = extender;
            this.extender.open();
        }

        public void run() {
            extender.close();
        }
    }

}
