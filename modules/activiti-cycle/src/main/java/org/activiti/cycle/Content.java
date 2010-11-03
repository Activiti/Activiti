package org.activiti.cycle;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.activiti.engine.impl.util.IoUtil;

/**
 * This class encapsulates real content (text, images, ...).
 * 
 * Depending on the source or usage of the content, you may have strings, byte
 * arrays, streams, ... This class keeps track of the necessary transformations.
 * 
 * So best is to initialize it with what you have and retrieve what you need and
 * don't think about it.
 * 
 * @author bernd.ruecker@camunda.com
 */
public class Content {
  
  private byte[] contentAsByteArray;

  private String contentAsString;

  private InputStream contentAsInputStream;
  
  private byte[] loadBytes(InputStream inputStream) {
    byte[] bytes = IoUtil.readInputStream(inputStream, "Repository Artifact Content");
    IoUtil.closeSilently(inputStream);
    return bytes;
  }
  
   public byte[] asByteArray() {
    if (contentAsByteArray != null) {
      return contentAsByteArray;
    } else if (contentAsString != null) {
      return contentAsString.getBytes();
    } else if (contentAsInputStream != null) {
      return loadBytes(contentAsInputStream);
    } else {
      throw new RuntimeException("Not yet implemented");
    }
  }

  public String asString() {
    if (contentAsString != null) {
      return contentAsString;
    } else if (contentAsByteArray != null) {
      return new String(contentAsByteArray);
    } else if (contentAsInputStream != null) {
      return new String(loadBytes(contentAsInputStream));
    } else {
      throw new RuntimeException("Not yet implemented");
    }
  }

  public InputStream asInputStream() {
    if (contentAsString != null) {
      return new ByteArrayInputStream(contentAsString.getBytes());
    } else if (contentAsByteArray != null) {
      return new ByteArrayInputStream(contentAsByteArray);
    } else if (contentAsInputStream != null) {
      return contentAsInputStream;
    } else {
      throw new RuntimeException("Not yet implemented");
    }
  }
  
  public boolean isNull() {
    return (contentAsString == null && contentAsByteArray == null && contentAsInputStream == null);
  }

  public void setValue(String text) {
    this.contentAsString = text;
    this.contentAsByteArray = null;
    this.contentAsInputStream = null;
  }

  public void setValue(byte[] content) {
    this.contentAsString = null;
    this.contentAsByteArray = content;
    this.contentAsInputStream = null;
  }

  /**
   * TODO: When can we close that stream? How do we now we are done?
   */
  public void setValue(InputStream stream) {
    this.contentAsString = null;
    this.contentAsByteArray = null;
    this.contentAsInputStream = stream;
  }

}
