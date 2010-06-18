/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.impl.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.ActivitiException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;


/**
 * represents one XML element.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class Element {

  protected String uri;
  protected String tagName;
  
  /* 
   * Key of map = 'uri':attributeName 
   * 
   * if namespace is empty, key is 'attributeName'
   */
  protected Map<String, Attribute> attributeMap = new HashMap<String, Attribute>();
  
  protected int line;
  protected int column;
  protected String text;
  protected List<Element> elements = new ArrayList<Element>();
  
  public Element(String uri, String localName, String qName, Attributes attributes, Locator locator) {
    this.uri = uri;
    this.tagName = qName;
    
    if (attributes!=null) {
      for (int i=0; i<attributes.getLength(); i++) {
        String name = attributes.getLocalName(i);
        String value = attributes.getValue(i);
        String attributeUri = attributes.getURI(i);
        this.attributeMap.put(composeMapKey(attributeUri, name), 
          new Attribute(name, value, attributeUri));
      }
    }
    
    if (locator!=null) {
      line = locator.getLineNumber();
      column = locator.getColumnNumber();
    }
  }

  public List<Element> elements(String tagName) {
    List<Element> selectedElements = new ArrayList<Element>();
    for (Element element: elements) {
      if (tagName.equals(element.getTagName())) {
        selectedElements.add(element);
      }
    }
    return selectedElements;
  }
  
  public Element element(String tagName) {
    List<Element> elements = elements(tagName);
    if (elements.size() == 0) {
      return null;
    } else if (elements.size() > 1) {      
      throw new ActivitiException("Parsing exception: multiple elements with tag name " + tagName + " found");
    }
    return elements.get(0);
  }

  public void add(Element element) {
    elements.add(element);
  }

  public String attribute(String name) {
    if (attributeMap.containsKey(name)) {
      return attributeMap.get(name).getValue();
    }
    return null;
  }
  
  public String attributeNS(String namespaceUri, String name) {
    return attribute(composeMapKey(namespaceUri, name));
  }
  
  public String attribute(String name, String defaultValue) {
    if (attributeMap.containsKey(name)) {
      return attributeMap.get(name).getValue();
    }
    return defaultValue;
  }
  
  public String attributeNS(String namespaceUri, String name, String defaultValue) {
    return attribute(composeMapKey(namespaceUri, name), defaultValue);
  }
  
  protected String composeMapKey(String attributeUri, String attributeName) {
    StringBuilder strb = new StringBuilder();
    if (attributeUri != null && !attributeUri.equals("")) {
      strb.append(attributeUri);
      strb.append(":");
    }
    strb.append(attributeName);
    return strb.toString();
  }

  public List<Element> elements() {
    return elements;
  }
  
  public String toString() {
    return "<"+tagName+"...";
  }
  
  
  public String getUri() {
    return uri;
  }
  public String getTagName() {
    return tagName;
  }
  public int getLine() {
    return line;
  }
  public int getColumn() {
    return column;
  }
  public void setText(String text) {
    this.text = text;
  }
  public String getText() {
    return text;
  }
}
