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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * @author Tom Baeyens
 */
public class Parser {

  protected static SAXParserFactory defaultSaxParserFactory = SAXParserFactory.newInstance();
  
  public static final Parser INSTANCE = new Parser();

  public Parse createParse() {
    return new Parse(this);
  }

  protected SAXParser getSaxParser() throws Exception {
    return getSaxParserFactory().newSAXParser();
  }

  protected SAXParserFactory getSaxParserFactory() {
    return defaultSaxParserFactory;
  }
}
