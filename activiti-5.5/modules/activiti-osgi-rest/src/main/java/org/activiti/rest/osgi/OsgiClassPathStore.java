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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.extensions.webscripts.ClassPathStore;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="gnodet@gmail.com">Guillaume Nodet</a>
 */
public class OsgiClassPathStore extends ClassPathStore {

    // Logger
    private static final Log logger = LogFactory.getLog(OsgiClassPathStore.class);

    /*
     * Unchanged, just because it calls a private method
     */
    public String[] getAllDocumentPaths()
    {
        String[] paths;

        try
        {
            List<String> documentPaths = matchDocumentPaths("/**/*");
            paths = documentPaths.toArray(new String[documentPaths.size()]);
        }
        catch (IOException e)
        {
            // Note: Ignore: no documents found
            paths = new String[0];
        }

        return paths;
    }

    /*
     * Unchanged, just because it calls a private method
     */
    public String[] getDocumentPaths(String path, boolean includeSubPaths, String documentPattern)
        throws IOException
    {
        if ((path == null) || (path.length() == 0))
        {
            path = "/";
        }

        if (!path.startsWith("/"))
        {
            path = "/" + path;
        }

        if (!path.endsWith("/"))
        {
            path = path + "/";
        }

        if ((documentPattern == null) || (documentPattern.length() == 0))
        {
            documentPattern = "*";
        }

        // classpath*:
        final StringBuilder pattern = new StringBuilder(128);
        pattern.append(path)
               .append((includeSubPaths ? "**/" : ""))
               .append(documentPattern);

        List<String> documentPaths = matchDocumentPaths(pattern.toString());
        return documentPaths.toArray(new String[documentPaths.size()]);
    }

    /*
     * Unchanged, just because it's private
     */
    private List<String> matchDocumentPaths(String pattern)
        throws IOException
    {
        Resource[] resources = getDocumentResources(pattern);
        List<String> documentPaths = new ArrayList<String>(resources.length);
        for (Resource resource : resources)
        {
            String documentPath = toDocumentPath(resource.getURL().toExternalForm());
            documentPaths.add(documentPath);
        }
        return documentPaths;
    }

    /*
     * Changed to accomodate inner paths in OSGi / WARs, for example resources in WEB-INF/classes/**
     */
    private String toDocumentPath(final String resourcePath)
    {
        String documentPath = null;

        // check if this is a valid url (either a java URL or a Spring classpath prefix URL)
        try
        {
            final URL url = ResourceUtils.getURL(resourcePath);

            String urlString = resourcePath;

            // if the URL is a JAR url, trim off the reference to the JAR
            if (isJarURL(url))
            {
                // find the URL to the jar file and split off the prefix portion that references the jar file
                String jarUrlString = extractJarFileURL(url).toExternalForm();

                final int x = urlString.indexOf(jarUrlString);
                if (x != -1)
                {
                    urlString = urlString.substring(x + jarUrlString.length());

                    // remove a prefix ! if it is found
                    if (urlString.charAt(0) == '!')
                    {
                        urlString = urlString.substring(1);
                    }

                    // remove a prefix / if it is found
                    if (urlString.charAt(0) == '/')
                    {
                        urlString = urlString.substring(1);
                    }
                }
            }

            // if the url string starts with the classpath: prefix, remove it
            if (urlString.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX))
            {
                urlString = urlString.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
            }

            // if the url string starts with the file: prefix, remove the storeDir path
            // this also remove the base path
            if (urlString.startsWith(ResourceUtils.FILE_URL_PREFIX))
            {
                if (storeDirs == null)
                {
                    throw new WebScriptException("Unable to resolve a file: resource without a storeDir.");
                }
                for (int i=0; i<this.storeDirs.length; i++)
                {
                    if (urlString.startsWith(this.storeDirs[i]))
                    {
                        urlString = urlString.substring(this.storeDirs[i].length());
                        break;
                    }
                }
            }
            // handle the JBoss app-server virtual filesystem prefix
            else if (urlString.startsWith(VFSFILE_URL_PREFIX))
            {
                if (storeDirs == null)
                {
                    throw new WebScriptException("Unable to resolve a vfsfile: resource without a storeDir.");
                }
                for (int i=0; i<this.storeDirs.length; i++)
                {
                    if (urlString.startsWith(this.storeDirs[i]))
                    {
                        urlString = urlString.substring(this.storeDirs[i].length() + 3); // to account for "vfs" prefix
                        break;
                    }
                }
            }
            else
            {
                // now remove the class path store base path
                if (classPath != null && classPath.length() != 0)
                {
                    // the url string should always contain the class path
                    int idx = urlString.indexOf(classPath);
                    if (idx >= 0)
                    {
                        urlString = urlString.substring(idx + classPath.length());
                    }

                    // remove extra / at the front if found
                    if (urlString.charAt(0) == '/')
                    {
                        urlString = urlString.substring(1);
                    }
                }
            }

            // what remains is the document path
            documentPath = urlString;
        }
        catch (FileNotFoundException fnfe)
        {
            if (logger.isWarnEnabled())
                logger.warn("Unable to determine document path for resource: " + resourcePath + " with base path " + classPath, fnfe);
        }
        catch (MalformedURLException mue)
        {
            if (logger.isWarnEnabled())
                logger.warn("Unable to determine document path for resource: " + resourcePath + " with base path " + classPath, mue);
        }

