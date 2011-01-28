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
package org.activiti.rest.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.extensions.webscripts.ClassPathStoreResourceResolver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author <a href="gnodet@gmail.com">Guillaume Nodet</a>
 */
public class OsgiClassPathStoreResourceResolver extends ClassPathStoreResourceResolver {

    // Logger
    private static final Log logger = LogFactory.getLog(OsgiClassPathStoreResourceResolver.class);

    public OsgiClassPathStoreResourceResolver(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    protected Resource resolveRootDirResource(Resource original) throws IOException {
        Resource resolved = super.resolveRootDirResource(original);
        if (resolved == original) {
            // Equinox/Felix specific hack
            try {
                URL url = original.getURL();
                URLConnection con = url.openConnection();
                try {
                    Method mth = con.getClass().getMethod("getLocalURL");
                    mth.setAccessible(true);
                    URL localUrl = (URL) mth.invoke(con);
                    if (localUrl != null) {
                        return new UrlResource(localUrl);
                    }
                } catch (NoSuchMethodException t) {
                    Object targetModule = getField(con, "m_targetModule");
                    int classPathIdx = (Integer) getField(con, "m_classPathIdx");
                    Object content;
                    if (classPathIdx == 0) {
                        content = getField(targetModule, "m_content");
                    } else {
                        Object[] contentPath = (Object[]) getField(targetModule, "m_contentPath");
                        content = contentPath[classPathIdx - 1];
                    }
                    URL localUrl = getContentUrl(content, url.getPath());
                    return new UrlResource(localUrl);
                }
            } catch (Throwable t) {
                logger.debug("Could not resolve url");
            }
        }
        return original;
    }

    private URL getContentUrl(Object content, String path) throws Exception {
        if (content.getClass().getName().endsWith(".JarContent")) {
            File file = (File) getField(content, "m_file");
            URL localUrl = new URL("jar:" + file.toURI().toString() + "!" + path);
            return localUrl;
        } else if (content.getClass().getName().endsWith(".ContentDirectoryContent")) {
            Object c = getField(content, "m_content");
            String p = (String) getField(content, "m_rootPath");
            if (!p.startsWith("/")) {
                p = "/" + p;
            }
            if (p.endsWith("/") && path.startsWith("/")) {
                p = p + path.substring(1);
            } else if (!p.endsWith("/") && !path.endsWith("/")) {
                p = p + "/" + path;
            } else {
                p = p + path;
            }
            URL localUrl = getContentUrl(c, p);
            return localUrl;
        }
        throw new IllegalStateException();
    }

    private static Object getField(Object source, String name) throws NoSuchFieldException, IllegalAccessException {
        Field f = source.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(source);
    }

}