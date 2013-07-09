package org.activiti.bpmn.converter.util;

import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.apache.commons.lang3.StringUtils;

public class BpmnXMLUtil implements BpmnXMLConstants {

  public static void addXMLLocation(BaseElement element, XMLStreamReader xtr) {
    Location location = xtr.getLocation();
    element.setXmlRowNumber(location.getLineNumber());
    element.setXmlColumnNumber(location.getColumnNumber());
  }
  
  public static void addXMLLocation(GraphicInfo graphicInfo, XMLStreamReader xtr) {
    Location location = xtr.getLocation();
    graphicInfo.setXmlRowNumber(location.getLineNumber());
    graphicInfo.setXmlColumnNumber(location.getColumnNumber());
  }
  
  public static void writeDefaultAttribute(String attributeName, String value, XMLStreamWriter xtw) throws Exception {
    if (StringUtils.isNotEmpty(value) && "null".equalsIgnoreCase(value) == false) {
      xtw.writeAttribute(attributeName, value);
    }
  }
  
  public static void writeQualifiedAttribute(String attributeName, String value, XMLStreamWriter xtw) throws Exception {
    if (StringUtils.isNotEmpty(value)) {
      xtw.writeAttribute(ACTIVITI_EXTENSIONS_PREFIX, ACTIVITI_EXTENSIONS_NAMESPACE, attributeName, value);
    }
  }
  
  public static List<String> parseDelimitedList(String s) {
    List<String> result = new ArrayList<String>();
    if (StringUtils.isNotEmpty(s)) {

      StringCharacterIterator iterator = new StringCharacterIterator(s);
      char c = iterator.first();

      StringBuilder strb = new StringBuilder();
      boolean insideExpression = false;

      while (c != StringCharacterIterator.DONE) {
        if (c == '{' || c == '$') {
          insideExpression = true;
        } else if (c == '}') {
          insideExpression = false;
        } else if (c == ',' && !insideExpression) {
          result.add(strb.toString().trim());
          strb.delete(0, strb.length());
        }

        if (c != ',' || (insideExpression)) {
          strb.append(c);
        }

        c = iterator.next();
      }

      if (strb.length() > 0) {
        result.add(strb.toString().trim());
      }

    }
    return result;
  }
  
  public static String convertToDelimitedString(List<String> stringList) {
    StringBuilder resultString = new StringBuilder();
    for (String result : stringList) {
      if (resultString.length() > 0) {
        resultString.append(",");
      }
      resultString.append(result);
    }
    return resultString.toString();
  }
  
}
