package org.activiti.cycle.impl;

import java.io.Serializable;

import org.activiti.cycle.ContentRepresentation;

/**
 * Data structure for link to content, including the URL to the content, the
 * type (see {@link ContentType}) and a name (which is shown in
 * the GUI).
 * 
 * The client URL should be normally set by the infrastructure, so a
 * {@link ContentRepresentationProvider} can concentrate on really providing the
 * content itself (as byte array). If that is an expensive operation (maybe slow
 * or big content), then this should only be done if the
 * {@link ContentRepresentationProvider} is asked to create the content as well.
 * 
 * @author bernd.ruecker@camunda.com
 */
public class ContentRepresentationImpl implements ContentRepresentation, Serializable {
	
	private static final long serialVersionUID = 1L;

	/**
   * type of content as normally indicated by {@link ContentType}
   * (e.g. text file, image, ...). Information for the client to render it
   * correctly.
   */
  private String mimeType;

  /**
   * Name of this representation, serves as a <b>unique key</b> to query the
   * correct representation and may be used by the client to show a list of
   * possible {@link ContentRepresentationDefinition}s
   */
	private String id;

  public ContentRepresentationImpl(String id, String mimeType) {
    super();
    this.id = id;
    this.mimeType = mimeType;
  }
  
  public String getId() {
    return id;
  }

  public String getMimeType() {
    return mimeType;
  }
}
