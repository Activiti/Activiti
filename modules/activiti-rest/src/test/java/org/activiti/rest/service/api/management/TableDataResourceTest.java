package org.activiti.rest.service.api.management;

import java.util.List;

import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Test for all REST-operations related to the Table columns.
 * 
 * @author Frederik Heremans
 */
public class TableDataResourceTest extends BaseSpringRestTestCase {

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

      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_DATA, tableName)), HttpStatus.SC_OK);
      
      // Check paging result
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
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
      response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_DATA, tableName) + "?orderAscendingColumn=LONG_"), HttpStatus.SC_OK);
      responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
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
      response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_DATA, tableName) + "?orderDescendingColumn=LONG_"), HttpStatus.SC_OK);
      responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
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
      response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_DATA, tableName) + "?orderAscendingColumn=LONG_&start=1&size=1"), HttpStatus.SC_OK);
      responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
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
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_DATA, "unexisting")), HttpStatus.SC_NOT_FOUND));
  }
  
  public void testGetDataSortByIllegalColumn() throws Exception {
    // We use variable-table as a reference
    String tableName = managementService.getTableName(VariableInstanceEntity.class);
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_DATA, tableName) + "?orderAscendingColumn=unexistingColumn"), HttpStatus.SC_INTERNAL_SERVER_ERROR));
  }
}