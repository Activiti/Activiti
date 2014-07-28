package org.activiti.rest.service.api.management;

import java.util.Map;

import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test for all REST-operations related to the Table collection and a single
 * table resource.
 * 
 * @author Frederik Heremans
 */
public class TableResourceTest extends BaseRestTestCase {

  /**
   * Test getting tables. GET management/tables
   */
  public void testGetTables() throws Exception {
    Map<String, Long> tableCounts = managementService.getTableCount();

    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLES_COLLECTION));
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    // Check table array
    JsonNode responseNode = objectMapper.readTree(response.getStream());
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

    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE, tableNameToGet));
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    // Check table
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(tableNameToGet, responseNode.get("name").textValue());
    assertEquals(((Long) tableCounts.get(responseNode.get("name").textValue())).longValue(), responseNode.get("count").longValue());
    assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE, tableNameToGet)));
  }
  
  public void testGetUnexistingTable() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE, "unexisting"));
    try {
      client.get();
      fail("404 expected, but was: " + client.getResponse().getStatus());
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
      assertEquals("Could not find a table with name 'unexisting'.", client.getResponse().getStatus().getDescription());
    }
  }
}