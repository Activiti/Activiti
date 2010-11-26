package org.activiti.cycle.impl.connector.signavio.util;

import static org.junit.Assert.*;

import org.junit.Test;


public class CustomPropertyTest {

  @Test
  public void testGetValue() {
    String expected = "Do\n\"Something\"";

    String propertyContainer = "Original Name: \"Do\\n\"\"Something\"\"\" Original ID: \"sid-910BF305-8A75-4319-A3A9-23C5B2828944\"";
    String actual = CustomProperty.ORIGINAL_NAME.getValue(propertyContainer);
    assertEquals(expected, actual);

    propertyContainer = "Original Name: \"Do\\n\"\"Something\"\"\"\nOriginal ID: \"sid-910BF305-8A75-4319-A3A9-23C5B2828944\"";
    actual = CustomProperty.ORIGINAL_NAME.getValue(propertyContainer);
    assertEquals(expected, actual);
  }

}
