package org.activiti.rest.service.api.management;

import java.util.Map;

import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test for all REST-operations related to the Table collection and a single
 * table resource.
 * 
 * @author Frederik Heremans
 */
public class TableResourceTest extends BaseSpringRestTestCase {

  /**
   * Test getting tables. GET management/tables
   */
  public void testGetTables() throws Exception {
    Map<String, Long> tableCounts = managementService.getTableCount();

    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLES_COLLECTION)), HttpStatus.SC_OK);
    
    // Check table array
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(tableCounts.size(), responseNode.size());

    for (int i = 0; i < responseNode.size(); i++) {
      ObjectNode table = (ObjectNode) responseNode.get(i);
      assertNotNull(table.get("name").textValue());
      assertNotNull(table.get("count").longValue());
      assertTrue(table.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE, table.get("name").textValue())));

      assertEquals(((Long) tableCounts.get(table.get("name").textValue())).longValue(), table.get("count").longValue());
    }
  }

  /**
   * Test getting a single table. GET management/tables/{tableName}
   */
  public void testGetTable() throws Exception {
    Map<String, Long> tableCounts = managementService.getTableCount();

    String tableNameToGet = tableCounts.keySet().iterator().next();

    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE, tableNameToGet)), HttpStatus.SC_OK);
    
    // Check table
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals(tableNameToGet, responseNode.get("name").textValue());
    assertEquals(((Long) tableCounts.get(responseNode.get("name").textValue())).longValue(), responseNode.get("count").longValue());
    assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE, tableNameToGet)));
  }
  
  public void testGetUnexistingTable() throws Exception {
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE, "unexisting")), HttpStatus.SC_NOT_FOUND));
  }
}