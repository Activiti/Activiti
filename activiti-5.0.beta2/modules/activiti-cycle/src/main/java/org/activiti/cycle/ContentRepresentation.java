package org.activiti.cycle;

import java.io.Serializable;

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
public interface ContentRepresentation extends Serializable {
	
  /**
   * Name of this representation, serves as a <b>unique key</b> to query the
   * correct representation and may be used by the client to show a list of
   * possible {@link ContentRepresentationDefinition}s
   */
	public String getId();

	/**
   * type of content as normally indicated by {@link ContentType} (e.g. text
   * file, image, ...). Information for the client to render it correctly.
   */
  public String getMimeType();
	// TODO: Think about that, maybe as annotation in the Plugin-Config
  // public boolean isDownloadable();
}
