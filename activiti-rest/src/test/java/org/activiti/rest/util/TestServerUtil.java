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
package org.activiti.rest.util;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.activiti.rest.WebConfigurer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * @author Joram Barrez
 */
public class TestServerUtil {
	
	private static final Logger log = LoggerFactory.getLogger(TestServerUtil.class);
	
	protected static final int START_PORT = 9797;
	private static AtomicInteger NEXT_PORT = new AtomicInteger(9797);
	
  public static TestServer createAndStartServer(Class<?> ... configClasses) {
  	int port= NEXT_PORT.incrementAndGet();
    Server server = new Server(port);

    HashSessionIdManager idmanager = new HashSessionIdManager();
    server.setSessionIdManager(idmanager);
    
    AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
    applicationContext.register(configClasses);
    applicationContext.refresh();
      
    try {
      server.setHandler(getServletContextHandler(applicationContext));
      server.start();
    } catch (Exception e) {
      log.error("Error starting server", e);
    }
    
    return new TestServer(server, applicationContext, port);
  }
  
  private static ServletContextHandler getServletContextHandler(AnnotationConfigWebApplicationContext context) throws IOException {
    ServletContextHandler contextHandler = new ServletContextHandler();
    WebConfigurer configurer = new WebConfigurer();
    configurer.setContext(context);
    contextHandler.addEventListener(configurer);
    
    // Create the SessionHandler (wrapper) to handle the sessions
    HashSessionManager manager = new HashSessionManager();
    SessionHandler sessions = new SessionHandler(manager);
    contextHandler.setHandler(sessions);
    
    return contextHandler;
  }
  
  
  public static class TestServer {
  	
  	private Server server;
  	private ApplicationContext applicationContext;
  	private int port;
  	
  	public TestServer(Server server, ApplicationContext applicationContext, int port) {
  		this.server = server;
  		this.applicationContext = applicationContext;
  		this.port = port;
  	}

		public Server getServer() {
			return server;
		}

		public void setServer(Server server) {
			this.server = server;
		}

		public ApplicationContext getApplicationContext() {
			return applicationContext;
		}

		public void setApplicationContext(ApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}
		
		public String getServerUrlPrefix() {
			return "http://localhost:" + getPort() + "/service/";
		}
  	
  }

}
