package org.activiti.cycle.impl.connector.signavio.util;

import static org.junit.Assert.*;

import org.activiti.cycle.impl.ActivitiCycleTest;
import org.junit.Test;


public class CustomPropertyTest extends ActivitiCycleTest {

  @Test
  public void testGetValue() {
    String expected = "Do\n\"Something\"";

    String propertyContainer = "Original Name: \"Do\\n\"\"Something\"\"\" Original ID: \"sid-910BF305-8A75-4319-A3A9-23C5B2828944\"";
    String actual = CustomProperty.ORIGINAL_NAME.getValue(propertyContainer);
    assertEquals(expected, actual);

    propertyContainer = "Original Name: \"Do\\n\"\"Something\"\"\"\nOriginal ID: \"sid-910BF305-8A75-4319-A3A9-23C5B2828944\"";
    actual = CustomProperty.ORIGINAL_NAME.getValue(propertyContainer);
    assertEquals(expected, actual);

    expected = "";
    propertyContainer = "Original Name: \"\" Original ID: \"sid-910BF305-8A75-4319-A3A9-23C5B2828944\"";
    actual = CustomProperty.ORIGINAL_NAME.getValue(propertyContainer);
    assertEquals(expected, actual);

    propertyContainer = "Original Name: \"\"\nOriginal ID: \"sid-910BF305-8A75-4319-A3A9-23C5B2828944\"";
    actual = CustomProperty.ORIGINAL_NAME.getValue(propertyContainer);
    assertEquals(expected, actual);

    expected = "\"";
    propertyContainer = "Original Name: \"\"\"\" Original ID: \"sid-910BF305-8A75-4319-A3A9-23C5B2828944\"";
    actual = CustomProperty.ORIGINAL_NAME.getValue(propertyContainer);
    assertEquals(expected, actual);

    propertyContainer = "Original Name: \"\"\"\"\nOriginal ID: \"sid-910BF305-8A75-4319-A3A9-23C5B2828944\"";
    actual = CustomProperty.ORIGINAL_NAME.getValue(propertyContainer);
    assertEquals(expected, actual);
  }
  
  @Test
  public void testSetValueUnlessPropertyExists() {
    String propertyContainer = "Original ID: \"sid-9DFFCBB5-DA6F-4981-BEC5-68D4B24700E9\" Original Type: \"SequenceFlow\"";
    assertEquals(propertyContainer, CustomProperty.ORIGINAL_ID.setValueUnlessPropertyExists(propertyContainer, "sid-9DFFCBB5-DA6F-4981-BEC5-68D4B24700E9-copy-1"));
  }

}
