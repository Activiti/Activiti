package org.activiti.cycle.impl.representation;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.mimetype.JpegMimeType;

/**
 * Default {@link ContentRepresentation} for {@link JpegMimeType}
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class DefaultPngContentRepresentation extends AbstractBasicArtifactTypeContentRepresentation {

  private static final long serialVersionUID = 1L;

  public RenderInfo getRenderInfo() {
    return RenderInfo.IMAGE;
  }

  protected Class< ? extends MimeType> getMimeType() {
    return JpegMimeType.class;
  }

}
