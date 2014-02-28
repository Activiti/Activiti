package org.activiti.rest.service.api.management;

import org.activiti.engine.management.TableMetaData;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Test for all REST-operations related to the Table columns.
 * 
 * @author Frederik Heremans
 */
public class TableColumnsResourceTest extends BaseRestTestCase {


  /**
   * Test getting a single table's columns. 
   * GET management/tables/{tableName}/columns
   */
  public void testGetTableColumns() throws Exception {
    String tableName = managementService.getTableCount().keySet().iterator().next();
    
    TableMetaData metaData = managementService.getTableMetaData(tableName);

    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_COLUMNS, tableName));
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    // Check table
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(tableName, responseNode.get("tableName").getTextValue());
    
    ArrayNode names = (ArrayNode) responseNode.get("columnNames");
    ArrayNode types = (ArrayNode) responseNode.get("columnTypes");
    assertNotNull(names);
    assertNotNull(types);
    
    assertEquals(metaData.getColumnNames().size(), names.size());
    assertEquals(metaData.getColumnTypes().size(), types.size());
    
    for(int i=0; i<names.size(); i++) {
      assertEquals(names.get(i).getTextValue(), metaData.getColumnNames().get(i));
      assertEquals(types.get(i).getTextValue(), metaData.getColumnTypes().get(i));
    }
  }
  
  public void testGetColumnsForUnexistingTable() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TABLE_COLUMNS, "unexisting"));
    try {
      client.get();
      fail("404 expected, but was: " + client.getResponse().getStatus());
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
      assertEquals("Could not find a table with name 'unexisting'.", client.getResponse().getStatus().getDescription());
    }
  }
}