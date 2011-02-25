package org.activiti.cycle.impl.representation;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.mimetype.GifMimeType;

/**
 * Default {@link ContentRepresentation} for {@link GifMimeType}
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class DefaultGifContentRepresentation extends AbstractBasicArtifactTypeContentRepresentation {

  private static final long serialVersionUID = 1L;

  public RenderInfo getRenderInfo() {
    return RenderInfo.IMAGE;
  }

  protected Class< ? extends MimeType> getMimeType() {
    return GifMimeType.class;
  }

}
