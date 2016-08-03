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
package org.activiti.rest.service.api.runtime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.rest.conf.ObjectVariableSerializationDisabledApplicationConfiguration;
import org.activiti.rest.service.HttpMultipartHelper;
import org.activiti.rest.service.api.RestUrls;
import org.activiti.rest.util.TestServerUtil;
import org.activiti.rest.util.TestServerUtil.TestServer;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class SerializableVariablesDiabledTest {

  private RepositoryService repositoryService;
  private RuntimeService runtimeService;
  private TaskService taskService;
  private IdentityService identityService;

  private String serverUrlPrefix;

  private String testUserId;
  private String testGroupId;

  @Before
  public void setupServer() {
    if (serverUrlPrefix == null) {
      TestServer testServer = TestServerUtil.createAndStartServer(ObjectVariableSerializationDisabledApplicationConfiguration.class);
      serverUrlPrefix = testServer.getServerUrlPrefix();

      this.repositoryService = testServer.getApplicationContext().getBean(RepositoryService.class);
      this.runtimeService = testServer.getApplicationContext().getBean(RuntimeService.class);
      this.identityService = testServer.getApplicationContext().getBean(IdentityService.class);
      this.taskService = testServer.getApplicationContext().getBean(TaskService.class);

      User user = identityService.newUser("kermit");
      user.setFirstName("Kermit");
      user.setLastName("the Frog");
      user.setPassword("kermit");
      identityService.saveUser(user);

      Group group = identityService.newGroup("admin");
      group.setName("Administrators");
      identityService.saveGroup(group);

      identityService.createMembership(user.getId(), group.getId());

      this.testUserId = user.getId();
      this.testGroupId = group.getId();
    }
  }

  @After
  public void removeUsers() {
    identityService.deleteMembership(testUserId, testGroupId);
    identityService.deleteGroup(testGroupId);
    identityService.deleteUser(testUserId);

    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void testCreateSingleSerializableProcessVariable() throws Exception {
    repositoryService.createDeployment()
      .addClasspathResource("org/activiti/rest/service/api/runtime/ProcessInstanceVariablesCollectionResourceTest.testProcess.bpmn20.xml")
      .deploy();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    TestSerializableVariable serializable = new TestSerializableVariable();
    serializable.setSomeField("some value");

    // Serialize object to readable stream for representation
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    ObjectOutputStream output = new ObjectOutputStream(buffer);
    output.writeObject(serializable);
    output.close();

    InputStream binaryContent = new ByteArrayInputStream(buffer.toByteArray());

    // Add name, type and scope
    Map<String, String> additionalFields = new HashMap<String, String>();
    additionalFields.put("name", "serializableVariable");
    additionalFields.put("type", "serializable");

    // Upload a valid BPMN-file using multipart-data
    HttpPost httpPost = new HttpPost(serverUrlPrefix +
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION, processInstance.getId()));
    httpPost.setEntity(HttpMultipartHelper.getMultiPartEntity("value", "application/x-java-serialized-object", binaryContent, additionalFields));

    // We have serializeable object disabled, we should get a 415.
    assertResponseStatus(httpPost, HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
  }

  @Test
  public void testCreateSingleSerializableTaskVariable() throws Exception {
    repositoryService.createDeployment()
      .addClasspathResource("org/activiti/rest/service/api/runtime/ProcessInstanceVariablesCollectionResourceTest.testProcess.bpmn20.xml")
      .deploy();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    TestSerializableVariable serializable = new TestSerializableVariable();
    serializable.setSomeField("some value");

    // Serialize object to readable stream for representation
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    ObjectOutputStream output = new ObjectOutputStream(buffer);
    output.writeObject(serializable);
    output.close();

    InputStream binaryContent = new ByteArrayInputStream(buffer.toByteArray());

    // Add name, type and scope
    Map<String, String> additionalFields = new HashMap<String, String>();
    additionalFields.put("name", "serializableVariable");
    additionalFields.put("type", "serializable");

    HttpPost httpPost = new HttpPost(serverUrlPrefix +
            RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    httpPost.setEntity(HttpMultipartHelper.getMultiPartEntity("value", "application/x-java-serialized-object", binaryContent, additionalFields));

    // We have serializeable object disabled, we should get a 415.
    assertResponseStatus(httpPost, HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
  }

  public void assertResponseStatus(HttpUriRequest request, int expectedStatusCode) {
    CloseableHttpResponse response = null;
    try {

      CredentialsProvider provider = new BasicCredentialsProvider();
      UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("kermit", "kermit");
      provider.setCredentials(AuthScope.ANY, credentials);
      HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();

      response = (CloseableHttpResponse) client.execute(request);
      int statusCode = response.getStatusLine().getStatusCode();
      Assert.assertEquals(expectedStatusCode, statusCode);

      if (client instanceof CloseableHttpClient) {
        ((CloseableHttpClient) client).close();
      }

      response.close();
    } catch (ClientProtocolException e) {
      Assert.fail(e.getMessage());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }
}
