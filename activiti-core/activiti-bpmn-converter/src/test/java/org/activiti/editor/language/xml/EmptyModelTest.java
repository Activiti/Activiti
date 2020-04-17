package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.activiti.bpmn.exceptions.XMLException;
import org.junit.jupiter.api.Test;

public class EmptyModelTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    assertThatExceptionOfType(XMLException.class)
      .isThrownBy(() -> readXMLFile());
  }

  @Test
  public void convertModelToXML() throws Exception {
    assertThatExceptionOfType(XMLException.class)
      .isThrownBy(() -> readXMLFile());
  }

  protected String getResource() {
    return "empty.bpmn";
  }
}
