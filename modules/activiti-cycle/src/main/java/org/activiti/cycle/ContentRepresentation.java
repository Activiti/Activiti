package org.activiti.cycle;

import java.io.Serializable;

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
public interface ContentRepresentation extends Serializable {

  /**
   * Name of this representation, serves as a <b>unique key</b> to query the
   * correct representation and may be used by the client to show a list of
   * possible {@link ContentRepresentationDefinition}s
   */
  public String getId();

  /**
   * Type of content (e.g. text file, image, ...). Information for the client to
   * render the content correctly.
   */
  public MimeType getMimeType();

  /**
   * The renderInfo property is used by the user interface to determine how to
   * render a content-representation, the supported formats are defined in
   * {@link RenderInfo}.
   */
  public RenderInfo getRenderInfo();

  // TODO: Think about that, maybe as annotation in the Plugin-Config
  // public boolean isDownloadable();
}