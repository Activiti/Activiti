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

package org.activiti.rest.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.activiti.engine.impl.test.PvmTestCase;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.persistence.Persistence;
import org.activiti.persistence.entity.Task;
import org.activiti.persistence.entity.User;
import org.activiti.rest.RestServlet;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Tom Baeyens
 */
public class RestTestCase extends PvmTestCase {
  
  private static Logger log = Logger.getLogger(RestTestCase.class.getName());
  
  static int serverPort = 8765;
  static String baseContextUrl = "http://localhost:"+serverPort+"/activiti";
  static Server server = null;

  protected String username = "kermit";
  protected String password = "kermit";
  protected List<String> createdUsers = new ArrayList<String>();
  protected List<String> createdTaskIds = new ArrayList<String>();
  
  public void setUp() throws Exception {
    super.setUp();
    
    if (server==null) {
      server = new Server(serverPort);
      ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/", true, false);
      servletContextHandler.addServlet(RestServlet.class, "/activiti/*");
      server.start();
      
      User defaultUser = new User()
        .setUserId(username)
        .setPassword(password);
      
      createUser(defaultUser);
    }
  }
  
  protected void tearDown() throws Exception {
    log.info("=== cleaning db at teardown =================================");
    Persistence.clean();
    
    super.tearDown();
  }

  protected void createUser(User user) {
    Persistence.getUsers().insertUser(user);
    createdUsers.add(username);
  }

  protected void deleteUser(String userId) {
    Persistence.getUsers().deleteUser(userId);
  }

  protected void createTask(Task task) {
    Persistence.getTasks().createTask(task);
    createdTaskIds.add(task.getId());
  }

  protected void deleteTask(String taskId) {
    Persistence.getTasks().deleteTask(taskId);
  }

  
  protected void setUser(String username, String password) {
    setUser(username, password, false);
  }
  
  protected void setUser(String username, String password, boolean create) {
    if (create) {
      User user = new User()
        .setUserId(username)
        .setPassword(password);
      createUser(user);
    }
    this.username = username;
    this.password = password;
  }
  
  protected String get(String contextPath) {
    HttpURLConnection urlConnection = null;
    try {
      String authHeaderValue = username+":"+password;
      String encodedAuthHeaderValue = new String(Base64.encodeBase64(authHeaderValue.getBytes()));
      urlConnection = (HttpURLConnection) new URL(baseContextUrl+contextPath).openConnection();
      urlConnection.addRequestProperty("Auth", "Basic "+encodedAuthHeaderValue);
      InputStream stream = urlConnection.getInputStream();
      return new String(IoUtil.readInputStream(stream, "test servlet response"));
    } catch (Exception e) {
      
      int responseCode = -1;
      String responseMessage = null;
      try {
        responseCode = urlConnection.getResponseCode();
        responseMessage = urlConnection.getResponseMessage();
      } catch (IOException e2) {
        e.printStackTrace();
      }
      
      throw new RuntimeException("exception getting "+contextPath+" ("+responseCode+") "+responseMessage, e);
    }
  }
}
