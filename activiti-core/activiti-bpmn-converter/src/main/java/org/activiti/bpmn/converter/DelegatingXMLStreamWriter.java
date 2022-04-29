/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.bpmn.converter;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


abstract class DelegatingXMLStreamWriter implements XMLStreamWriter {

  private final XMLStreamWriter writer;

  public DelegatingXMLStreamWriter(XMLStreamWriter writer) {
    this.writer = writer;
  }

  public void writeStartElement(String localName) throws XMLStreamException {
    writer.writeStartElement(localName);
  }

  public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
    writer.writeStartElement(namespaceURI, localName);
  }

  public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
    writer.writeStartElement(prefix, localName, namespaceURI);
  }

  public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
    writer.writeEmptyElement(namespaceURI, localName);
  }

  public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
    writer.writeEmptyElement(prefix, localName, namespaceURI);
  }

  public void writeEmptyElement(String localName) throws XMLStreamException {
    writer.writeEmptyElement(localName);
  }

  public void writeEndElement() throws XMLStreamException {
    writer.writeEndElement();
  }

  public void writeEndDocument() throws XMLStreamException {
    writer.writeEndDocument();
  }

  public void close() throws XMLStreamException {
    writer.close();
  }

  public void flush() throws XMLStreamException {
    writer.flush();
  }

  public void writeAttribute(String localName, String value) throws XMLStreamException {
    writer.writeAttribute(localName, value);
  }

  public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
    writer.writeAttribute(prefix, namespaceURI, localName, value);
  }

  public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
    writer.writeAttribute(namespaceURI, localName, value);
  }

  public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
    writer.writeNamespace(prefix, namespaceURI);
  }

  public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
    writer.writeDefaultNamespace(namespaceURI);
  }

  public void writeComment(String data) throws XMLStreamException {
    writer.writeComment(data);
  }

  public void writeProcessingInstruction(String target) throws XMLStreamException {
    writer.writeProcessingInstruction(target);
  }

  public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
    writer.writeProcessingInstruction(target, data);
  }

  public void writeCData(String data) throws XMLStreamException {
    writer.writeCData(data);
  }

  public void writeDTD(String dtd) throws XMLStreamException {
    writer.writeDTD(dtd);
  }

  public void writeEntityRef(String name) throws XMLStreamException {
    writer.writeEntityRef(name);
  }

  public void writeStartDocument() throws XMLStreamException {
    writer.writeStartDocument();
  }

  public void writeStartDocument(String version) throws XMLStreamException {
    writer.writeStartDocument(version);
  }

  public void writeStartDocument(String encoding, String version) throws XMLStreamException {
    writer.writeStartDocument(encoding, version);
  }

  public void writeCharacters(String text) throws XMLStreamException {
    writer.writeCharacters(text);
  }

  public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
    writer.writeCharacters(text, start, len);
  }

  public String getPrefix(String uri) throws XMLStreamException {
    return writer.getPrefix(uri);
  }

  public void setPrefix(String prefix, String uri) throws XMLStreamException {
    writer.setPrefix(prefix, uri);
  }

  public void setDefaultNamespace(String uri) throws XMLStreamException {
    writer.setDefaultNamespace(uri);
  }

  public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
    writer.setNamespaceContext(context);
  }

  public NamespaceContext getNamespaceContext() {
    return writer.getNamespaceContext();
  }

  public Object getProperty(String name) throws IllegalArgumentException {
    return writer.getProperty(name);
  }
}
