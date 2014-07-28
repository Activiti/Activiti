package org.activiti.rest.service.api.management;

import java.util.List;

import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Test for all REST-operations related to the Table columns.
 * 
 * @author Frederik Heremans
 */
public class TableDataResourceTest extends BaseRestTestCase {

  /**
   * Test getting a single table's row data. GET
   * management/tables/{tableName}/data
   */
  public void testGetTableColumns() throws Exception {

    try {
      
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskService.setVariable(task.getId(), "var1", 123);
      taskService.setVariable(task.getId(), "var2", 456);
      taskService.setVariable(task.getId(), "var3", 789);
      
      // We use variable-table as a reference
      String tableName = managementService.getTableName(VariableInstanceEntity.class);

      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_DATA, tableName));
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

      // Check paging result
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals(3, responseNode.get("total").intValue());
      assertEquals(3, responseNode.get("size").intValue());
      assertEquals(0, responseNode.get("start").intValue());
      assertTrue(responseNode.get("order").isNull());
      assertTrue(responseNode.get("sort").isNull());
      
      // Check variables are actually returned
      ArrayNode rows = (ArrayNode) responseNode.get("data");
      assertNotNull(rows);
      assertEquals(3, rows.size());
      
      
      // Check sorting, ascending
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_DATA, tableName) + "?orderAscendingColumn=LONG_");
      response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

      responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals(3, responseNode.get("total").intValue());
      assertEquals(3, responseNode.get("size").intValue());
      assertEquals(0, responseNode.get("start").intValue());
      assertEquals("asc", responseNode.get("order").textValue());
      assertEquals("LONG_", responseNode.get("sort").textValue());
      rows = (ArrayNode) responseNode.get("data");
      assertNotNull(rows);
      assertEquals(3, rows.size());
      
      assertEquals("var1", rows.get(0).get("NAME_").textValue());
      assertEquals("var2", rows.get(1).get("NAME_").textValue());
      assertEquals("var3", rows.get(2).get("NAME_").textValue());
      
      // Check sorting, descending
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_DATA, tableName) + "?orderDescendingColumn=LONG_");
      response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

      responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals(3, responseNode.get("total").intValue());
      assertEquals(3, responseNode.get("size").intValue());
      assertEquals(0, responseNode.get("start").intValue());
      assertEquals("desc", responseNode.get("order").textValue());
      assertEquals("LONG_", responseNode.get("sort").textValue());
      rows = (ArrayNode) responseNode.get("data");
      assertNotNull(rows);
      assertEquals(3, rows.size());
      
      assertEquals("var3", rows.get(0).get("NAME_").textValue());
      assertEquals("var2", rows.get(1).get("NAME_").textValue());
      assertEquals("var1", rows.get(2).get("NAME_").textValue());
      
      
      // Finally, check result limiting
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_DATA, tableName) + "?orderAscendingColumn=LONG_&start=1&size=1");
      response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

      responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals(3, responseNode.get("total").intValue());
      assertEquals(1, responseNode.get("size").intValue());
      assertEquals(1, responseNode.get("start").intValue());
      rows = (ArrayNode) responseNode.get("data");
      assertNotNull(rows);
      assertEquals(1, rows.size());
      assertEquals("var2", rows.get(0).get("NAME_").textValue());

    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }

  public void testGetDataForUnexistingTable() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_DATA, "unexisting"));
    try {
      client.get();
      fail();
    } catch (ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
      assertEquals("Could not find a table with name 'unexisting'.", client.getResponse().getStatus().getDescription());
    }
  }
  
  public void testGetDataSortByIllegalColumn() throws Exception {
    // We use variable-table as a reference
    String tableName = managementService.getTableName(VariableInstanceEntity.class);

    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_DATA, tableName) + "?orderAscendingColumn=unexistingColumn");
    try {
      client.get();
      fail("404 expected, but was: " + client.getResponse().getStatus());
    } catch (ResourceException expected) {
      assertEquals(Status.SERVER_ERROR_INTERNAL, client.getResponse().getStatus());
    }
  }
}