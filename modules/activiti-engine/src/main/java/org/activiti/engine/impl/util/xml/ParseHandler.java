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
package org.activiti.engine.impl.util.xml;

import java.util.Stack;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author Tom Baeyens
 */
public class ParseHandler extends DefaultHandler {
  
  private static Logger log = Logger.getLogger(ParseHandler.class.getName());

  protected String defaultNamespace;
  protected Parse parse;
  protected Locator locator;
  protected Stack<Element> elementStack = new Stack<Element>();
  
  public ParseHandler(Parse parse) {
    this.parse = parse;
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    Element element = new Element(uri, localName, qName, attributes, locator);
    if (elementStack.isEmpty()) {
      parse.rootElement = element;
    } else {
      elementStack.peek().add(element);
    }
    elementStack.push(element);
  }
  
  public void characters(char[] ch, int start, int length) throws SAXException {
    elementStack.peek().appendText(String.valueOf(ch, start, length));
  }
  
  public void endElement(String uri, String localName, String qName) throws SAXException {
    elementStack.pop();
  }

  public void error(SAXParseException e) {
    parse.addError(e);
  }
  public void fatalError(SAXParseException e) {
    parse.addError(e);
  }
  public void warning(SAXParseException e) {
    log.warning(e.toString());
  }
  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }
  
  public void setDefaultNamespace(String defaultNamespace) {
    this.defaultNamespace = defaultNamespace;
  }


}
