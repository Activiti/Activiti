package org.activiti.cycle.impl.transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.activiti.cycle.Content;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.impl.mimetype.TextMimeType;
import org.activiti.cycle.impl.mimetype.XmlMimeType;
import org.activiti.cycle.transform.ContentMimeTypeTransformation;
import org.activiti.cycle.transform.ContentTransformationException;
import org.activiti.engine.impl.util.IoUtil;
import org.w3c.dom.Document;

/**
 * {@link ContentMimeTypeTransformation} for transforming {@link XmlMimeType}
 * -Content to {@link TextMimeType}-Content
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent
public class XmlToTextTransformation implements ContentMimeTypeTransformation {

  public MimeType getSourceType() {
    return CycleApplicationContext.get(XmlMimeType.class);
  }

  public MimeType getTargetType() {
    return CycleApplicationContext.get(TextMimeType.class);
  }

  public Content transformContent(Content content) throws ContentTransformationException {
    Content newContent = new Content();
    InputStream is = content.asInputStream();
    try {
      Document doc = buildDocument(is);
      String xmlString = getXmlAsString(new DOMSource(doc));
      newContent.setValue(xmlString);
      return new Content();
    } catch (Exception e) {
      throw new ContentTransformationException("Error during content transformation form Xml to Text.", e);
    } finally {
      IoUtil.closeSilently(is);
    }
  }

  private Document buildDocument(InputStream is) throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
    db = dbf.newDocumentBuilder();
    return db.parse(is);
  }

  public String getXmlAsString(DOMSource xmlData) throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException,
          IOException {
    StringWriter stringWriter = new StringWriter();
    StreamResult streamResult = new StreamResult(stringWriter);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.transform(xmlData, streamResult);
    return stringWriter.toString();
  }

}
