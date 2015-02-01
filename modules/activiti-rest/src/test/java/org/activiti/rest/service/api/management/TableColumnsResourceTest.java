package org.activiti.rest.service.api.management;

import org.activiti.engine.management.TableMetaData;
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
public class TableColumnsResourceTest extends BaseSpringRestTestCase {

  /**
   * Test getting a single table's columns. 
   * GET management/tables/{tableName}/columns
   */
  public void testGetTableColumns() throws Exception {
    String tableName = managementService.getTableCount().keySet().iterator().next();
    
    TableMetaData metaData = managementService.getTableMetaData(tableName);

    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_COLUMNS, tableName)), HttpStatus.SC_OK);
    
    // Check table
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals(tableName, responseNode.get("tableName").textValue());
    
    ArrayNode names = (ArrayNode) responseNode.get("columnNames");
    ArrayNode types = (ArrayNode) responseNode.get("columnTypes");
    assertNotNull(names);
    assertNotNull(types);
    
    assertEquals(metaData.getColumnNames().size(), names.size());
    assertEquals(metaData.getColumnTypes().size(), types.size());
    
    for(int i=0; i<names.size(); i++) {
      assertEquals(names.get(i).textValue(), metaData.getColumnNames().get(i));
      assertEquals(types.get(i).textValue(), metaData.getColumnTypes().get(i));
    }
  }
  
  public void testGetColumnsForUnexistingTable() throws Exception {
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_COLUMNS, "unexisting")), HttpStatus.SC_NOT_FOUND));
  }
}