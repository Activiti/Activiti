package org.activiti.cycle;

/**
 * Provides a list of standard content-type strings and a factory method to create
 * MimeType objects for custom content-type strings.
 * 
 * A content-type is usually a string, starting with one of "application", "audio", "example",
 * "image", "message", "model", "multipart", "text" or "video", followed by "/" and the name of 
 * the content-type, e.g. "application/pdf", "text/plain" or "image/jpeg".
 * 
 * @author bernd.ruecker@camunda.com
 * @author nils.preusker@camunda.com
 */
public enum StandardMimeType implements MimeType {

  PNG("image/png"),
  GIF("image/gif"),
  JPEG("image/jpeg"),
  XML("application/xml"),
  HTML("text/html"),
  TEXT("text/plain"),
  PDF("application/pdf"),
  JSON("application/json;charset=UTF-8"),
  MS_WORD("application/msword"),
  MS_POWERPOINT("application/powerpoint"),
  MS_EXCEL("application/excel"),
  JAVASCRIPT("application/javascript");

  private String contentType;

  private StandardMimeType(String contentType) {
    this.contentType = contentType;
  }

  public String getContentType() {
    return this.contentType;
  }

  /**
   * Creates a MimeType instance for a given content-type.
   * 
   * @param contentType the content-type to create a MimeType instance for.
   */
  public MimeType getMimeTypeFor(final String contentType) {
    // Check whether string equals one of the pre-defined 
    // standard content-types
    for (StandardMimeType standardMimeType : values()) {
      if (standardMimeType.getContentType().equals(contentType)) {
        return standardMimeType;
      }
    }
    // create a new MymeType instance if the provided string
    // is not one of the pre-defined content-types
    return new MimeType() {
      public String getContentType() {
        return contentType;
      }
    };
  }

}

