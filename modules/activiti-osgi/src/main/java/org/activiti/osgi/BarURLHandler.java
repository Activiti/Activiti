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

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="gnodet@gmail.com">Guillaume Nodet</a>
 */
public class BarURLHandler extends AbstractURLStreamHandlerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BarURLHandler.class);

    private static String SYNTAX = "bar: bar-xml-uri";

    private URL barXmlURL;

    /**
     * Open the connection for the given URL.
     *
     * @param url the url from which to open a connection.
     * @return a connection on the specified URL.
     * @throws IOException if an error occurs or if the URL is malformed.
     */
    @Override
    public URLConnection openConnection(URL url) throws IOException {
        if (url.getPath() == null || url.getPath().trim().length() == 0) {
            throw new MalformedURLException("Path can not be null or empty. Syntax: " + SYNTAX );
        }
        barXmlURL = new URL(url.getPath());

        LOGGER.debug("bar xml URL is: [{}]", barXmlURL);
        return new Connection(url);
    }

    public URL getBarXmlURL() {
        return barXmlURL;
    }

    public class Connection extends URLConnection {

        public Connection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            final PipedInputStream pin = new PipedInputStream();
            final PipedOutputStream pout = new PipedOutputStream( pin );
            new Thread() {
                public void run() {
                    try {
                        BarTransformer.transform(barXmlURL, pout);
                    }
                    catch( Exception e ) {
                        LOGGER.warn("Bundle cannot be generated" );
                    }
                    finally {
                        try {
                            pout.close();
                        }
                        catch( IOException ignore ) {
                            // if we get here something is very wrong
                            LOGGER.error("Bundle cannot be generated", ignore );
                        }
                    }
                }
            }.start();
            return pin;
        }
    }

}
