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

import org.xml.sax.SAXParseException;


/**
 * @author Tom Baeyens
 */
public class Problem {

  protected String errorMessage;
  protected String resource;
  protected int line;
  protected int column;

  public Problem(SAXParseException e, String resource) {
    Throwable exception = e;
    while (exception!=null) {
      if (this.errorMessage==null) {
        this.errorMessage = exception.getMessage(); 
      } else {
        this.errorMessage += ": "+exception.getMessage();
      }
      exception = exception.getCause();
    }
    this.line = e.getLineNumber();
    this.column = e.getColumnNumber();
  }
  
  public Problem(String errorMessage, String resourceName, Element element) {
    this.errorMessage = errorMessage;
    this.resource = resourceName;
    if (element!=null) {
      this.line = element.getLine();
      this.column = element.getColumn();
    }
  }

  public String toString() {
    return errorMessage+(resource!=null ? " | "+resource : "")+" | line "+line+" | column "+column;
  }
}
