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

import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.BUNDLE_VERSION;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="gnodet@gmail.com">Guillaume Nodet</a>
 */
public class BarDeploymentListener implements ArtifactUrlTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BarDeploymentListener.class);

    public URL transform(URL artifact) throws Exception {
        try {
            return new URL("bar", null, artifact.toString());
        } catch (Exception e) {
            LOGGER.error("Unable to build bar bundle", e);
            return null;
        }
    }

    public boolean canHandle(File artifact) {
        JarFile jar = null;
    	try {
            if (!artifact.getName().endsWith(".bar")) {
                return false;
            }
            jar = new JarFile(artifact);
            // Only handle non OSGi bundles
            Manifest m = jar.getManifest();
            if (m!= null && m.getMainAttributes().getValue(
                    new Attributes.Name(BUNDLE_SYMBOLICNAME)) != null
                    && m.getMainAttributes().getValue(
                            new Attributes.Name(BUNDLE_VERSION)) != null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
        	if(jar != null) {
        		try {
					jar.close();
				} catch (IOException e) {
					LOGGER.error("Unable to close jar", e);
				}
        	}
        }
    }

}
