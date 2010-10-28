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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.impl.util.io.ResourceStreamSource;
import org.activiti.engine.impl.util.io.StreamSource;
import org.activiti.engine.impl.util.io.StringStreamSource;
import org.activiti.engine.impl.util.io.UrlStreamSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author Tom Baeyens
 */
public class Parse extends DefaultHandler {
  
  private static final Logger LOGGER = Logger.getLogger(Parse.class.getName());
  
  private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
  private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
  private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
  
  private static final String NEW_LINE = System.getProperty("line.separator");
  
  protected Parser parser;
  protected String name;
  protected StreamSource streamSource;
  protected Element rootElement = null;
  protected List<Problem> errors = new ArrayList<Problem>();
  protected List<Problem> warnings = new ArrayList<Problem>();
  protected String schemaResource;

  public Parse(Parser parser) {
    this.parser = parser;
  }
  
  public Parse name(String name) {
    this.name = name;
    return this;
  }
  
  public Parse sourceInputStream(InputStream inputStream) {
    if (name==null) {
      name("inputStream");
    }
    setStreamSource(new InputStreamSource(inputStream)); 
    return this;
  }

  public Parse sourceResource(String resource) {
    return sourceResource(resource, null);
  }

  public Parse sourceUrl(URL url) {
    if (name==null) {
      name(url.toString());
    }
    setStreamSource(new UrlStreamSource(url));
    return this;
  }
  
  public Parse sourceUrl(String url) {
    try {
      return sourceUrl(new URL(url));
    } catch (MalformedURLException e) {
      throw new ActivitiException("malformed url: "+url, e);
    }
  }
  
  public Parse sourceResource(String resource, ClassLoader classLoader) {
    if (name==null) {
      name(resource);
    }
    setStreamSource(new ResourceStreamSource(resource, classLoader)); 
    return this;
  }

  public Parse sourceString(String string) {
    if (name==null) {
      name("string");
    }
    setStreamSource(new StringStreamSource(string)); 
    return this;
  }

  protected void setStreamSource(StreamSource streamSource) {
    if (this.streamSource!=null) {
      throw new ActivitiException("invalid: multiple sources "+this.streamSource+" and "+streamSource);
    }
    this.streamSource = streamSource;
  }
  
  public Parse execute() {
    try {
      InputStream inputStream = streamSource.getInputStream();

      if (schemaResource == null) { // must be done before parser is created
        parser.getSaxParserFactory().setNamespaceAware(false);
        parser.getSaxParserFactory().setValidating(false);
      }

      SAXParser saxParser = parser.getSaxParser(); 
      if (schemaResource != null) { 
        saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        saxParser.setProperty(JAXP_SCHEMA_SOURCE, schemaResource);
      }
      saxParser.parse(inputStream, new ParseHandler(this));
      
    } catch (Exception e) { // any exception can happen (Activiti, Io, etc.)
      throw new ActivitiException("couldn't parse '"+name+"': "+e.getMessage(), e);
    }
    
    return this;
  }

  public Element getRootElement() {
    return rootElement;
  }

  public List<Problem> getProblems() {
    return errors;
  }

  public void addError(SAXParseException e) {
    errors.add(new Problem(e, name));
  }
  
  public void addError(String errorMessage, Element element) {
    errors.add(new Problem(errorMessage, name, element));
  }
  
  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }
  
  public void addWarning(SAXParseException e) {
    warnings.add(new Problem(e, name));
  }
  
  public void addWarning(String errorMessage, Element element) {
    warnings.add(new Problem(errorMessage, name, element));
  }
  
  public boolean hasWarnings() {
    return warnings != null && !warnings.isEmpty();
  }
  
  public void logWarnings() {
    for (Problem warning : warnings) {
      LOGGER.warning(warning.toString());
    }
  }
  
  public void throwActivitiExceptionForErrors() {
    StringBuilder strb = new StringBuilder();
    for (Problem error : errors) {
      strb.append(error.toString());
      strb.append(NEW_LINE);
    }
    throw new ActivitiException(strb.toString());
  }
  
  public void setSchemaResource(String schemaResource) {
    SAXParserFactory saxParserFactory = parser.getSaxParserFactory();
    saxParserFactory.setNamespaceAware(true);
    saxParserFactory.setValidating(true);
    this.schemaResource = schemaResource;
  }

}
