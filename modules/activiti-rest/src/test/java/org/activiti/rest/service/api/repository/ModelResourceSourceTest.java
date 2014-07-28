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

package org.activiti.rest.service.api.repository;

import java.io.ByteArrayInputStream;

import org.activiti.engine.repository.Model;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.HttpMultipartRepresentation;
import org.activiti.rest.service.api.RestUrls;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;


/**
 * @author Frederik Heremans
 */
public class ModelResourceSourceTest extends BaseRestTestCase {

  public void testGetModelEditorSource() throws Exception {
    
    Model model = null;
    try {
      
      model = repositoryService.newModel();
      model.setName("Model name");
      repositoryService.saveModel(model);
      
      repositoryService.addModelEditorSource(model.getId(), "This is the editor source".getBytes());
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_MODEL_SOURCE, model.getId()));
      Representation response = client.get();
      
      // Check "OK" status
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getMediaType());
      assertEquals("This is the editor source", response.getText());
      
    } finally
    {
      try {
        repositoryService.deleteModel(model.getId());
      } catch(Throwable ignore) {
        // Ignore, model might not be created
      }
    }
  }
  
  public void testGetModelEditorSourceNoSource() throws Exception {
    Model model = null;
    try {
      
      model = repositoryService.newModel();
      model.setName("Model name");
      repositoryService.saveModel(model);
      
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_MODEL_SOURCE, model.getId()));
      
      try {
        client.get();
        fail("404 expected, but was: " + client.getResponse().getStatus());
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
        assertEquals("Model with id '" + model.getId() + "' does not have source available.", client.getResponse().getStatus().getDescription());
      }
      
    } finally
    {
      try {
        repositoryService.deleteModel(model.getId());
      } catch(Throwable ignore) {
        // Ignore, model might not be created
      }
    }
  }
  
  public void testGetModelEditorSourceExtra() throws Exception {
    
    Model model = null;
    try {
      
      model = repositoryService.newModel();
      model.setName("Model name");
      repositoryService.saveModel(model);
      
      repositoryService.addModelEditorSourceExtra(model.getId(), "This is the extra editor source".getBytes());
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_MODEL_SOURCE_EXTRA, model.getId()));
      Representation response = client.get();
      
      // Check "OK" status
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getMediaType());
      assertEquals("This is the extra editor source", response.getText());
      
    } finally
    {
      try {
        repositoryService.deleteModel(model.getId());
      } catch(Throwable ignore) {
        // Ignore, model might not be created
      }
    }
  }
  
  public void testGetModelEditorSourceExtraNoSource() throws Exception {
    Model model = null;
    try {
      
      model = repositoryService.newModel();
      model.setName("Model name");
      repositoryService.saveModel(model);
      
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_MODEL_SOURCE_EXTRA, model.getId()));
      
      try {
        client.get();
        fail("404 expected, but was: " + client.getResponse().getStatus());
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
        assertEquals("Model with id '" + model.getId() + "' does not have extra source available.", client.getResponse().getStatus().getDescription());
      }
      
    } finally
    {
      try {
        repositoryService.deleteModel(model.getId());
      } catch(Throwable ignore) {
        // Ignore, model might not be created
      }
    }
  }
 
  
  public void testGetModelSourceUnexistingModel() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_SOURCE, "unexisting"));
    try {
      client.get();
      fail("404 expected, but was: " + client.getResponse().getStatus());
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
      assertEquals("Could not find a model with id 'unexisting'.", client.getResponse().getStatus().getDescription());
    }
  }
  
  public void testGetModelSourceExtraUnexistingModel() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_SOURCE_EXTRA, "unexisting"));
    try {
      client.get();
      fail("404 expected, but was: " + client.getResponse().getStatus());
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
      assertEquals("Could not find a model with id 'unexisting'.", client.getResponse().getStatus().getDescription());
    }
  }
  
  public void testSetModelEditorSource() throws Exception {
    
    Model model = null;
    try {
      
      model = repositoryService.newModel();
      model.setName("Model name");
      repositoryService.saveModel(model);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_MODEL_SOURCE, model.getId()));
      Representation response = client.put(new HttpMultipartRepresentation("sourcefile", new ByteArrayInputStream("This is the new editor source".getBytes())));
      
      // Check "OK" status
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getMediaType());
      assertEquals("This is the new editor source", response.getText());
      
      assertEquals("This is the new editor source", new String(repositoryService.getModelEditorSource(model.getId())));

      
    } finally
    {
      try {
        repositoryService.deleteModel(model.getId());
      } catch(Throwable ignore) {
        // Ignore, model might not be created
      }
    }
  }
  
  public void testSetModelEditorSourceExtra() throws Exception {
    
    Model model = null;
    try {
      
      model = repositoryService.newModel();
      model.setName("Model name");
      repositoryService.saveModel(model);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_MODEL_SOURCE_EXTRA, model.getId()));
      Representation response = client.put(new HttpMultipartRepresentation("sourcefile", new ByteArrayInputStream("This is the new extra editor source".getBytes())));
      
      // Check "OK" status
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getMediaType());
      assertEquals("This is the new extra editor source", response.getText());
      
      assertEquals("This is the new extra editor source", new String(repositoryService.getModelEditorSourceExtra(model.getId())));

      
    } finally
    {
      try {
        repositoryService.deleteModel(model.getId());
      } catch(Throwable ignore) {
        // Ignore, model might not be created
      }
    }
  }
  
  public void testSetModelSourceUnexistingModel() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_SOURCE, "unexisting"));
    try {
      client.put("");
      fail("404 expected, but was: " + client.getResponse().getStatus());
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
      assertEquals("Could not find a model with id 'unexisting'.", client.getResponse().getStatus().getDescription());
    }
  }
  
  public void testSetModelSourceExtraUnexistingModel() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_SOURCE_EXTRA, "unexisting"));
    try {
      client.put("");
      fail("404 expected, but was: " + client.getResponse().getStatus());
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
      assertEquals("Could not find a model with id 'unexisting'.", client.getResponse().getStatus().getDescription());
    }
  }
}
