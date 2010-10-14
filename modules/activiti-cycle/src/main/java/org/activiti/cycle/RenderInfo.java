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
  /* The UI will attempt to render HTML artifact representations as HTML. */
  HTML,
  /*
   * The UI will not attempt to render binary content-representations, it will
   * however display an icon based on the mimeType property of the
   * content-representation.
   */
  BINARY;

}