        return documentPath;
    }

    /*
     * Unchanged, just because it's private
     */
    private Resource[] getDocumentResources(String locationPattern)
        throws IOException
    {
        String resourcePath = toResourcePath(locationPattern);

        Resource[] resources = resolver.getResources("classpath*:" + resourcePath);
        ArrayList<Resource> list = new ArrayList<Resource>(resources.length);
        for (Resource resource : resources)
        {
            // only keep documents, not directories
            if (!resource.getURL().toExternalForm().endsWith("/"))
            {
                list.add(resource);
            }
        }

        return list.toArray(new Resource[list.size()]);
    }

    /*
     * Unchanged, just because it's private
     */
    private String toResourcePath(String documentPath)
    {
        return createPath(classPath, documentPath);
    }


    /**
     * The only change is to resolver instantiation
     */
    @Override
    public void init() {
        // wrap the application context resource resolver with our own
        this.resolver = new OsgiClassPathStoreResourceResolver(applicationContext);

        // check if there are any resources that live under this path
        // this is valid for read-only classpaths (class files + JAR file contents)
        try
        {
            Resource[] resources = resolver.getResources("classpath*:" + classPath + "/**/*");
            if (resources.length != 0)
            {
                exists = true;
            }
            else
            {
                resources = resolver.getResources("classpath*:" + classPath + "/*");
                if (resources.length != 0)
                {
                    exists = true;
                }
            }

            // NOTE: Locate root of web script store
            // NOTE: Following awkward approach is used to mirror lookup of web scripts within store.  This
            //       ensures root paths match.
            try
            {
                // Process each root resource - there may be several as the classpath* could match
                // multiple location that each contain the configured path.
                Resource rootResource = null;
                resources = applicationContext.getResources("classpath*:" + classPath + "*");
                List<String> storeDirList = new ArrayList<String>(resources.length);
                for (Resource resource : resources)
                {
                    String externalForm = resource.getURL().toExternalForm();
                    if (externalForm.endsWith(classPath) || externalForm.endsWith(classPath + "/"))
                    {
                        // we've found the right resource, let's now bind using string constructor
                        // so that Spring 3 will correctly create relative paths
                        String directoryPath = resource.getFile().getAbsolutePath();
                        if (resource.getFile().isDirectory() && !directoryPath.endsWith("/"))
                        {
                            directoryPath += "/";
                        }
                        if (new FileSystemResource(directoryPath).exists())
                        {
                            // retrieve file system directory
                            storeDirList.add(resource.getFile().toURI().toURL().toExternalForm());
                        }
                    }
                }
                this.storeDirs = storeDirList.toArray(new String[storeDirList.size()]);
            }
            catch (IOException ioErr)
            {
                // unable to resolve a storeDir - this is expected for certain protocols such as "vfszip"
                // it is not critical and those protocols don't require it during path resolution later
                if (logger.isDebugEnabled())
                    logger.debug("Unable to resolve storeDir for base path " + classPath);
            }
        }
        catch (IOException ioe)
        {
            throw new WebScriptException("Failed to initialise Web Script Store classpath: " + classPath, ioe);
        }

        if (!exists && mustExist)
        {
            throw new WebScriptException("Web Script Store classpath:" + classPath + " must exist; it was not found");
        }
    }

}