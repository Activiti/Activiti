package org.activiti.cycle.impl.transform.signavio;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.junit.Test;


public class SubProcessExpansionTest {

  @Test
  public void testGetModelIdFromSignavioUrl() throws UnsupportedEncodingException {
    assertEquals("root-directory;sub-process-child.signavio.xml", SubProcessExpansion.getModelIdFromSignavioUrl("http://localhost:8080/activiti-modeler/p/editor?id=root-directory%3Bsub-process-child.signavio.xml"));
    assertEquals("aa4414840103420193a18de25ae4b446", SubProcessExpansion.getModelIdFromSignavioUrl("https://editor.signavio.com/p/model/aa4414840103420193a18de25ae4b446"));
  }

}
