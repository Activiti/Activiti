package org.activiti.cycle.impl;

import java.io.Serializable;

import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.StandardMimeType;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.MimeType;

/**
 * Data structure for link to content, including the URL to the content, the
 * type (see {@link StandardMimeType}) and a name (which is shown in the GUI).
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
   * Name of this representation, serves as a <b>unique key</b> to query the
   * correct representation and may be used by the client to show a list of
   * possible {@link ContentRepresentationDefinition}s
   */
  private String id;

  /**
   * type of content (e.g. text file, image, ...). Information for the client to
   * render the content correctly.
   */
  private MimeType mimeType;

  /**
   * The renderInfo attribute provides information as to how to render the
   * contentRepresentation instance. The available formats are defined in
   * {@link RenderInfo}.
   */
  private RenderInfo renderInfo;

  public ContentRepresentationImpl(String id, MimeType mimeType, RenderInfo renderInfo) {
    super();
    this.id = id;
    this.mimeType = mimeType;
    this.renderInfo = renderInfo;
  }

  public String getId() {
    return id;
  }

  public MimeType getMimeType() {
    return this.mimeType;
  }

  public RenderInfo getRenderInfo() {
    return this.renderInfo;
  }
}
