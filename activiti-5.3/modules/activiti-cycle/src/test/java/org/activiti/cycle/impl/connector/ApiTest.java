package org.activiti.cycle.impl.connector;

import static org.junit.Assert.assertEquals;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNodeCollection;
import org.junit.Test;

/**
 * Test suite to test {@link RepositoryConnector}-API
 * 
 * @author bernd.ruecker@camunda.com
 * @author nils.preusker@camunda.com
 */
public class ApiTest {

  private RepositoryConnector connector = null;
  private String rootFolderId = "/";
  
  public void init() {
    RepositoryFolder folderTest1 = connector.createFolder(rootFolderId, "test1");
  }
  
  public void test() {
    RepositoryNodeCollection children = connector.getChildren("/");
    assertEquals(2, children.asList().size());
    
  }
  
  @Test
  public void noTestYet() {

  }

}
