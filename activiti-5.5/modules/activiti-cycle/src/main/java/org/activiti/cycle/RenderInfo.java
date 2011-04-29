package org.activiti.cycle;

/**
 * Provides a list of available rendering formats.
 * 
 * @author nils.preusker@camunda.com
 */
public enum RenderInfo {

  /*
   * The UI will render plain text in a plain text tab without syntax
   * highlighting.
   */
  TEXT_PLAIN,
  /*
   * The UI will render code as text and attempt to provide syntax highlighting
   * based on the MimeType property of the content-representation.
   */
  CODE,
  /* The UI will attempt to render images as HTML image tags. */
  IMAGE,
  /* ContentRepresentations that use the HTML RenderInfo should return HTML as 
   * content, the UI will attempt to render HTML in an iframe. */
  HTML,
  /* ContentRepresentations that use the HTML_REFERENCE RenderInfo should return 
   * a URL as content, the UI will then use this URL as "src" attribute in the 
   * iframe it uses to render the ContentRepresentation */
  HTML_REFERENCE,
  /*
   * The UI will not attempt to render binary content-representations, it will
   * however display an icon based on the mimeType property of the
   * content-representation.
   */
  BINARY;

}
