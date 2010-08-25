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
import java.util.ListIterator;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

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
  
  protected Parser parser;
  protected String name;
  protected StreamSource streamSource;
  protected Element rootElement = null;
  protected List<Problem> problems = new ArrayList<Problem>();
  protected String schemaResource;
  protected Stack<Object> contextStack;

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
      if (schemaResource != null) {
        
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(Thread.currentThread().getContextClassLoader().getResource(schemaResource));
        parser.getSaxParserFactory().setSchema(schema);
        
        Validator validator = schema.newValidator();
        validator.validate(new javax.xml.transform.stream.StreamSource(inputStream));
        
        // The validator will read the stream until the end, rendering it useless for further usage
        // PLEASE do not remove this comment. It has cost me 2 hours to figure it out.
        inputStream.reset();
      }
      
      SAXParser saxParser = parser.getSaxParser(); 
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
    return problems;
  }

  public void addProblem(SAXParseException e) {
    problems.add(new Problem(e, name));
  }
  
  public void addProblem(String errorMessage, Element element) {
    problems.add(new Problem(errorMessage, name, element));
  }
  
  public void setSchemaResource(String schemaResource) {
    parser.getSaxParserFactory().setNamespaceAware(true);
    parser.getSaxParserFactory().setValidating(true);
    this.schemaResource = schemaResource;
  }
  
  public void pushContextObject(Object obj) {
    if (contextStack == null) {
      contextStack = new Stack<Object>();
    }
    contextStack.push(obj);
  }
  
  public Object popContextObject() {
    if (contextStack != null) {
      return contextStack.pop();      
    } else {
      throw new ActivitiException("Context stack was never initialised, so calling the pop() operation is invalid");
    }
  }
  
  /**
   * Searches the contextual stack from top to bottom for an object of the given class
   */
  public <T> T findContextualObject(Class<T> clazz) {
    if (contextStack != null) {
      ListIterator<Object> iterator = contextStack.listIterator(contextStack.size());
      while (iterator.hasPrevious()) {
        Object obj = iterator.previous();
        if (clazz.isInstance(obj)) {
          return clazz.cast(obj);
        }
      }
    }
    return null;
  }
}
